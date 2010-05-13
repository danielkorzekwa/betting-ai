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
	* @param bet
	*/
	def placeBet(betId:Long,userId: Long, betSize:Double, betPrice:Double, betType:Bet.BetTypeEnum.BetTypeEnum, marketId:Long,selectionId:Long)
	
	/** Cancels a bet on a betting exchange.
	 * 
	 * @param userId Unique id of a user that cancels a bet.
	 * @param betId Unique id of a bet to be cancelled.
	*/
	def cancelBet(userId: Int, betId:Long)
	
	/**Returns all markets that are not settled.*/
	def getActiveMarkets(): List[Market]
	
	/**Returns all bets place bet user.
	 *
	 *@param userId
	 */
	def getBets(userId:Int):List[Bet]
}