package dk.bettingai.marketsimulator.trader.examples

import dk.bettingai.marketsimulator.trader._
import dk.betex.api._

/** Trader that does nothing.
 * 
 * @author korzekwad
 *
 */
class NopTrader extends ITrader{
	
	/**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
	 */
	def execute(ctx: ITraderContext) = {/**Do nothing.*/}
	
}