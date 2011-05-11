package dk.bettingai.trader.fsm.simple

import dk.bettingai.trader.fsm.simple.SimpleFsmTrader._
import dk.bettingai.trader._
import scala.collection._
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import IMarket._
/**
 * Creates a number of state machines, each of them trying to place a bet and then trade it with a profit or a minimal loss.
 * This is a proof of concept for fsm only.
 *
 * @author korzekwad
 *
 */
case class RunnerTrader(val runnerId: Long) extends ITrader {

  sealed abstract class TraderState
  case object Init extends TraderState
  case class BackPlaced(backBetId: Long) extends TraderState
  case class LayPlaced(layBetId: Long) extends TraderState
  var state: TraderState = Init

  val betSize = 2d
  def execute(ctx: ITraderContext) {

    val bestPrices = ctx.getBestPrices(runnerId)
    val matchedBets = ctx.getBets(true)

    if (!bestPrices._1.price.isNaN && !bestPrices._2.price.isNaN)
      state match {
        case Init => {
          val bet = ctx.placeBet(betSize, bestPrices._2.price, BACK, runnerId)
          state = BackPlaced(bet.betId)
        }
        case BackPlaced(backBetId) => {
          if (matchedBets.exists(b => b.betId == backBetId)) {
            val bet = ctx.placeBet(betSize, bestPrices._1.price, LAY, runnerId)
            state = LayPlaced(bet.betId)
          }
        }
        case LayPlaced(layBetId) => {
          if (matchedBets.exists(b => b.betId == layBetId)) {
            state = Init
          }
        }
      }
  } //end of execute

}

class SimpleFsmTrader extends ITrader {

  /**key - runnerId.*/
  var backTraders = mutable.ListBuffer[ITrader]()
  override def init(ctx: ITraderContext) {
    for (i <- 1 to 1) ctx.runners.foreach(r => backTraders += RunnerTrader(r.runnerId))
  }

  def execute(ctx: ITraderContext) {
    backTraders.foreach(t => t.execute(ctx))

  }

}