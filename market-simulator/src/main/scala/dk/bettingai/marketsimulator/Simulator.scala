package dk.bettingai.marketsimulator

import ISimulator._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.trader._
import ITrader._
import Simulator._
import IBet.BetTypeEnum._

/**This trait represents a simulator that processes market events, analyses trader implementation and returns analysis report for trader implementation.
 * 
 * @author korzekwad
 *
 */
object Simulator {
	class MarketRiskReport(val marketId:Long,val marketName:String,val eventName:String,val expectedProfit:Double,val matchedBetsNumber:Long,val unmatchedBetsNumber:Long) extends IMarketRiskReport {

		override def toString() = "MarketRiskReport [marketId=%s, marketName=%s, eventName=%s, expectedProfit=%s, matchedBetsNumber=%s, unmatchedBetsNumber=%s]".format(marketId,marketName,eventName,expectedProfit,matchedBetsNumber,unmatchedBetsNumber)
	}

	class TraderContext(nextBetId: () => Long,userId:Long, market:IMarket) extends ITraderContext {
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
			market.placeBet(nextBetId(),userId,betSize,betPrice,betType,runnerId)
		}

		/**Returns best toBack/toLay prices for market runner.
		 * Element 1 - best price to back, element 2 - best price to lay
		 * Double.NaN is returned if price is not available.
		 * @return 
		 * */
		def getBestPrices(runnerId: Long): Tuple2[Double,Double] = market.getBestPrices(runnerId)
	}
}
class Simulator(marketEventProcessor:MarketEventProcessor,trader:ITrader,traderUserId:Long,firstTraderBetId:Long,betex:IBetex) extends ISimulator{

	var nextBetId=firstTraderBetId

	/** Processes market event in a json format and calls appropriate method on a betting exchange.
	 * 
	 * @param marketEvent 
	 */
	def process(marketEvent:String) = marketEventProcessor.process(marketEvent)

	/** Triggers trader implementation for all markets on a betting exchange, so it can take appropriate bet placement decisions.
	 * 
	 */
	def callTrader = {
		for(market <- betex.getMarkets) {
			val traderContext = new TraderContext( () => {nextBetId = nextBetId +1;nextBetId},traderUserId,market)
			trader.execute(traderContext)
		}
	}

	/**Calculates market expected profit based on all bets that are placed by the trader implementation on all betting exchange markets.
	 * 
	 * @return
	 */
	def calculateRiskReport:List[IMarketRiskReport] = {

			def calculateMarketReport(market:IMarket):IMarketRiskReport =  new MarketRiskReport(market.marketId,market.marketName,market.eventName, 0,0,0)
			betex.getMarkets.map(calculateMarketReport)
	}
}