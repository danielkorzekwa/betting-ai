package dk.bettingai.marketsimulator

import ISimulator._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.trader._
import ITrader._
import IMarket._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import dk.bettingai.marketsimulator.risk._
import scala.io._
import java.io.File
import java.io.BufferedReader
import java.io.FileReader
import scala.annotation._
import scala.collection._
import immutable.TreeMap
import scala.actors.Actor
import Actor._
import java.util.concurrent.atomic._

/**This trait represents a simulator that processes market events, analyses trader implementation and returns analysis report for trader implementation.
 * 
 * @author korzekwad
 *
 * 
 * @param MarketEventProcessor
 * @param betex
 * @param commission Commission on winnings in percentage.
 *
 */
class Simulator(betex: IBetex, commission: Double) extends ISimulator {

  var nextBetIdValue = new AtomicLong(1)
  def nextBetId = () => nextBetIdValue.addAndGet(1)

  var lastTraderUserId = new AtomicInteger(1)
  def nextTraderUserId() = lastTraderUserId.addAndGet(1)
  val historicalDataUserId = nextTraderUserId()

  /** Processes market events, analyses traders and returns analysis reports.
   * 
   * @param marketDataContains market events that the market simulation is executed for. Key - marketId, value - market events
   * @param traders Traders to analyse, all are analysed on the same time, so they compete against each other
   * @param p Progress listener. Value between 0% and 100% is passed as an function argument.
   */
  def runSimulation(marketData: TreeMap[Long, File], traders: List[ITrader], p: (Int) => Unit): SimulationReport = {

    /**Register traders on a betting exchange by assigning user ids for them.*/
    val registeredTraders = traders.map(trader => RegisteredTrader(nextTraderUserId(), trader))

    val numOfMarkets = marketData.size

    p(0)

    /**Process all markets in parallel and send back market reports.*/
    for ((marketIndex, marketId) <- marketData.keys.zipWithIndex.map { case (mId, index) => (index, mId) }.toList.sorted) {
      val slave: Actor = actor {
        react {
          case marketId: Long => {
            val marketReport = processMarketFile(marketId, marketData(marketId), registeredTraders)
            sender ! marketReport
          }
        }
      }
      slave ! marketId
    }

    /**Collect market reports from slaves.*/
    val marketReports = mutable.ListBuffer[Option[MarketReport]]()
    while (marketReports.size != numOfMarkets) {
      receive {
        case marketReport: Option[MarketReport] => {
          val prevProgress = ((marketReports.size - 1).max(0) * 100) / numOfMarkets
          marketReports += marketReport
          val newProgress = ((marketReports.size - 1).max(0) * 100) / numOfMarkets
          if (newProgress > prevProgress) p(newProgress)
        }
      }
    }
    val nonEmptyReports = marketReports.filter(r => r.isDefined).map(_.get).toList.sortWith((a,b) => a.marketTime.getTime < b.marketTime.getTime)

    p(100)
    SimulationReport(nonEmptyReports)

  }

  /**Process all market events and returns market reports.*/
  private def processMarketFile(marketId: Long, marketFile: File, traders: List[RegisteredTrader]): Option[MarketReport] = {

	val marketEventProcessor = new MarketEventProcessorImpl(betex)
	  
    val marketDataReader = new BufferedReader(new FileReader(marketFile))

    /**Process CREATE_MARKET EVENT*/
    val createMarketEvent = marketDataReader.readLine

    val marketReport = if (createMarketEvent != null) {
      val processedEventTimestamp = marketEventProcessor.process(createMarketEvent, nextBetId(), historicalDataUserId)
      val market = betex.findMarket(marketId)

      val traderContexts: List[Tuple2[RegisteredTrader, TraderContext]] = for (trader <- traders) yield trader -> new TraderContext(nextBetId(), trader.userId, market, commission, this)
      traderContexts.foreach { case (trader, ctx) => ctx.setEventTimestamp(processedEventTimestamp) }
      traderContexts.foreach { case (trader, ctx) => trader.init(ctx) }

      @tailrec
      def processMarketEvents(marketEvent: String, eventTimestamp: Long): Unit = {

        val processedEventTimestamp = marketEventProcessor.process(marketEvent, nextBetId(), historicalDataUserId)

        /**Triggers traders for all markets on a betting exchange.
         * Warning! traders actually should be called within marketEventProcessor.process, after event time stamp is parsed and before event is added to betting exchange.*/
        if (processedEventTimestamp > eventTimestamp) traderContexts.foreach { case (trader, ctx) => trader.execute(ctx) }

        traderContexts.foreach { case (trader, ctx) => ctx.setEventTimestamp(processedEventTimestamp) }

        /**Recursive  call.*/
        val nextMarketEvent = marketDataReader.readLine
        if (nextMarketEvent == null) {
          /**Process remaining events*/
          if (processedEventTimestamp <= eventTimestamp) traderContexts.foreach { case (trader, ctx) => trader.execute(ctx) }
        } else processMarketEvents(nextMarketEvent, processedEventTimestamp)
      }

      /**Process remaining market events.*/
      val marketEvent = marketDataReader.readLine
      if (marketEvent != null) processMarketEvents(marketEvent, processedEventTimestamp)
      traderContexts.foreach { case (trader, ctx) => trader.after(ctx) }

      /**Create market report.*/
      Option(createMarketReport(marketId, traderContexts))
    } else None

    marketReport
  }

  /** Calculates market expected profit based on all bets that are placed by traders on all betting exchange markets.
   * 
   * @param marketId
   * @param traderContext
   * @return Market report with market expected profit for all traders and with a few others statistics.
   */
  private def createMarketReport(marketId: Long, traderContexts: List[Tuple2[RegisteredTrader, TraderContext]]): MarketReport = {
    val market = betex.findMarket(marketId)

    val traderReports = for {
      (trader, traderCtx) <- traderContexts

      val marketPrices = market.getBestPrices().mapValues(prices => prices._1.price -> prices._2.price)
      val marketProbs = ProbabilityCalculator.calculate(marketPrices, market.numOfWinners)
      val matchedBets = market.getBets(traderCtx.userId).filter(_.betStatus == M)
      val unmatchedBets = market.getBets(traderCtx.userId).filter(_.betStatus == U)
      val marketExpectedProfit = ExpectedProfitCalculator.calculate(matchedBets, marketProbs, commission)

    } yield TraderReport(trader, marketExpectedProfit, matchedBets.size, unmatchedBets.size, traderCtx.getChartLabels, traderCtx.getChartValues)

    MarketReport(market.marketId, market.marketName, market.eventName, market.marketTime,traderReports)
  }

  /**Registers new trader and return trader context. 
   * This context can be used to trigger some custom traders that are registered manually by a master trader, 
   * e.g. when testing some evolution algorithms for which more than one trader is required.
   * @return trader context
   */
  def registerTrader(market: IMarket): ITraderContext = new TraderContext(nextBetId(), nextTraderUserId(), market, commission, this)
}