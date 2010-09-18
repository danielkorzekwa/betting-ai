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
import dk.bettingai.marketsimulator.risk.IExpectedProfitCalculator._
import java.io.File
import java.io.BufferedReader
import java.io.FileReader

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
	class MarketRiskReport(val marketId:Long,val marketName:String,val eventName:String,val marketExpectedProfit:MarketExpectedProfit,
val matchedBetsNumber:Long,val unmatchedBetsNumber:Long,	val chartLabels:List[String],val chartValues:List[Tuple2[Long,List[Double]]]) extends IMarketRiskReport {
		override def toString() = "MarketRiskReport [marketId=%s, marketName=%s, eventName=%s, marketExpectedProfit=%s, matchedBetsNumber=%s, unmatchedBetsNumber=%s, chartLabels=%s, chartValues=%s]".format(marketId,marketName,eventName,marketExpectedProfit,matchedBetsNumber,unmatchedBetsNumber,chartLabels,chartValues)
	}

}

class Simulator(marketEventProcessor:MarketEventProcessor,betex:IBetex) extends ISimulator{

/** Processes market events, analyses trader implementation and returns analysis report for trader implementation.
 * 
 * @param marketDataContains market events that the market simulation is executed for. Key - marketId, value - market events
 * @param trader
 * @param traderUserId
 * @param historicalDataUserId
 * @param p Progress listener. Value between 0% and 100% is passed as an function argument.
 * @param commision Commission on winnings in percentage.
 */
	def runSimulation(marketData:Map[Long,File], trader:ITrader,traderUserId:Int,historicalDataUserId:Int,p: (Int) => Unit,commission:Double):List[IMarketRiskReport] = {

			var nextBetIdValue=1
			val nextBetId = () => {nextBetIdValue = nextBetIdValue +1;nextBetIdValue}

			val numOfMarkets = marketData.size
			val marketDataIterator = marketData.iterator

			/**Process all markets data.*/
			def processMarketData(marketIndex:Int,progress:Int):List[TraderContext] = {
					val (marketId,marketEvents) = marketDataIterator.next

					val newProgress=(marketIndex*100)/numOfMarkets
					if(newProgress>progress) p(newProgress)

					val in = new BufferedReader(new FileReader(marketEvents));

					/**Process all market events.*/
					def processMarketEvents(marketEvent:String,traderContext:ITraderContext,eventTimestamp:Long):Unit = {

							val processedEventTimestamp = marketEventProcessor.process(marketEvent,nextBetId(),historicalDataUserId)

							/**Triggers trader implementation for all markets on a betting exchange, so it can take appropriate bet placement decisions.*/
							if(processedEventTimestamp>eventTimestamp) trader.execute(processedEventTimestamp,traderContext)

							/**Recursive  call.*/
							val nextMarketEvent = in.readLine
							if(nextMarketEvent==null) {
								/**Process remaining events*/
								if(processedEventTimestamp<=eventTimestamp) trader.execute(processedEventTimestamp,traderContext)
							}
							else processMarketEvents(nextMarketEvent,traderContext,processedEventTimestamp)
					}

					/**Process CREATE_MARKET EVENT*/
					val createMarketEvent = in.readLine	
					if(createMarketEvent!=null) {
					val processedEventTimestamp = marketEventProcessor.process(createMarketEvent,nextBetId(),historicalDataUserId)
					val market = betex.findMarket(marketId)
					val traderContext = new TraderContext(nextBetId(),traderUserId,market,commission)
					trader.init(traderContext)
					val marketEvent = in.readLine
					if(marketEvent!=null) processMarketEvents(marketEvent,traderContext,processedEventTimestamp)
					val traderContexts = if(marketDataIterator.hasNext) processMarketData(marketIndex+1,newProgress) else Nil
					traderContext :: traderContexts
					}
					else Nil
			}

			p(0)
			val traderContexts = if(!marketDataIterator.isEmpty)	processMarketData(0,0) else Nil
			p(100)
			val riskReport = 	betex.getMarkets.map(market => calculateRiskReport(traderUserId,market,traderContexts.find(_.marketId==market.marketId).get,commission))
			riskReport
	}

	/**Calculates market expected profit based on all bets that are placed by the trader implementation on all betting exchange markets.
	 * 
	 * @param traderUserId
	 * @param market
	 * @param traderContext
	 * @param commision Commission on winnings in percentage.
	 * @return
	 */
	private def calculateRiskReport(traderUserId:Int,market:IMarket,traderContext:TraderContext,commission:Double):IMarketRiskReport = {

			val marketPrices = market.getBestPrices()
			val marketProbs = ProbabilityCalculator.calculate(marketPrices,market.numOfWinners)
			val matchedBets = market.getBets(traderUserId).filter(_.betStatus==M)
			val unmatchedBets = market.getBets(traderUserId).filter(_.betStatus==U)
			val marketExpectedProfit = ExpectedProfitCalculator.calculate(matchedBets,marketProbs,commission)

			new MarketRiskReport(market.marketId,market.marketName,market.eventName, marketExpectedProfit,matchedBets.size,unmatchedBets.size,traderContext.getChartLabels,traderContext.getChartValues)	
	}
}