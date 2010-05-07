package dk.bettingai.marketsimulator.betex

import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer

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
	def placeBet(bet: Bet) = {
		require(markets.contains(bet.marketId),"Can't place a bet on a market. Market not found for marketId=" + bet.marketId)
		require(markets.values.exists(m => m.selections.exists(s => s.selectionId==bet.selectionId)),"Can't place a bet on a market. Market selection not found for marketId/selectionId=" + bet.marketId + "/" + bet.selectionId)
		
		bets += bet
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