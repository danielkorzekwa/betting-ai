package dk.bettingai.marketcollector.marketservice

import java.util.Date
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.api._
import IBet._
import BetTypeEnum._
import BetStatusEnum._
import IMarket._
import Market._
import IMarketService._

object IMarketService {
	
	/**
	 * @param inPlayDelay if bigger than 0 then market is in play.
	 * @param runnerPrices Key - runnerId, Value - List[IRunnerPrice]
	 */
	case class MarketPrices(val inPlayDelay:Int, val runnerPrices:Map[Long,List[IRunnerPrice]]) {
		override def toString = "MarketPrices [inPlayDelay=%s, runnerPrices=%s]".format(inPlayDelay,runnerPrices)
	}
	
		/**
	 * @param inPlayDelay if bigger than 0 then market is in play.
	 * @param runnerPrices Key - runnerId, Value - Tuple[runner prices, price traded volume]
	 */
	case class MarketRunners(val inPlayDelay:Int, val runnerPrices:Map[Long,Tuple2[List[RunnerPrice],RunnerTradedVolume]]) {
		override def toString = "MarketRunners [inPlayDelay=%s, runnerPrices=%s]".format(inPlayDelay,runnerPrices)
	}
	
	case class MarketDetails(val marketId:Long,val marketName:String, val menuPath:String,val numOfWinners:Int, val marketTime:java.util.Date, val runners:List[RunnerDetails]) {
		override def toString = "MarketDetails [marketId=%s, marketName=%s, menuPath=%s, numOfWinners=%s, marketTime=%s, runners=%s]".format(marketId,marketName,menuPath,numOfWinners,marketTime,runners)
	}
	case class RunnerDetails(val runnerId:Long, val runnerName:String) {
		override def toString = "RunnerDetails [runnerId=%s, runnerName=%s]".format(runnerId,runnerName)
	}
}

/**Betfair service adapter.
 * 
 * @author KorzekwaD
 *
 */
trait IMarketService {

	/**Returns markets from betfair betting exchange that fulfil the following criteria:
	 * - UK Horse Racing
	 * - Win only markets
	 * - Active markets
	 * - isInPlay
	 * - isBsbMarket.
	 * 
	 * @param marketTimeFrom Filter markets by market time.
	 * @param marketTimeTo Filter markets by market time.
	 * 
	 * @return List of market ids.
	 */
	def getMarkets(marketTimeFrom:Date, marketTimeTo:Date):List[Long]
	
	/**Returns markets from betfair betting exchange that fulfil the following criteria:
	 * - UK Horse Racing
	 * - Win only markets
	 * - Active markets
	 * - isInPlay
	 * - isBsbMarket.
	 * 
	 * @param marketTimeFrom Filter markets by market time.
	 * @param marketTimeTo Filter markets by market time.
	 * @param menuPathFilter Returns those markets only for which market.menuPath.contains(menuPathFilter)
	 * @return List of market ids.
	 */
	def getMarkets(marketTimeFrom:Date, marketTimeTo:Date, menuPathFilter:String):List[Long]
	
	/** Returns runner prices and price traded volumes for market runner.
	 * 
	 * @param marketId
	 * @return marketRunners
	 */
	def getMarketRunners(marketId:Long):MarketRunners
	
	/**Returns runner prices (best to back and best to lay) for all market runners.
	 * 
	 * @param marketId
	 * 
	 * @return market prices
	 * */
	def getMarketPrices(marketId:Long):MarketPrices
	
	/**Returns traded volume for all market runners.
	 * 
	 * @param marketId
	 * 
	 * @return Traded volume for all market runners, key - runnerId, ,value - runner traded volume for all prices.
	 * */
	def getMarketTradedVolume(marketId:Long):Map[Long,IRunnerTradedVolume]
	
	def getMarketDetails(marketId:Long):IMarketService.MarketDetails
	
	def getUserMatchedBets(marketId:Long,matchedSince:Date): List[IBet]
	
	/**Returns matched/unmatched/all user bets for a market id.
	 * 
	 * @param marketId
	 * @param betStatus if None (default) all bets are returned.
	 * @return
	 */
	def getUserBets(marketId: Long, betStatus: Option[BetStatusEnum] = None): List[IBet]
	
	/** Places a bet on a betting exchange market.
	 * 
	 * @param betSize
	 * @param betPrice
	 * @param betType
	 * @param marketId
	 * @param runnerId
	 * 
	 * @return The bet that was placed.
	 */
	def placeBet(betSize:Double, betPrice:Double, betType:BetTypeEnum, marketId:Long,runnerId:Long):IBet 
}