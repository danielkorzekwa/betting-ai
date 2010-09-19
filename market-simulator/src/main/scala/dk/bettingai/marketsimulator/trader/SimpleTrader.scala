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

	/**It is called once on trader initialisation.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
	 * */
	def init(ctx: ITraderContext) {
		ctx.setChartLabels("toBack" :: "toLay" :: Nil)
	}

	/**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param eventTimestamp Time stamp of market event
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
	 */
	def execute(eventTimestamp:Long,ctx: ITraderContext) = {

		/**Add data to a time line chart.*/
		val bestPrices = ctx.getBestPrices(ctx.runners.head.runnerId)
		ctx.addChartValues(eventTimestamp -> (bestPrices._1.price :: bestPrices._2.price :: Nil))

		for(runner <- ctx.runners) {
			val bestPrices = ctx.getBestPrices(runner.runnerId)

			if(bestPrices._1.price>2) {
				ctx.placeBet(2,bestPrices._1.price,BACK,runner.runnerId)
				ctx.placeBet(2,bestPrices._1.price - 0.02,LAY,runner.runnerId)
			}
			if(bestPrices._2.price<2.2) {
				ctx.placeBet(2,bestPrices._2.price,LAY,runner.runnerId)
				ctx.placeBet(2,bestPrices._2.price + 0.02,BACK,runner.runnerId)
			}
		}	

	}
}