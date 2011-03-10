package dk.bettingai.marketsimulator.trader.examples

import dk.bettingai.marketsimulator.trader._

import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import SimpleTraderWithChildren._
/** Creates two children that place back bet if priceToBack>2, place lay bet if priceToLay<2.2.
 * 
 * @author korzekwad
 *
 */
object SimpleTraderWithChildren {
  class ChildTrader extends ITrader {

    def execute(ctx: ITraderContext) {
      /**Add data to a time line chart.*/
      val bestPrices = ctx.getBestPrices(ctx.runners.head.runnerId)
      ctx.addChartValue("toBack", bestPrices._1.price)
      ctx.addChartValue("toLay", bestPrices._2.price)

      for (runner <- ctx.runners) {
        val bestPrices = ctx.getBestPrices(runner.runnerId)

        if (bestPrices._1.price > 2.0) {
          ctx.placeBet(2, bestPrices._1.price, BACK, runner.runnerId)
          ctx.placeBet(2, bestPrices._1.price - 0.02, LAY, runner.runnerId)
        }
        if (bestPrices._2.price < 2.2) {
          ctx.placeBet(2, bestPrices._2.price, LAY, runner.runnerId)
          ctx.placeBet(2, bestPrices._2.price + 0.02, BACK, runner.runnerId)
        }
      }
    }
  }
}
class SimpleTraderWithChildren extends ITrader {

  var children: Iterable[Tuple2[ChildTrader, ITraderContext]] = _

  override def init(ctx: ITraderContext) {
    /** Born 10 traders and search for the best solution.*/
    children = for (i <- 0 until 1) yield new ChildTrader() -> ctx.registerTrader()

  }

  def execute(ctx: ITraderContext) = {
    /**Trigger all child traders.*/
    children.foreach { case (trader, context) => trader.execute(context) }
  }

  def getTotalMarketExpectedProfit(): Double = children.map(c => c._2.risk.marketExpectedProfit).sum

}