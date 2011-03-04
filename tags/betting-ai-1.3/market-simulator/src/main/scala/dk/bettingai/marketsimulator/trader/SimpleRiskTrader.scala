package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import ITrader._

/** Places bets based on expected profit and wealth.
 * 
 * @author korzekwad
 *
 */
class SimpleRiskTrader extends ITrader{
	var i=0

	/**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
	 */
	def execute(ctx: ITraderContext) = {

		
		val expectedProfit = ctx.risk
		val wealth = ctx.wealth(1000)
		
		for(runner <- ctx.runners) {

			val bestPrices = ctx.getBestPrices(runner.runnerId)

			if(!bestPrices._2.price.isNaN && bestPrices._2.price < 50 && expectedProfit.marketExpectedProfit > -40 && wealth.marketExpectedProfit  > -30) {
				ctx.placeBet(2,1/((1/bestPrices._2.price)-0.01),BACK,runner.runnerId)
			}

		}	

	}
	
}