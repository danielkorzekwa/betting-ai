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
			val marketProbs:Map[Long,Double],val matchedBetsNumber:Long,val unmatchedBetsNumber:Long) extends IMarketRiskReport {
		override def toString() = "MarketRiskReport [marketId=%s, marketName=%s, eventName=%s, marketExpectedProfit=%s, marketProbs=%s,matchedBetsNumber=%s, unmatchedBetsNumber=%s]".format(marketId,marketName,eventName,marketExpectedProfit,marketProbs,matchedBetsNumber,unmatchedBetsNumber)
	}

	class TraderContext(nextBetId: => Long,userId:Int, market:IMarket) extends ITraderContext {
		val marketId = market.marketId
		val marketName = market.marketName
		val eventName = market.eventName
		val numOfWinners = market.numOfWinners
		val marketTime = market.marketTime
		val runners = market.runners

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
			def processMarketData(marketIndex:Int,progress:Int):Unit = {
					val (marketId,marketEvents) = marketDataIterator.next

					val newProgress=(marketIndex*100)/numOfMarkets
					if(newProgress>progress) p(newProgress)

					val in = new BufferedReader(new FileReader(marketEvents));

					/**Process all market events.*/
					def processMarketEvents(marketEvent:String,eventTimestamp:Long):Unit = {

							val processedEventTimestamp = marketEventProcessor.process(marketEvent,nextBetId(),historicalDataUserId)

							/**Triggers trader implementation for all markets on a betting exchange, so it can take appropriate bet placement decisions.*/
							def triggerTrader() {
								val market = betex.findMarket(marketId)
								val traderContext = new TraderContext(nextBetId(),traderUserId,market)
								trader.execute(traderContext)
							}
							if(processedEventTimestamp>eventTimestamp) triggerTrader()

							/**Recursive  call.*/
							val nextMarketEvent = in.readLine
							if(nextMarketEvent==null) {
								/**Process remaining events*/
								if(processedEventTimestamp<=eventTimestamp) triggerTrader()
							}
							else processMarketEvents(nextMarketEvent,processedEventTimestamp)
					}
					
					val marketEvent = in.readLine
					if(marketEvent!=null) processMarketEvents(marketEvent,0)
					if(marketDataIterator.hasNext) processMarketData(marketIndex+1,newProgress)
			}

			p(0)
			if(!marketDataIterator.isEmpty)	processMarketData(0,0)	
			p(100)
			val riskReport = 	betex.getMarkets.map(calculateRiskReport(traderUserId,_))
			riskReport
	}

	/**Calculates market expected profit based on all bets that are placed by the trader implementation on all betting exchange markets.
	 * 
	 * @return
	 */
	private def calculateRiskReport(traderUserId:Int,market:IMarket):IMarketRiskReport = {

			val marketPrices = market.getBestPrices()
			val marketProbs = ProbabilityCalculator.calculate(marketPrices,market.numOfWinners)
			val matchedBets = market.getBets(traderUserId).filter(_.betStatus==M)
			val unmatchedBets = market.getBets(traderUserId).filter(_.betStatus==U)
			val marketExpectedProfit = ExpectedProfitCalculator.calculate(matchedBets,marketProbs)

			new MarketRiskReport(market.marketId,market.marketName,market.eventName, marketExpectedProfit,marketProbs,matchedBets.size,unmatchedBets.size)	
	}
}