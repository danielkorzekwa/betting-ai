package dk.bettingai.trader.fsm.simple

import dk.bettingai.marketsimulator.trader.ITrader
import dk.bettingai.marketsimulator.trader._

/**Creates a number of state machines, each of them trying to place a bet and then trade it with a profit or a minimal loss. 
 * This is a proof of concept for fsm only.
 * 
 * @author korzekwad
 * 
 */
class SimpleFsmTrader extends ITrader {

	def execute(ctx:ITraderContext) {
		println(ctx.marketId)
	}
	
}