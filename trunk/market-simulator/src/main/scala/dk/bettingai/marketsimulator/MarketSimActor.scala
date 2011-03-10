package dk.bettingai.marketsimulator

import scala.actors._
import java.io.File
import ISimulator._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.betex.api._
import java.io.BufferedReader
import java.io.FileReader
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.risk._
import IBet.BetStatusEnum._
import scala.annotation._
import MarketSimActor._

/**This is an actor that performs simulation for a given market, then returns simulation report to the sender and finally dies.
 * 
 * @author korzekwad
 *
 */
object MarketSimActor {
  case class MarketSimRequest(
    marketId: Long,
    marketFile: File,
    registeredTraders: List[RegisteredTrader])
}

/**
 * @param newTraderContext (traderUserId,market) => trader context
 *
 */
class MarketSimActor(betex: IBetex, nextBetId: () => Long, historicalDataUserId: Int, commission: Double,
  newTraderContext: (Int, IMarket) => TraderContext) extends Actor {

  def act {
    react {
      case req: MarketSimRequest => {
        try {
          val marketReport = processMarketFile(req.marketId, req.marketFile, req.registeredTraders)
          sender ! marketReport
        } catch {
          case e: Exception => { sender ! None; throw e }
        }
      }
    }
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

      val traderContexts: List[Tuple2[RegisteredTrader, TraderContext]] = for (trader <- traders) yield trader -> newTraderContext(trader.userId, market)
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
      val report = Option(createMarketReport(marketId, traderContexts))
      
      /**Remove market from betex after market simulation is finished.*/
      betex.removeMarket(marketId)
      
      report
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

    MarketReport(market.marketId, market.marketName, market.eventName, market.marketTime, traderReports)
  }
}