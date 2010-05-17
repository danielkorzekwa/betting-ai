package dk.bettingai.marketsimulator.betex

import dk.bettingai.marketsimulator.betex.api._
import scala.collection.mutable.Map
import java.util.Date

/** This class represents a betting exchange. It allows to create market, place bet, cancel bet, etc.
 * @author korzekwad
 *
 */
class Betex extends IBetex{

	private val markets = Map[Long,IMarket]()

	/**Creates market on a betting exchange.
	 * 
	 * @param market
	 * 
	 */
	def createMarket(marketId:Long,marketName:String,eventName:String,numOfWinners:Int,marketTime:Date,selections:List[IMarket.ISelection]) = {
		require(!markets.contains(marketId),"Market already exist for marketId=" + marketId)

		markets+= marketId -> new Market(marketId,marketName,eventName,numOfWinners,marketTime,selections)
	}
	
	/**Finds market for market id.
	 * 
	 * @param marketId
	 * 
	 * @return Found market is returned or exception is thrown if market not exists.
	 */
	def findMarket(marketId: Long):IMarket = markets(marketId)

	/** Cancels a bet on a betting exchange.
	 * 
	 * @param userId Unique id of a user that cancels a bet.
	 * @param betId Unique id of a bet to be cancelled.
	 */
	def cancelBet(userId: Int, betId:Long) = throw new UnsupportedOperationException("Not implemented")

/**Returns all markets.*/
	def getMarkets(): List[IMarket] = markets.values.toList

}