package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import java.util.Date
import ITrader._
import dk.bettingai.marketsimulator.risk._

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
		
	/**Set labels for all chart series.*/
	def setChartLabels(chartLabels:List[String])
			
	/**Add chart values to time line chart. Key - time stamp, value - list of values for all series in the same order as labels.*/
	def addChartValues(chartValues:Tuple2[Long,List[Double]])
	
	/**Returns best toBack/toLay prices for market runner.
	* Element 1 - best price to back, element 2 - best price to lay
	* Double.NaN is returned if price is not available.
	* @return 
	* */
	def getBestPrices(runnerId: Long): Tuple2[IRunnerPrice,IRunnerPrice]
	
	/**Returns best toBack/toLay prices for market.
	 * 
	 * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
	 */
	def getBestPrices():Map[Long,Tuple2[IRunnerPrice,IRunnerPrice]]
	
	/** Places a bet on a betting exchange market.
	* 
	* @param betSize
	* @param betPrice
	* @param betType
	* @param runnerId
	* 
  * @return The bet that was placed.
	*/
	def placeBet(betSize:Double, betPrice:Double, betType:BetTypeEnum, runnerId:Long):IBet
	
	/** Places a bet on a betting exchange market.
	 * 
	 * @param betSizeLimit Total user unmatched volume that should be achieved after calling this method. 
	 * For example is unmatched volume is 2 and betSizeLimit is 5 then bet with bet size 3 is placed. 
	 * @param betPrice
	 * @param betType
	 * @param runnerId
	 * 
	 * @return The bet that was placed or None if nothing has been placed.
	 */
	def fillBet(betSizeLimit:Double, betPrice:Double, betType:BetTypeEnum, runnerId:Long):Option[IBet]
	
	/** Cancels a bet on a betting exchange market.
	 * 
	 * @param betId Unique id of a bet to be cancelled.
	 * 
	 * @return amount cancelled
	*/
	def cancelBet(betId:Long):Double
	
	/**Place hedge bet on a market runner to make ifwin/iflose profits even. Either back or lay bet is placed on best available price.
	 * 
	 * @param runnerId
	 * 
	 * @return Hedge bet that was placed or none if no hedge bet was placed.
	 */
	def placeHedgeBet(runnerId:Long):Option[IBet]
	
	/**Returns all bets placed by user on that market.
	 *
	 *@param matchedBetsOnly If true then matched bets are returned only, 
	 * otherwise all unmatched and matched bets for user are returned.
	 */
	def getBets(matchedBetsOnly:Boolean):List[IBet]
	
	/** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
	 *  Prices with zero volume are not returned by this method.
   * 
   * @param runnerId Unique runner id that runner prices are returned for.
   * @return
   */
	def getRunnerPrices(runnerId:Long):List[IRunnerPrice]
	
	/**Returns total traded volume for all prices on all runners in a market.*/
	def getRunnerTradedVolume(runnerId:Long): IRunnerTradedVolume
	
	def risk():MarketExpectedProfit
	}
}
trait ITrader {

	/**It is called once on trader initialisation.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
	 * */
	def init(ctx: ITraderContext)
	
	/**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param eventTimestamp Time stamp of market event
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
	 */
	def execute(eventTimestamp:Long,ctx: ITraderContext)
	

}