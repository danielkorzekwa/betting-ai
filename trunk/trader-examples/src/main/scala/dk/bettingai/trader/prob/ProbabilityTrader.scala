package dk.bettingai.trader.prob

import dk.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import dk.bettingai.marketsimulator.trader._
import dk.betex._

/** Creates time series chart with implied probability for all market runners.
 * 
 * @author korzekwad
 *  
 */
class ProbabilityTrader extends ITrader{

	/**It is called once for every analysed market.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
	 * */
	override def init(ctx: ITraderContext) {

	}

	/**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
	 */

	def execute(ctx: ITraderContext)= {

		for(runnerId <- ctx.runners.map(_.runnerId)) {
			val bestPrices = ctx.getBestPrices(runnerId)
			val avgPrice = PriceUtil.avgPrice(bestPrices._1.price -> bestPrices._2 .price)
			ctx.addChartValue(runnerId.toString, 1/avgPrice)
		}
	}
}