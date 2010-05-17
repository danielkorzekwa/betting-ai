package dk.bettingai.marketsimulator.betex.api

import java.util.Date
import IBet.BetTypeEnum._

/**This trait represents a market on a betting exchange. Market is a place that bets can be placed on, for example football match between Man Utd and Arsenal.
 * 
 * @author korzekwad
 *
 */
object IMarket {
	trait ISelection {
		val selectionId:Long
		val selectionName:String
	}
}
trait IMarket {
	
	val marketId:Long
	val marketName:String
	val eventName:String
	val numOfWinners:Int
	val marketTime:Date
	val selections:List[IMarket.ISelection]
	
	/** Places a bet on a betting exchange market.
	 * 
	* @param betId
	* @param userId
	* @param betSize
	* @param betPrice
	* @param betType
	* @param selectionId
	*/
	def placeBet(betId:Long,userId: Long, betSize:Double, betPrice:Double, betType:BetTypeEnum, selectionId:Long)
	
	/**Returns all bets placed by user on that market.
	 *
	 *@param userId
	 */
	def getBets(userId:Int):List[IBet]
}