package dk.bettingai.marketcollector.marketservice

import java.util.Date
import dk.bot.betfairservice._
import scala.collection.JavaConversions._
import dk.bettingai.marketsimulator.betex._
import Market._
import dk.bot.betfairservice.model._
import IMarketService._

/**Betfair service adapter.
 * 
 * @author KorzekwaD
 *
 */
class MarketService(betfairService: BetFairService) extends IMarketService {

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
	def getMarkets(marketTimeFrom:Date, marketTimeTo:Date):List[Long] = {
			/**7 - HorceRacing markets*/
			val eventTypeIds:java.util.Set[Integer] = Set(new Integer(7))
			val markets = betfairService.getMarkets(marketTimeFrom,marketTimeTo,eventTypeIds)

			val filteredMarkets = markets.filter(m => m.getMarketStatus=="ACTIVE" && m.getEventHierarchy.startsWith("/7/298251/") && m.isTurningInPlay && m.isBsbMarket && m.getNumberOfWinners==1)
			filteredMarkets.map(_.getMarketId.asInstanceOf[Long]).toList
	}

	/** Returns runner prices and price traded volumes for market runner.
	 * 
	 * @param marketId
	 * @return key - runnerId, value Tuple[runner prices, price traded volume]
	 */
	def getMarketRunners(marketId:Long):Map[Long,Tuple2[List[RunnerPrice],List[PriceTradedVolume]]] = {
			/**Get runner prices and runner traded volume*/
			val bfMarketRunners = betfairService.getMarketRunners(marketId.asInstanceOf[Int])

			if(bfMarketRunners!=null && bfMarketRunners.getInPlayDelay==0) {
				val bfTradedVolume = betfairService.getMarketTradedVolume(marketId.asInstanceOf[Int])

				val runnerIds = (bfMarketRunners.getMarketRunners.map(_.getSelectionId).toList ::: bfMarketRunners.getMarketRunners.map(_.getSelectionId).toList).distinct

				val marketRunners = for{
					runnerId <- runnerIds
					val bfMarketRunner=bfMarketRunners.getMarketRunners.find(r => r.getSelectionId==runnerId).getOrElse(new BFMarketRunner(runnerId,0,0,0,0,0,List()))
					val runnerPrices = bfMarketRunner.getPrices.map(p => new RunnerPrice(p.getPrice,p.getTotalToBack,p.getTotalToLay)).filter(price => (price.totalToBack>0 || price.totalToLay>0)).toList

					val bfRunnerTradedVolume = bfTradedVolume.getRunnerTradedVolume.find(rtv => rtv.getSelectionId==runnerId).getOrElse(new BFRunnerTradedVolume(runnerId,List()))
					val priceTradedVolume = bfRunnerTradedVolume.getPriceTradedVolume.map(tv => new PriceTradedVolume(tv.getPrice,tv.getTradedVolume))
				} yield (runnerId.asInstanceOf[Long], (runnerPrices,priceTradedVolume.toList))

				/**key - selectionId, value - runner prices + price traded volume*/
				val marketRunnersMap:Map[Long,Tuple2[List[RunnerPrice],List[PriceTradedVolume]]] = Map(marketRunners : _*)
				marketRunnersMap
			}
			else throw new IllegalStateException("Market is closed/suspended/inplay. MarketId=" + marketId)
	}
	
	def getMarketDetails(marketId:Long):IMarketService.MarketDetails = {
		val bfMarketDetails = betfairService.getMarketDetails(marketId.asInstanceOf[Int])
		
		val runners = bfMarketDetails.getRunners.map(r => new RunnerDetails(r.getSelectionId,r.getSelectionName)).toList
		val marketDetails = new MarketDetails(bfMarketDetails.getMarketId,bfMarketDetails.getMarketName(), bfMarketDetails.getMenuPath(),bfMarketDetails.getNumOfWinners, bfMarketDetails.getMarketTime, runners)
		marketDetails
	}
}