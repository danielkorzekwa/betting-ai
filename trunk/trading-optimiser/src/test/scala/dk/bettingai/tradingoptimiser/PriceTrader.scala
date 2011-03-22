package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.betex.api._
import IBet.BetTypeEnum._

/** An example of trader, which is used by hill climbing tests.
 * 
 * @author korzekwad
 *
 */
case class PriceTrader(val price: Double) extends ITrader {

  val market1Id = 101655622
  val market1RunnerId = 4207432

  val market2Id = 101655610
  val market2RunnerId = 3364827

  def execute(ctx: ITraderContext) = {

    if (ctx.marketId == market1Id) {
      val bestPrices = ctx.getBestPrices(market1RunnerId)
      if (bestPrices._1.price > price) ctx.fillBet(2, bestPrices._1.price, BACK, market1RunnerId)
    }

    if (ctx.marketId == market2Id) {
      val bestPrices = ctx.getBestPrices(market2RunnerId)
      if (bestPrices._1.price > price) ctx.fillBet(2, bestPrices._1.price, BACK, market2RunnerId)
    }

  }

  override def toString = "PriceTrader [price=%s]".format(price)
}