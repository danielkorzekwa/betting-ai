package dk.bettingai.trader.fsm.simple3

import dk.bettingai.trader._
import scala.collection._
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex.PriceUtil._
import dk.bettingai.marketsimulator.betex._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import IMarket._
/**
 * Creates a number of state machines, each of them trying to place a bet and then trade it with a profit or a minimal loss.
 *
 * @author korzekwad
 *
 */
case class RunnerTrader(val runnerId: Long) {

  sealed abstract class TraderState { val code: Double }
  case object Init extends TraderState { val code = 1d }
  case class BackPlaced(backBet: IBet) extends TraderState { val code = 2d }
  case class LayPlaced(layBet: IBet,backBet:IBet) extends TraderState { val code = 3d }
  var state: TraderState = Init

  val betSize = 2d
  def execute(ctx: ITraderContext, tvDelta: Double) {

    val bestPrices = ctx.getBestPrices(runnerId)
    val matchedBets = ctx.getBets(true)

    if (!bestPrices._1.price.isNaN && !bestPrices._2.price.isNaN)
      state match {
        case Init => {

          if (tvDelta > 1) {
            val bet = ctx.placeBet(betSize, bestPrices._2.price, BACK, runnerId)
            state = BackPlaced(bet)
          }
        }
        case BackPlaced(backBet) => {
          if (ctx.getBet(backBet.betId).exists(b => b.betStatus == M)) {
            val bet = ctx.placeBet(betSize, move(bestPrices._1.price,-1), LAY, runnerId)
            state = LayPlaced(bet,backBet)
          } else if (bestPrices._2.price < backBet.betPrice) {
            try { ctx.cancelBet(backBet.betId) } catch { case _ => {} }
            val bet = ctx.placeBet(betSize, bestPrices._2.price, BACK, runnerId)
            state = BackPlaced(bet)
          }
        }
        case LayPlaced(layBet,backBet) => {
          if (ctx.getBet(layBet.betId).exists(b => b.betStatus == M)) {
            state = Init
          } else if (move(bestPrices._1.price,-1) > layBet.betPrice) {
            try { ctx.cancelBet(layBet.betId) } catch { case _ => {} }
            val bet = ctx.placeBet(betSize, bestPrices._1.price, LAY, runnerId)
            state = LayPlaced(bet,backBet)
          }
        }
      }
  } //end of execute

}

class Simple3FsmTrader extends ITrader {

  val runnerId = 4207432l

  /**key - runnerId.*/
  var backTraders = mutable.ListBuffer[RunnerTrader]()

  var backBets = mutable.ListBuffer[IBet]()
  var layBets = mutable.ListBuffer[IBet]()

  override def init(ctx: ITraderContext) {

    def listener(bet: IBet): Unit = {
      val matchedDelay = bet.matchedDate.get - bet.placedDate
      if (matchedDelay == 0) {

        bet.betType match {
          case BACK => backBets += bet
          case LAY => layBets += bet
        }
      }

    }
    ctx.addMatchedBetsListener(bet => true, listener)

    for (i <- 1 to 1) ctx.runners.filter(r => r.runnerId == runnerId).foreach(r => backTraders += RunnerTrader(r.runnerId))
  }

  def execute(ctx: ITraderContext) {

    for (trader <- backTraders) {
      val back = backBets.filter(b => b.runnerId == trader.runnerId && ctx.getEventTimestamp - b.matchedDate.get < 60000).foldLeft(0d)((sum, bet) => sum + bet.betSize)
      val lay = layBets.filter(b => b.runnerId == trader.runnerId && ctx.getEventTimestamp - b.matchedDate.get < 60000).foldLeft(0d)((sum, bet) => sum + bet.betSize)
      val tvDelta = (back - lay) / 1000
      trader.execute(ctx, tvDelta)

      if (back < 400000) {
        ctx.addChartValue("delta", tvDelta)
        ctx.addChartValue("price", ctx.getBestPrices(runnerId)._1.price)
      }
    }

  }

}