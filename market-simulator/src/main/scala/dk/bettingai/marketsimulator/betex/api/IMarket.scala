package dk.bettingai.marketsimulator.betex.api

import java.util.Date
import IBet.BetTypeEnum._
import IMarket._
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
	
	/**This trait represents total unmatched volume to back and to lay at a given price.*/ 
	trait IRunnerPrice {
		val price:Double
		val totalToBack:Double
		val totalToLay: Double
	}
	
	/**This trait represents total amount matched for the given odds.*/
	trait IPriceTradedVolume {
		val price: Double
		val totalMatchedAmount:Double
	}
}
trait IMarket {
	
	val marketId:Long
	val marketName:String
	val eventName:String
	val numOfWinners:Int
	val marketTime:Date
	val selections:List[ISelection]
	
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
	
	/** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
	 *  Prices with zero volume are not returned by this method.
   * 
   * @param runnerId Unique runner id that runner prices are returned for.
   * @return
   */
	def getRunnerPrices(selectionId:Long):List[IRunnerPrice]
	
	/**Returns total traded volume for all prices on all runners in a market.*/
	def getRunnerTradedVolume(selectionId:Long): List[IPriceTradedVolume]
	
	/**Returns all bets placed by user on that market.
	 *
	 *@param userId
	 */
	def getBets(userId:Int):List[IBet]
}