package dk.bettingai.marketcollector.marketservice

import java.util.Date
import dk.bot.betfairservice._
import scala.collection.JavaConversions._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.api._
import IBet._
import BetTypeEnum._
import BetStatusEnum._
import Market._
import dk.bot.betfairservice.model._
import IMarketService._
import MarketService._
import dk.bettingai.marketsimulator.betex.RunnerTradedVolume._

/**Betfair service adapter.
 * 
 * @author KorzekwaD
 *
 */
object MarketService {
	class MarketClosedOrSuspendedException(message:String) extends RuntimeException(message)
}

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
	 * @return market runners
	 * @throw 
	 */
	def getMarketRunners(marketId:Long):MarketRunners = {
			/**Get runner prices and runner traded volume*/
			val bfMarketRunners = betfairService.getMarketRunners(marketId.asInstanceOf[Int])

			if(bfMarketRunners!=null) {
				val bfTradedVolume = try {
					betfairService.getMarketTradedVolume(marketId.asInstanceOf[Int])
				}catch {
				case e: BetFairException => throw new MarketClosedOrSuspendedException("Market is probably closed/suspended (but not sure!). MarketId=" + marketId)
				}
				val runnerIds = (bfMarketRunners.getMarketRunners.map(_.getSelectionId).toList ::: bfMarketRunners.getMarketRunners.map(_.getSelectionId).toList).distinct

				val marketRunnersList = for{
					runnerId <- runnerIds
					val bfMarketRunner=bfMarketRunners.getMarketRunners.find(r => r.getSelectionId==runnerId).getOrElse(new BFMarketRunner(runnerId,0,0,0,0,0,List()))
					val runnerPrices = bfMarketRunner.getPrices.map(p => new RunnerPrice(p.getPrice,p.getTotalToBack,p.getTotalToLay)).filter(price => (price.totalToBack>0 || price.totalToLay>0)).toList

					val bfRunnerTradedVolume = bfTradedVolume.getRunnerTradedVolume.find(rtv => rtv.getSelectionId==runnerId).getOrElse(new BFRunnerTradedVolume(runnerId,List()))
					val priceTradedVolume = bfRunnerTradedVolume.getPriceTradedVolume.map(tv => new PriceTradedVolume(tv.getPrice,tv.getTradedVolume))
				} yield (runnerId.asInstanceOf[Long], (runnerPrices,new RunnerTradedVolume(priceTradedVolume.toList)))

				/**key - selectionId, value - runner prices + price traded volume*/
				val marketRunnersMap:Map[Long,Tuple2[List[RunnerPrice],RunnerTradedVolume]] = Map(marketRunnersList : _*)
				val marketRunners = new MarketRunners(bfMarketRunners.getInPlayDelay,marketRunnersMap)
				marketRunners
			}
			else throw new MarketClosedOrSuspendedException("Market is closed/suspended. MarketId=" + marketId)
	}

	def getMarketDetails(marketId:Long):IMarketService.MarketDetails = {
			val bfMarketDetails = betfairService.getMarketDetails(marketId.asInstanceOf[Int])

			val runners = bfMarketDetails.getRunners.map(r => new RunnerDetails(r.getSelectionId,r.getSelectionName)).toList
			val marketDetails = new MarketDetails(bfMarketDetails.getMarketId,bfMarketDetails.getMarketName(), bfMarketDetails.getMenuPath(),bfMarketDetails.getNumOfWinners, bfMarketDetails.getMarketTime, runners)
			marketDetails
	}
	
	def getUserMatchedBets(marketId:Long,matchedSince:Date): List[IBet] = {
		val muBets = betfairService.getMUBets(BFBetStatus.MU,marketId.toInt,matchedSince)
		def betType(b:BFMUBet):BetTypeEnum = b.getBetType match {
			case BFBetType.B => BACK
			case BFBetType.L => LAY
		}
		def betStatus(b:BFMUBet):BetStatusEnum = b.getBetStatus match {
			case BFBetStatus.M => M 
			case BFBetStatus.U => U 
			case _ => throw new IllegalArgumentException("Not supported bet status")
		}
		val bets = muBets.map(b => new Bet(b.getBetId(),-1, b.getSize(), b.getPrice(), betType(b), betStatus(b),b.getMarketId,b.getSelectionId)).toList
		bets
	}
	
	/** Places a bet on a betting exchange market.
	 * 
	 * @param betSize
	 * @param betPrice
	 * @param betType
	 * @param runnerId
	 * @param marketId
	 * 
	 * @return The bet that was placed.
	 */
	def placeBet(betSize:Double, betPrice:Double, betType:BetTypeEnum, marketId:Long, runnerId:Long):IBet = {
		def betTypeValue(b:BetTypeEnum):BFBetType = betType match {
			case BACK => BFBetType.B
			case LAY => BFBetType.L
		}
		val betResult = betfairService.placeBet(marketId.toInt,runnerId.toInt,betTypeValue(betType),betPrice,betSize,true)
		Bet(betResult.getBetId,-1l,betResult.getSize(),betResult.getPrice(),betType,marketId,runnerId)
	}
}