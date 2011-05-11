package dk.bettingai.trader.fsm.simpledelayed

import dk.bettingai.trader.fsm.simpledelayed.SimpleDelayedFsmTrader._
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
object SimpleDelayedFsmTrader {

  private case class RunnerTrader(val runnerId: Long) extends ITrader {

    sealed abstract class TraderState
    case class BackPlaced(backBetId: Long) extends TraderState
    case class LayPlaced(layBetId: Long) extends TraderState

    var state: Option[TraderState] = None

    var recentBestPrices: Option[Tuple2[IRunnerPrice, IRunnerPrice]] = None
    var recentMatchedBets: Option[List[IBet]] = None

    def execute(ctx: ITraderContext) {

      if (recentBestPrices.isDefined && !recentBestPrices.get._1.price.isNaN && !recentBestPrices.get._2.price.isNaN) {

        val bestPrices = recentBestPrices.get
        val matchedBets = recentMatchedBets.get

        state match {
          case None => {
            val bet = ctx.placeBet(2, bestPrices._2.price, BACK, runnerId)
            state = Option(BackPlaced(bet.betId))
          }
          case Some(BackPlaced(backBetId)) => {
            if (matchedBets.exists(b => b.betId == backBetId)) {
              val bet = ctx.placeBet(2, bestPrices._1.price, LAY, runnerId)
              state = Option(LayPlaced(bet.betId))
            }
          }
          case Some(LayPlaced(layBetId)) => {
            if (matchedBets.exists(b => b.betId == layBetId)) {
              state = None
            }
          }
        }
      }
      recentBestPrices = Option(ctx.getBestPrices(runnerId))
      recentMatchedBets = Option(ctx.getBets(true))
    }
  }
}

class SimpleDelayedFsmTrader extends ITrader {

  /**key - runnerId.*/
  var backTraders = mutable.Map[Long, ITrader]()

  override def init(ctx: ITraderContext) {
    ctx.runners.foreach(r => backTraders += r.runnerId -> RunnerTrader(r.runnerId))
  }

  def execute(ctx: ITraderContext) {
    backTraders.values.foreach(t => t.execute(ctx))
  }

}