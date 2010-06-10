package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import ITrader._

/** Place back bet if priceToBack>2, place lay bet if priceToLay<2.2. I addition to that trading bets are placed.
 * 
 * @author korzekwad
 *
 */
class SimpleTrader extends ITrader{

	/**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
	 */
	def execute(ctx: ITraderContext) = {

		for(runner <- ctx.runners) {
			val bestPrices = ctx.getBestPrices(runner.runnerId)

			if(bestPrices._1>2) {
				ctx.placeBet(2,bestPrices._1,BACK,runner.runnerId)
				ctx.placeBet(2,bestPrices._1 - 0.02,LAY,runner.runnerId)
			}
			if(bestPrices._2<2.2) {
				ctx.placeBet(2,bestPrices._2,LAY,runner.runnerId)
				ctx.placeBet(2,bestPrices._2 + 0.02,BACK,runner.runnerId)
			}
		}	

	}
}