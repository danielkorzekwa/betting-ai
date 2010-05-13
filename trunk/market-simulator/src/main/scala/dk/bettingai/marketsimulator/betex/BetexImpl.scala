package dk.bettingai.marketsimulator.betex

import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import Bet.BetStatusEnum._
import Bet.BetTypeEnum._

/** This class represents a betting exchange. It allows to create market, place bet, cancel bet, etc.
 * @author korzekwad
 *
 */
class BetexImpl extends Betex{

	private val markets = Map[Long,Market]()

	private val bets = ListBuffer[Bet]()
	/**Creates market on a betting exchange.
	 * 
	 * @param market
	 * 
	 */
	def createMarket(market:Market) = {
		require(!markets.contains(market.marketId),"Market already exist for marketId=" + market.marketId)

		markets+= market.marketId -> market
	}

	/** Places a bet on a betting exchange.
	 * 
	 * @param userId Unique id of a user that places a bet.
	 * @param bet
	 */
	def placeBet(betId:Long,userId: Long, betSize:Double, betPrice:Double, betType:Bet.BetTypeEnum.BetTypeEnum, marketId:Long,selectionId:Long) = {
		require(betSize>=2, "Bet size must be >=2, betSize=" + 2)
		require(markets.contains(marketId),"Can't place a bet on a market. Market not found for marketId=" + marketId)
		require(markets.values.exists(m => m.selections.exists(s => s.selectionId==selectionId)),"Can't place a bet on a market. Market selection not found for marketId/selectionId=" + marketId + "/" + selectionId)


		/**This is a spike implementation only, will be improved very soon.*/
		if(betType==LAY) {
			var newBet = new Bet(betId,userId, betSize, betPrice, betType, U,marketId,selectionId)
			var continue=true
			while(newBet.betSize>0 && continue) {
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
			var newBet = new Bet(betId,userId, betSize, betPrice, betType, U,marketId,selectionId)
			var continue=true
			while(newBet.betSize>0 && continue) {
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

	/** Cancels a bet on a betting exchange.
	 * 
	 * @param userId Unique id of a user that cancels a bet.
	 * @param betId Unique id of a bet to be cancelled.
	 */
	def cancelBet(userId: Int, betId:Long) = throw new UnsupportedOperationException("Not implemented")

	/**Returns all markets that are not settled.*/
	def getActiveMarkets():List[Market] = markets.values.toList

	/**Returns all bets place bet user.
	 *
	 *@param userId
	 */
	def getBets(userId:Int):List[Bet] = bets.filter(b => b.userId == userId).toList
}