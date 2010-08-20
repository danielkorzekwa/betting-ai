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
	def createMarket(marketId:Long,marketName:String,eventName:String,numOfWinners:Int,marketTime:Date,runners:List[IMarket.IRunner]) = {
		require(!markets.contains(marketId),"Market already exist for marketId=" + marketId)

		markets+= marketId -> new Market(marketId,marketName,eventName,numOfWinners,marketTime,runners)
	}
	
	/**Finds market for market id.
	 * 
	 * @param marketId
	 * 
	 * @return Found market is returned or exception is thrown if market not exists.
	 */
	def findMarket(marketId: Long):IMarket = markets(marketId)

/**Returns all markets.*/
	def getMarkets(): List[IMarket] = markets.values.toList

}