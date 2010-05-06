package dk.bettingai.marketsimulator.betex

import scala.collection.mutable.Map

/** This class represents a betting exchange. It allows to create market, place bet, cancel bet, etc.
 * @author korzekwad
 *
 */
class BetexImpl extends Betex{

	private val markets = Map[Long,Market]()
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
	def placeBet(userId:Int,bet: Bet) = throw new UnsupportedOperationException("Not implemented")

	/** Cancels a bet on a betting exchange.
	 * 
	 * @param userId Unique id of a user that cancels a bet.
	 * @param betId Unique id of a bet to be cancelled.
	 */
	def cancelBet(userId: Int, betId:Long) = throw new UnsupportedOperationException("Not implemented")

	/**Returns all markets that are not settled.*/
	def getActiveMarkets():List[Market] = {markets.values.toList}
}