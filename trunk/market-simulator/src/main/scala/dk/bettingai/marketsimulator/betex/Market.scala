package dk.bettingai.marketsimulator.betex

import java.util.Date
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex.api.IBet._
import scala.collection.mutable.ListBuffer
import IBet.BetStatusEnum._
import IBet.BetTypeEnum._
import Market._

/**This class represents a market on a betting exchange.
 * @author korzekwad
 *
 */
object Market {
	class Runner(val runnerId:Long, val runnerName:String) extends IMarket.IRunner {
		override def toString = "Runner [runnerId=%s, runnerName=%s]".format(runnerId, runnerName)
	}

	class RunnerPrice(val price:Double,val totalToBack:Double,val totalToLay: Double) extends IMarket.IRunnerPrice {
		override def toString = "RunnerPrice [price=%s, totalToBack=%s, totalToLay=%s".format(price,totalToBack,totalToLay)
	}

	class PriceTradedVolume(val price:Double, val totalMatchedAmount:Double) extends IMarket.IPriceTradedVolume {

		override def toString = "PriceTradedVolume [price=%s, totalMatchedAmount=%s]".format(price,totalMatchedAmount)
	}
}

class Market(val marketId:Long, val marketName:String,val eventName:String,val numOfWinners:Int, val marketTime:Date,val runners:List[IMarket.IRunner]) extends IMarket{

	private val bets = ListBuffer[IBet]()

	require(numOfWinners>0,"numOfWinners should be bigger than 0, numOfWinners=" + numOfWinners)
	require(runners.size>1,"Number of market runners should be bigger than 1, numOfRunners=" + runners.size)


	/** Places a bet on a betting exchange market.
	 * 
	 * @param betId
	 * @param userId
	 * @param betSize
	 * @param betPrice
	 * @param betType
	 * @param runnerId
	 */
	def placeBet(betId:Long,userId: Long, betSize:Double, betPrice:Double, betType:BetTypeEnum, runnerId:Long) {
		require(betSize>=2, "Bet size must be >=2, betSize=" + 2)
		require(runners.exists(s => s.runnerId==runnerId),"Can't place bet on a market. Market runner not found for marketId/runnerId=" + marketId + "/" + runnerId)

		val betsToBeMatched =  
			betType match {
			case LAY => bets.filter(b => b.runnerId==runnerId && b.betStatus == U && b.betType ==BACK && b.betPrice<= betPrice).sortWith((a,b) => a.betPrice<b.betPrice).toList
			case BACK => 	bets.filter(b => b.runnerId==runnerId && b.betStatus == U && b.betType ==LAY && b.betPrice>= betPrice).sortWith((a,b) => a.betPrice>b.betPrice).toList
		}
		
		matchBet(betSize,0)

		/**Match bet recursively until fully matched or nothing is to match with.*/
		def matchBet(unmatchedSize:Double, betToMatchIndex:Int):Unit = {
      val newBet = new Bet(betId,userId, unmatchedSize, betPrice, betType, U,marketId,runnerId)
			/**Nothing to match with.*/
			if(betToMatchIndex>=betsToBeMatched.size) {
				bets += newBet
			}
			/**Do matching.*/
			else if(betToMatchIndex<betsToBeMatched.size) {
				/**Get bet to be matched and remove it from the main list of bets - it will be added later as a result of matching.*/
				val betToBeMatched = betsToBeMatched(betToMatchIndex)
				bets -= betToBeMatched

				/**Do the bets matching.*/
				val matchingResult = newBet.matchBet(betToBeMatched)
				/**Add all matched portions of bets to the main bets list.*/
				matchingResult.filter(b => b.betStatus ==M || b.betId!=betId).foreach(b => bets += b)

				/**Find unmatched portion for a bet being placed.*/
				matchingResult.find(b => b.betId==betId && b.betStatus==U).foreach(unmatchedPortion => matchBet(unmatchedPortion.betSize,betToMatchIndex+1))
			}
		}
	}

	/** Cancels a bet on a betting exchange market.
	 *
	 * @param betId Unique id of a bet to be cancelled.
	 * 
	 * @return amount cancelled
	 * @throws NoSuchElementException is thrown if no unmatched bet for betId/userId found.
	 */
	def cancelBet(betId:Long):Double = {
			val betToBeCancelled = bets.find(b => b.betId==betId && b.betStatus==U).get
			bets -=  betToBeCancelled
			betToBeCancelled.betSize
	}

	/** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
	 *  Prices with zero volume are not returned by this method.
	 * 
	 * @param runnerId Unique runner id that runner prices are returned for.
	 * @return
	 */
	def getRunnerPrices(runnerId:Long):List[IMarket.IRunnerPrice] = {
			require(runners.exists(s => s.runnerId==runnerId),"Market runner not found for marketId/runnerId=" + marketId + "/" + runnerId)

			val betsByPriceMap = bets.toList.filter(b => b.betStatus==U && b.runnerId==runnerId).groupBy(b => b.betPrice) 

			def totalStake(bets: List[IBet],betType:BetTypeEnum) = bets.filter(b => b.betType==betType).map(b => b.betSize).foldLeft(0d)(_ + _)
			betsByPriceMap.map( entry => new RunnerPrice(entry._1,totalStake(entry._2,LAY),totalStake(entry._2,BACK))).toList
	}

	/**Returns total traded volume for all prices on all runners in a market.*/
	def getRunnerTradedVolume(runnerId:Long): List[IMarket.IPriceTradedVolume] = {
			require(runners.exists(s => s.runnerId==runnerId),"Market runner not found for marketId/runnerId=" + marketId + "/" + runnerId)

			val betsByPrice = bets.toList.filter(b => b.betStatus==M && b.betType==BACK && b.runnerId==runnerId).groupBy(b => b.betPrice)

			/**Map betsByPrice to list of PriceTradedVolume.*/
			betsByPrice.map( entry => new PriceTradedVolume(entry._1,entry._2.map(b =>b.betSize).foldLeft(0d)(_ + _))).toList
	}

	/**Returns all bets placed by user on that market.
	 *
	 *@param userId
	 */
	def getBets(userId:Int):List[IBet] = bets.filter(b => b.userId == userId).toList

	override def toString = "Market [marketId=%s, marketName=%s, eventName=%s, numOfWinners=%s, marketTime=%s, runners=%s]".format(marketId,marketName,eventName,numOfWinners,marketTime,runners)
}