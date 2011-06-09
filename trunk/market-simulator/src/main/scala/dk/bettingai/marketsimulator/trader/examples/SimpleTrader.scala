package dk.bettingai.marketsimulator.trader.examples

import dk.bettingai.marketsimulator.trader._
import dk.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import  java.util.concurrent.atomic.AtomicInteger
/** Place back bet if priceToBack>2, place lay bet if priceToLay<2.2. I addition to that trading bets are placed.
 * 
 * @author korzekwad
 *
 */
class SimpleTrader extends ITrader{

	var initTimestamp = -1l
	
	var initCalledTimes: AtomicInteger = new AtomicInteger(0)
	var afterCalledTimes: AtomicInteger = new AtomicInteger(0)
	/**It is called once for every analysed market.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
   * */
  override def init(ctx: ITraderContext) {
	initCalledTimes.addAndGet(1)
	initTimestamp = ctx.getEventTimestamp
	}
	
		/**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
	 */
	def execute(ctx: ITraderContext) = {

		/**Add data to a time line chart.*/
		val bestPrices = ctx.getBestPrices(ctx.runners.head.runnerId)
		ctx.addChartValue("toBack",bestPrices._1.price)
		ctx.addChartValue("toLay",bestPrices._2.price)

		for(runner <- ctx.runners) {
			val bestPrices = ctx.getBestPrices(runner.runnerId)

			if(bestPrices._1.price>2.0) {
				ctx.placeBet(2,bestPrices._1.price,BACK,runner.runnerId)
				ctx.placeBet(2,bestPrices._1.price - 0.02,LAY,runner.runnerId)
			}
			if(bestPrices._2.price<2.2) {
				ctx.placeBet(2,bestPrices._2.price,LAY,runner.runnerId)
				ctx.placeBet(2,bestPrices._2.price + 0.02,BACK,runner.runnerId)
			}
		}	

	}
	
	 /**It is called once for every analysed market, after market simulation is finished.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
   * */
  override def after(ctx: ITraderContext) {
  	afterCalledTimes.addAndGet(1)
  }

}