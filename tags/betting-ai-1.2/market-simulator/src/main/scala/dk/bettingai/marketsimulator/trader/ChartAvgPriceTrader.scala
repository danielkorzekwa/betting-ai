package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import ITrader._
import dk.bettingai.marketsimulator.betex._

/** Creates tie series chart with average price for all market runners.
 * 
 * @author korzekwad
 *
 */
class ChartAvgPriceTrader extends ITrader{


	/**It is called once on trader initialisation.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
	 * */
	def init(ctx: ITraderContext) {
		
	}

	/**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
	 */
	def execute(ctx: ITraderContext)= {

		for(runnerId <- ctx.runners.map(_.runnerId)) {
			val bestPrices = ctx.getBestPrices(runnerId)	
			val chartValue = 1/PriceUtil.avgPrice(bestPrices._1.price -> bestPrices._2 .price)
			ctx.addChartValue(runnerId.toString,chartValue)
		}
	}
}