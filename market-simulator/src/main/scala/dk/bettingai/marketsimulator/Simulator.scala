package dk.bettingai.marketsimulator

import ISimulator._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.trader._
import ITrader._
import Simulator._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import dk.bettingai.marketsimulator.risk._
import scala.io._

/**This trait represents a simulator that processes market events, analyses trader implementation and returns analysis report for trader implementation.
 * 
 * @author korzekwad
 *
 */
object Simulator {
	class MarketRiskReport(val marketId:Long,val marketName:String,val eventName:String,val expectedProfit:Double,val matchedBetsNumber:Long,val unmatchedBetsNumber:Long) extends IMarketRiskReport {

		override def toString() = "MarketRiskReport [marketId=%s, marketName=%s, eventName=%s, expectedProfit=%s, matchedBetsNumber=%s, unmatchedBetsNumber=%s]".format(marketId,marketName,eventName,expectedProfit,matchedBetsNumber,unmatchedBetsNumber)
	}

	class TraderContext(nextBetId: => Long,userId:Long, market:IMarket) extends ITraderContext {
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
	}
	
}
class Simulator(marketEventProcessor:MarketEventProcessor,trader:ITrader,traderUserId:Int,firstTraderBetId:Long,betex:IBetex) extends ISimulator{

	var nextBetIdValue=firstTraderBetId
	val nextBetId = () => {nextBetIdValue = nextBetIdValue +1;nextBetIdValue}

	/** Processes market events, analyses trader implementation and returns analysis report for trader implementation.
	 * @param Contains market events that the market simulation is executed for.
	 * @param p Progress listener. Value between 0% and 100% is passed as an function argument.
	 */
	def runSimulation(marketData:Source, p: (Int) => Unit):List[IMarketRiskReport]= {

			val marketDataFileSize = marketData.reset.size
			var marketDataFileSizeRead=0

			var currentProgress=0
			for(marketEvent <- marketData.reset.getLines()) {
				marketDataFileSizeRead = marketDataFileSizeRead + marketEvent.size
				currentProgress=(marketDataFileSizeRead*100)/marketDataFileSize
				p(currentProgress)
				marketEventProcessor.process(marketEvent)

				/**Triggers trader implementation for all markets on a betting exchange, so it can take appropriate bet placement decisions.*/
				for(market <- betex.getMarkets) {
					val traderContext = new TraderContext(nextBetId(),traderUserId,market)
					trader.execute(traderContext)
				}
			}
			val riskReport = calculateRiskReport
			if(currentProgress<100) p(100)
			riskReport
	}

	/**Calculates market expected profit based on all bets that are placed by the trader implementation on all betting exchange markets.
	 * 
	 * @return
	 */
	def calculateRiskReport:List[IMarketRiskReport] = {

					def calculateMarketReport(market:IMarket):IMarketRiskReport =  {
							val marketPrices = Map(market.runners.map(r => r.runnerId -> market.getBestPrices(r.runnerId)) : _*)
							val marketProbs = ProbabilityCalculator.calculate(marketPrices,market.numOfWinners)
							val matchedBets = market.getBets(traderUserId).filter(_.betStatus==M)
							val unmatchedBets = market.getBets(traderUserId).filter(_.betStatus==U)
							val marketExpectedProfit = ExpectedProfitCalculator.calculate(matchedBets,marketProbs)

							new MarketRiskReport(market.marketId,market.marketName,market.eventName, marketExpectedProfit,matchedBets.size,unmatchedBets.size)
					}

					betex.getMarkets.map(calculateMarketReport)
			}
}