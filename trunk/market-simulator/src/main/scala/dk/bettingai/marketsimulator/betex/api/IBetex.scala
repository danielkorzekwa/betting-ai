package dk.bettingai.marketsimulator.betex.api

import java.util.Date

/** This trait represents a betting exchange. It allows to create market, place bet, cancel bet, etc.
 * @author korzekwad
 *
 */
trait IBetex {

	/**Creates market on a betting exchange.
	 * 
	 * @param market
	 * 
	 */
	def createMarket(marketId:Long,marketName:String,eventName:String,numOfWinners:Int,marketTime:Date,selections:List[IMarket.ISelection])
	
	/**Finds market for market id.
	 * 
	 * @param marketId
	 * 
	 * @return Found market is returned or exception is thrown if market not exists.
	 */
	def findMarket(marketId: Long):IMarket
	
	/** Cancels a bet on a betting exchange.
	 * 
	 * @param userId Unique id of a user that cancels a bet.
	 * @param betId Unique id of a bet to be cancelled.
	*/
	def cancelBet(userId: Int, betId:Long)
	
	/**Returns all markets on a betting exchange.*/
	def getMarkets(): List[IMarket]
	
}