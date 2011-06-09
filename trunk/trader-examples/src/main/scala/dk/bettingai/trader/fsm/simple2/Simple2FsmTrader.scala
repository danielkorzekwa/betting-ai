package dk.bettingai.trader.fsm.simple2

import dk.bettingai.trader._
import scala.collection._
import dk.bettingai.marketsimulator.trader._
import dk.betex.api._
import dk.betex._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import IMarket._
/**
 * Creates a number of state machines, each of them trying to place a bet and then trade it with a profit or a minimal loss.
 *
 * @author korzekwad
 *
 */
case class RunnerTrader(val runnerId: Long) extends ITrader {

  sealed abstract class TraderState { val code: Double }
  case object Init extends TraderState { val code = 1d }
  case class BackPlaced(backBet: IBet) extends TraderState { val code = 2d }
  case class LayPlaced(layBet: IBet) extends TraderState { val code = 3d }
  var state: TraderState = Init

  val betSize = 2d
  def execute(ctx: ITraderContext) {

    val bestPrices = ctx.getBestPrices(runnerId)
    val matchedBets = ctx.getBets(true)

    if (!bestPrices._1.price.isNaN && !bestPrices._2.price.isNaN)
      state match {
        case Init => {
          val bet = ctx.placeBet(betSize, bestPrices._2.price, BACK, runnerId)
          state = BackPlaced(bet)
        }
        case BackPlaced(backBet) => {
          if (ctx.getBet(backBet.betId).exists(b => b.betStatus == M)) {
            val bet = ctx.placeBet(betSize, bestPrices._1.price, LAY, runnerId)
            state = LayPlaced(bet)
          } else if (bestPrices._2.price < backBet.betPrice) {
            try { ctx.cancelBet(backBet.betId) } catch { case _ => {} }
            val bet = ctx.placeBet(betSize, bestPrices._2.price, BACK, runnerId)
            state = BackPlaced(bet)
          }
        }
        case LayPlaced(layBet) => {
          if (ctx.getBet(layBet.betId).exists(b => b.betStatus == M)) {
            state = Init
          } else if (bestPrices._1.price > layBet.betPrice) {
            try { ctx.cancelBet(layBet.betId) } catch { case _ => {} }
            val bet = ctx.placeBet(betSize, bestPrices._1.price, LAY, runnerId)
            state = LayPlaced(bet)
          }
        }
      }
  } //end of execute

}

class Simple2FsmTrader extends ITrader {

  val runnerId = 4207432l

  /**key - runnerId.*/
  var backTraders = mutable.ListBuffer[RunnerTrader]()

  override def init(ctx: ITraderContext) {
    for (i <- 1 to 1) ctx.runners.filter(r => r.runnerId == runnerId).foreach(r => backTraders += RunnerTrader(r.runnerId))
  }

  def execute(ctx: ITraderContext) {
    backTraders.foreach(t => t.execute(ctx))

    //  val bestPrices = ctx.getBestPrices(4207432)
    //  val matchedBets = ctx.getBets(false)
    //  ctx.addChartValue("" + 4207432, bestPrices._1.price)
    //  ctx.addChartValue("risk", ctx.risk(2000).marketExpectedProfit)
    //  ctx.addChartValue("bets", matchedBets.size)
    //  ctx.addChartValue("state", backTraders.filter(trader => trader.runnerId == runnerId).head.state.code)
  }

}