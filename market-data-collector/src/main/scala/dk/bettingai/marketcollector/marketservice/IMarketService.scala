package dk.bettingai.marketcollector.marketservice

import java.util.Date
import dk.bettingai.marketsimulator.betex._
import Market._

object IMarketService {
	
	class MarketDetails(val marketId:Long,val marketName:String, val menuPath:String,val numOfWinners:Int, val marketTime:java.util.Date, val runners:List[RunnerDetails]) {
		override def toString = "MarketDetails [marketId=%s, marketName=%s, menuPath=%s, numOfWinners=%s, marketTime=%s, runners=%s]".format(marketId,marketName,menuPath,numOfWinners,marketTime,runners)
	}
	class RunnerDetails(val runnerId:Long, val runnerName:String) {
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
	
	/** Returns runner prices and price traded volumes for market runner.
	 * 
	 * @param marketId
	 * @return key - runnerId, value Tuple[runner prices, price traded volume]
	 */
	def getMarketRunners(marketId:Long):Map[Long,Tuple2[List[RunnerPrice],List[PriceTradedVolume]]]
	
	def getMarketDetails(marketId:Long):IMarketService.MarketDetails
}