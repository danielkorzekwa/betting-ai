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
	
	class MarketRiskReport(val marketId:Long,val marketName:String,val eventName:String,val marketExpectedProfit:MarketExpectedProfit,

			/**
			 * @param chartLabels Labels for all chart series.
			 * @param chartValues Key - time stamp, value - list of values for all series in the same order as labels.
			 */
			val marketProbs:Map[Long,Double],val matchedBetsNumber:Long,val unmatchedBetsNumber:Long,	val chartLabels:List[String],val chartValues:List[Tuple2[Long,List[Double]]]) extends IMarketRiskReport {
		override def toString() = "MarketRiskReport [marketId=%s, marketName=%s, eventName=%s, marketExpectedProfit=%s, marketProbs=%s,matchedBetsNumber=%s, unmatchedBetsNumber=%s, chartLabels=%s, chartValues=%s]".format(marketId,marketName,eventName,marketExpectedProfit,marketProbs,matchedBetsNumber,unmatchedBetsNumber,chartLabels,chartValues)
	}

	class TraderContext(nextBetId: => Long,userId:Int, market:IMarket) extends ITraderContext {
		val marketId = market.marketId
		val marketName = market.marketName
		val eventName = market.eventName
		val numOfWinners = market.numOfWinners
		val marketTime = market.marketTime
		val runners = market.runners

		var chartLabels = List[String]()
		/**Key - time stamp, value - list of values for all series in the same order as labels.*/
		val chartData = new scala.collection.mutable.ListBuffer[Tuple2[Long,List[Double]]]()
		
		/**Returns labels for all chart series.*/
		def getChartLabels:List[String] = chartLabels

		/**Set labels for all chart series.*/
		def setChartLabels(chartLabels:List[String]) {this.chartLabels = chartLabels}
		
		/**Returns chart values for all time series in the same order as chart labels. 
		 * Key - time stamp, value - list of values for all series in the same order as labels.*/
		def getChartValues:List[Tuple2[Long,List[Double]]] = chartData.toList

			/**Add chart values to time line chart. Key - time stamp, value - list of values for all series in the same order as labels.*/
		def addChartValues(chartValues:Tuple2[Long,List[Double]]) {chartData += chartValues}
	
		
		/** Places a bet on a betting exchange market.
		 * 
		 * @param betSize
		 * @param betPrice
		 * @param betType
		 * @param runnerId
		 */
		def placeBet(betSize:Double, betPrice:Double, betType:BetTypeEnum, runnerId:Long) {
			market.placeBet(nextBetId,userId,betSize,betPrice,betType,runnerId)
		}

		/**Returns best toBack/toLay prices for market runner.
		 * Element 1 - best price to back, element 2 - best price to lay
		 * Double.NaN is returned if price is not available.
		 * @return 
		 * */
		def getBestPrices(runnerId: Long): Tuple2[Double,Double] = market.getBestPrices(runnerId)

		/**Returns best toBack/toLay prices for market.
		 * 
		 * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
		 */
		def getBestPrices():Map[Long,Tuple2[Double,Double]] = {
				market.getBestPrices()
		}

		/**Returns all bets placed by user on that market.
		 *
		 *@param matchedBetsOnly If true then matched bets are returned only, 
		 * otherwise all unmatched and matched bets for user are returned.
		 */
		def getBets(matchedBetsOnly:Boolean):List[IBet] = market.getBets(userId,matchedBetsOnly)

		/** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
		 *  Prices with zero volume are not returned by this method.
		 * 
		 * @param runnerId Unique runner id that runner prices are returned for.
		 * @return
		 */
		def getRunnerPrices(runnerId:Long):List[IRunnerPrice] = market.getRunnerPrices(runnerId)

		/**Returns total traded volume for all prices on all runners in a market.*/
		def getRunnerTradedVolume(runnerId:Long): List[IPriceTradedVolume] = market.getRunnerTradedVolume(runnerId)

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
 */
	def runSimulation(marketData:Map[Long,File], trader:ITrader,traderUserId:Int,historicalDataUserId:Int,p: (Int) => Unit):List[IMarketRiskReport] = {

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
					val traderContext = new TraderContext(nextBetId(),traderUserId,market)
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
			val riskReport = 	betex.getMarkets.map(market => calculateRiskReport(traderUserId,market,traderContexts.find(_.marketId==market.marketId).get))
			riskReport
	}

	/**Calculates market expected profit based on all bets that are placed by the trader implementation on all betting exchange markets.
	 * 
	 * @return
	 */
	private def calculateRiskReport(traderUserId:Int,market:IMarket,traderContext:TraderContext):IMarketRiskReport = {

			val marketPrices = market.getBestPrices()
			val marketProbs = ProbabilityCalculator.calculate(marketPrices,market.numOfWinners)
			val matchedBets = market.getBets(traderUserId).filter(_.betStatus==M)
			val unmatchedBets = market.getBets(traderUserId).filter(_.betStatus==U)
			val marketExpectedProfit = ExpectedProfitCalculator.calculate(matchedBets,marketProbs)

			new MarketRiskReport(market.marketId,market.marketName,market.eventName, marketExpectedProfit,marketProbs,matchedBets.size,unmatchedBets.size,traderContext.getChartLabels,traderContext.getChartValues)	
	}
}