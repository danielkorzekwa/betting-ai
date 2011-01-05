package dk.bettingai.marketsimulator

import ISimulator._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.trader._
import ITrader._
import IMarket._
import Simulator._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import dk.bettingai.marketsimulator.risk._
import scala.io._
import java.io.File
import java.io.BufferedReader
import java.io.FileReader
import scala.annotation._

/**This trait represents a simulator that processes market events, analyses trader implementation and returns analysis report for trader implementation.
 * 
 * @author korzekwad
 *
 */
object Simulator {

  /**
   * @param chartLabels Labels for all chart series.
   * @param chartValues Key - time stamp, value - list of values for all series in the same order as labels.
   */
  class MarketRiskReport(val marketId: Long, val marketName: String, val eventName: String, val marketExpectedProfit: MarketExpectedProfit,
    val matchedBetsNumber: Long, val unmatchedBetsNumber: Long, val chartLabels: List[String], val chartValues: List[Tuple2[Long, List[Double]]]) extends IMarketRiskReport {
    override def toString() = "MarketRiskReport [marketId=%s, marketName=%s, eventName=%s, marketExpectedProfit=%s, matchedBetsNumber=%s, unmatchedBetsNumber=%s, chartLabels=%s, chartValues=%s]".format(marketId, marketName, eventName, marketExpectedProfit, matchedBetsNumber, unmatchedBetsNumber, chartLabels, chartValues)
  }

}

/**
 * 
 * @param MarketEventProcessor
 * @param betex
 * @param commission Commission on winnings in percentage.
 *
 */
class Simulator(marketEventProcessor: MarketEventProcessor, betex: IBetex, commission: Double) extends ISimulator {

  var nextBetIdValue = 1
  def nextBetId = () => { nextBetIdValue += 1; nextBetIdValue }

  var lastTraderUserId = 1
  def nextTraderUserId()= {lastTraderUserId += 1; lastTraderUserId }
  val historicalDataUserId = nextTraderUserId()
  val traderUserId = nextTraderUserId()

  /** Processes market events, analyses trader implementation and returns analysis report for trader implementation.
   * 
   * @param marketDataContains market events that the market simulation is executed for. Key - marketId, value - market events
   * @param trader
   * @param p Progress listener. Value between 0% and 100% is passed as an function argument.
   */
  def runSimulation(marketData: Map[Long, File], trader: ITrader, p: (Int) => Unit): List[IMarketRiskReport] = {

    val numOfMarkets = marketData.size

    p(0)

    /**Run simulation and return list of all market contexts that are used to build simulation report.*/
    val traderContexts = for {
      ((marketId, marketFile), marketIndex) <- marketData.zipWithIndex

      val traderContext = {
        /**Update progress listener.*/
      	val prevProgress = ((marketIndex - 1).max(0) * 100) / numOfMarkets
        val newProgress = (marketIndex * 100) / numOfMarkets
        if (newProgress > prevProgress) p(newProgress)
        
        /**Process market data.*/
        processMarketFile(marketId, marketFile, trader, commission)
      }
      
      if (!traderContext.isEmpty)
    } yield traderContext.get

    p(100)
    val riskReport = betex.getMarkets.map(market => calculateRiskReport(traderUserId, market, traderContexts.find(_.marketId == market.marketId).get, commission))
    riskReport
  }

  /**Process all market events.*/
  private def processMarketFile(marketId: Long, marketFile: File, trader: ITrader, commission: Double): Option[TraderContext] = {

    val marketDataReader = new BufferedReader(new FileReader(marketFile))

    /**Process CREATE_MARKET EVENT*/
    val createMarketEvent = marketDataReader.readLine
    
    val ctx = if (createMarketEvent != null) {
      val processedEventTimestamp = marketEventProcessor.process(createMarketEvent, nextBetId(), historicalDataUserId)
      val market = betex.findMarket(marketId)
      val traderContext = new TraderContext(nextBetId(), traderUserId, market, commission,this)
      traderContext.setEventTimestamp(processedEventTimestamp)
      trader.init(traderContext)

      @tailrec
      def processMarketEvents(marketEvent: String, traderContext: TraderContext, eventTimestamp: Long): Unit = {

        val processedEventTimestamp = marketEventProcessor.process(marketEvent, nextBetId(), historicalDataUserId)
        traderContext.setEventTimestamp(processedEventTimestamp)

        /**Triggers trader implementation for all markets on a betting exchange, so it can take appropriate bet placement decisions.*/
        if (processedEventTimestamp > eventTimestamp) trader.execute(traderContext)

        /**Recursive  call.*/
        val nextMarketEvent = marketDataReader.readLine
        if (nextMarketEvent == null) {
          /**Process remaining events*/
          if (processedEventTimestamp <= eventTimestamp) trader.execute(traderContext)
        } else processMarketEvents(nextMarketEvent, traderContext, processedEventTimestamp)
      }
      
      /**Process remaining market events.*/
      val marketEvent = marketDataReader.readLine
      if (marketEvent != null) processMarketEvents(marketEvent, traderContext, processedEventTimestamp)
      trader.after(traderContext)

      Option(traderContext)
    } else None

    ctx
  }
  /**Calculates market expected profit based on all bets that are placed by the trader implementation on all betting exchange markets.
   * 
   * @param traderUserId
   * @param market
   * @param traderContext
   * @param commision Commission on winnings in percentage.
   * @return
   */
  private def calculateRiskReport(traderUserId: Int, market: IMarket, traderContext: TraderContext, commission: Double): IMarketRiskReport = {

    val marketPrices = market.getBestPrices().mapValues(prices => prices._1.price -> prices._2.price)
    val marketProbs = ProbabilityCalculator.calculate(marketPrices, market.numOfWinners)
    val matchedBets = market.getBets(traderUserId).filter(_.betStatus == M)
    val unmatchedBets = market.getBets(traderUserId).filter(_.betStatus == U)
    val marketExpectedProfit = ExpectedProfitCalculator.calculate(matchedBets, marketProbs, commission)

    new MarketRiskReport(market.marketId, market.marketName, market.eventName, marketExpectedProfit, matchedBets.size, unmatchedBets.size, traderContext.getChartLabels, traderContext.getChartValues)
  }
  
  /**Registers new trader and return trader context. 
     * This context can be used to trigger some custom traders that are registered manually by a master trader, 
     * e.g. when testing some evolution algorithms for which more than one trader is required.
     * @return trader context
     */
    def registerTrader(market:IMarket):ITraderContext = new TraderContext(nextBetId(), nextTraderUserId(), market, commission,this)
}