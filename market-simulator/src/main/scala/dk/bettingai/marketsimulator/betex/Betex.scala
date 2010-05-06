package dk.bettingai.marketsimulator.betex


/** This interface represents a betting exchange. It allows to create market, place bet, cancel bet, etc.
 * @author korzekwad
 *
 */
trait Betex {

	/**Creates market on a betting exchange.
	 * 
	 * @param market
	 * 
	 */
	def createMarket(market:Market)
	
	/** Places a bet on a betting exchange.
	 * 
	 * @param userId Unique id of a user that places a bet.
	 * @param bet
	*/
	def placeBet(userId:Int,bet: Bet)
	
	/** Cancels a bet on a betting exchange.
	 * 
	 * @param userId Unique id of a user that cancels a bet.
	 * @param betId Unique id of a bet to be cancelled.
	*/
	def cancelBet(userId: Int, betId:Long)
	
	/**Returns all markets that are not settled.*/
	def getActiveMarkets(): List[Market]
}