package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import ITrader._

/** Trader that does nothing.
 * 
 * @author korzekwad
 *
 */
class NopTrader extends ITrader{

	/**It is called once on trader initialisation.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
	 * */
	def init(ctx: ITraderContext) {
		//do nothing
	}
	
	/**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param eventTimestamp Time stamp of market event
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
	 */
	def execute(eventTimestamp:Long,ctx: ITraderContext) = {/**Do nothing.*/}
}