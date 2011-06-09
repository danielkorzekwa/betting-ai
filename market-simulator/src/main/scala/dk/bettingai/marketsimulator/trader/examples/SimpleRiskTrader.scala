package dk.bettingai.marketsimulator.trader.examples

import dk.bettingai.marketsimulator.trader._
import dk.betex.api._
import IMarket._
import IBet.BetTypeEnum._

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

		
		val expectedProfit = ctx.risk(1000)
		
		for(runner <- ctx.runners) {

			val bestPrices = ctx.getBestPrices(runner.runnerId)

			if(!bestPrices._2.price.isNaN && bestPrices._2.price < 50 && expectedProfit.marketExpectedProfit > -40 && expectedProfit.wealth  > -30) {
				ctx.placeBet(2,1/((1/bestPrices._2.price)-0.01),BACK,runner.runnerId)
			}

		}	

	}
	
}