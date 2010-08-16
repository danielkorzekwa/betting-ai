package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import java.util.Date
import ITrader._

/**This trait represents a trader that can place bets on a betting exchange.
 * 
 * @author korzekwad
 *
 */
object ITrader {
	
	/**Provides market data and market operations that can be used by trader to place bets on a betting exchange market.*/
	trait ITraderContext {
		
	val marketId:Long
	val marketName:String
	val eventName:String
	val numOfWinners:Int
	val marketTime:Date
	val runners:List[IRunner]
	
	/**Returns best toBack/toLay prices for market runner.
	* Element 1 - best price to back, element 2 - best price to lay
	* Double.NaN is returned if price is not available.
	* @return 
	* */
	def getBestPrices(runnerId: Long): Tuple2[Double,Double]
	
	/**Returns best toBack/toLay prices for market.
	 * 
	 * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
	 */
	def getBestPrices():Map[Long,Tuple2[Double,Double]]
	
	/** Places a bet on a betting exchange market.
	* 
	* @param betSize
	* @param betPrice
	* @param betType
	* @param runnerId
	*/
	def placeBet(betSize:Double, betPrice:Double, betType:BetTypeEnum, runnerId:Long)
	
	/**Returns all bets placed by trader on the market.
	 *
	 *@param userId
	 */
	def getBets():List[IBet]
	}
}
trait ITrader {

	/**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
	 */
	def execute(ctx: ITraderContext)
}