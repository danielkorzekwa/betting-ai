package dk.bettingai.marketcollector.marketservice

import java.util.Date
import dk.bettingai.marketsimulator.betex._
import Market._

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
}