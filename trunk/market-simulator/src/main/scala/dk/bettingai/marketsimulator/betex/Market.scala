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
	class Selection(val selectionId:Long, val selectionName:String) extends IMarket.ISelection {
		override def toString = "Selection [selectionId=%s, selectionName=%s]".format(selectionId, selectionName)
	}
	
	class RunnerPrice(	val price:Double,val totalToBack:Double,val totalToLay: Double) extends IMarket.IRunnerPrice {
		override def toString = "RunnerPrice [price=%s, totalToBack=%s, totalToLay=%s".format(price,totalToBack,totalToLay)
	}
}

class Market(val marketId:Long, val marketName:String,val eventName:String,val numOfWinners:Int, val marketTime:Date,val selections:List[IMarket.ISelection]) extends IMarket{

	private val bets = ListBuffer[IBet]()

	require(numOfWinners>0,"numOfWinners should be bigger than 0, numOfWinners=" + numOfWinners)
	require(selections.size>1,"Number of market selections should be bigger than 1, numOfSelections=" + selections.size)

	/** Places a bet on a betting exchange market.
	 * 
	 * @param betId
	 * @param userId
	 * @param betSize
	 * @param betPrice
	 * @param betType
	 * @param selectionId
	 */
	def placeBet(betId:Long,userId: Long, betSize:Double, betPrice:Double, betType:BetTypeEnum, selectionId:Long) {
		require(betSize>=2, "Bet size must be >=2, betSize=" + 2)
		require(selections.exists(s => s.selectionId==selectionId),"Can't place bet on a market. Market selection not found for marketId/selectionId=" + marketId + "/" + selectionId)

		/**This is a spike implementation only, will be improved very soon.*/
		if(betType==LAY) {
			var newBet:IBet = new Bet(betId,userId, betSize, betPrice, betType, U,marketId,selectionId)
		while(newBet.betSize>0) {
			val betsToBeMatched = bets.filter(b => b.betStatus == U && b.betType ==BACK && b.betPrice<= newBet.betPrice).sortWith((a,b) => a.betPrice<b.betPrice)
			if(betsToBeMatched.size>0) {
				val matchingResult = newBet.matchBet(betsToBeMatched(0))
				matchingResult.filter(b => b.betStatus ==M || b.betId!=newBet.betId).foreach(b => bets += b)
				bets -= betsToBeMatched(0)
				newBet = if(matchingResult.filter(b => b.betId==newBet.betId && b.betStatus==U).size>0) matchingResult.filter(b => b.betId==newBet.betId && b.betStatus==U)(0) else new Bet(betId,userId, 0, betPrice, betType, U,marketId,selectionId)
			}
			else {
				bets += newBet
				newBet=new Bet(betId,userId, 0, betPrice, betType, U,marketId,selectionId)
			}
		}
		}
		else if(betType==BACK){
			var newBet:IBet = new Bet(betId,userId, betSize, betPrice, betType, U,marketId,selectionId)
		while(newBet.betSize>0) {
			val betsToBeMatched = bets.filter(b => b.betStatus == U && b.betType ==LAY && b.betPrice>= newBet.betPrice).sortWith((a,b) => a.betPrice>b.betPrice)
			if(betsToBeMatched.size>0) {
				val matchingResult = newBet.matchBet(betsToBeMatched(0))
				matchingResult.filter(b => b.betStatus ==M || b.betId!=newBet.betId).foreach(b => bets += b)
				bets -= betsToBeMatched(0)
				newBet = if(matchingResult.filter(b => b.betId==newBet.betId && b.betStatus==U).size>0) matchingResult.filter(b => b.betId==newBet.betId && b.betStatus==U)(0) else new Bet(betId,userId, 0, betPrice, betType, U,marketId,selectionId)
			}
			else {
				bets += newBet
				newBet=new Bet(betId,userId, 0, betPrice, betType, U,marketId,selectionId)
			}
		}
		}
	}
	
	/** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
	 *  Prices with zero volume are not returned by this method.
   * 
   * @param selectionId Unique runner id that runner prices are returned for.
   * @return
   */
	def getRunnerPrices(selectionId:Long):List[IMarket.IRunnerPrice] = {
		val betsByPriceMap = bets.toList.filter(b => b.betStatus==U && b.selectionId==selectionId).groupBy(b => b.betPrice) 
		
		def totalStake(bets: List[IBet],betType:BetTypeEnum) = bets.filter(b => b.betType==betType).map(b => b.betSize).foldLeft(0d)(_ + _)
		betsByPriceMap.map( entry => new RunnerPrice(entry._1,totalStake(entry._2,LAY),totalStake(entry._2,BACK))).toList
	}

	/**Returns all bets placed by user on that market.
	 *
	 *@param userId
	 */
	def getBets(userId:Int):List[IBet] = bets.filter(b => b.userId == userId).toList

	override def toString = "Market [marketId=%s, marketName=%s, eventName=%s, numOfWinners=%s, marketTime=%s, selections=%s]".format(marketId,marketName,eventName,numOfWinners,marketTime,selections)
}