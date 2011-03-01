package dk.bettingai.trader.wealthtest

import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import dk.bettingai.marketsimulator.trader._
import ITrader._
import dk.bettingai.marketsimulator.betex._

/**Compares wealth and market expected profit for a simple trading strategy.
 * 
 * @author korzekwad
 *
 */
class WealthTestTrader extends ITrader {

  /**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
   */

  def execute(ctx: ITraderContext) = {

    for (runnerId <- ctx.runners.map(_.runnerId)) {
      val bestPrices = ctx.getBestPrices(runnerId)
      if (bestPrices._1.price < 4) ctx.placeBet(2, bestPrices._1.price, LAY, runnerId)
    }
   
    ctx.addChartValue("expectedProfit", ctx.risk.marketExpectedProfit)
    ctx.addChartValue("wealth", ctx.wealth(1000).marketExpectedProfit)

  }
}