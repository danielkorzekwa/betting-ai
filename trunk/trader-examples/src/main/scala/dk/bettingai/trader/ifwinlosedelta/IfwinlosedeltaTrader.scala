package dk.bettingai.trader.ifwinlosedelta

import scala.collection.JavaConversions._
import dk.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import dk.bettingai.marketsimulator.trader._
import dk.betex._

/** This trader places back and lay bets across all market runners based on ifWin-ifWose indicator.
 * 
 * @author korzekwad
 *
 */
object IfwinlosedeltaTrader {
  def apply(backSignal: Double, laySignal: Double): IfwinlosedeltaTrader = {
    val trader = new IfwinlosedeltaTrader()
    trader.backSignal = backSignal
    trader.laySignal = laySignal
    trader
  }
}
class IfwinlosedeltaTrader extends ITrader {

  val bank=1000d
	
  var backSignal = 0d
  var laySignal = 0d
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

  def execute(ctx: ITraderContext) = {

    val risk = ctx.risk(bank)
    for (runnerId <- ctx.runners.map(_.runnerId)) {
      val bestPrices = ctx.getBestPrices(runnerId)

      val deltaValue = risk.ifWin(runnerId) - risk.ifLose(runnerId)
      if (deltaValue <= backSignal && !bestPrices._2.price.isNaN) ctx.fillBet(3, bestPrices._2.price, BACK, runnerId)
      else if (deltaValue > laySignal && !bestPrices._1.price.isNaN) ctx.fillBet(3, bestPrices._1.price, LAY, runnerId)
    }

    ctx.addChartValue("ExpectedProfit", risk.marketExpectedProfit)
  }

  override def toString = "IfwinlosedeltaTrader [backSignal=%s, laySignal=%s]".format(backSignal, laySignal)
}