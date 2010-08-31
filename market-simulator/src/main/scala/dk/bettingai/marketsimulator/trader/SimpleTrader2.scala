package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import ITrader._

/** Places bets on bestPrices - 0.01
 * 
 * @author korzekwad
 *
 */
class SimpleTrader2 extends ITrader{
	var i=0
	
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
	def execute(eventTimestamp:Long,ctx: ITraderContext) = {

		for(runner <- ctx.runners) {
			
			
				val bestPrices = ctx.getBestPrices(runner.runnerId)

			if(bestPrices._1<50 && !bestPrices._1.isNaN && !bestPrices._2.isNaN) {
				ctx.placeBet(2,1/((1/bestPrices._2)-0.01),BACK,runner.runnerId)
			}
				
		}	

	}
}