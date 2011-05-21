package dk.bettingai.trader.stat.s2

import dk.bettingai.trader._
import scala.collection._
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import IMarket._
import scala.collection._
/**
 * Compares traded volume with price.
 *
 * @author korzekwad
 *
 */

class S2Trader extends ITrader {

  val runnerId = 4207432l
  // val runnerId = 3954418

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
    ctx.addMatchedBetsListener(bet => bet.runnerId == runnerId, listener)
  }

  def execute(ctx: ITraderContext) {

    val back = backBets.filter(b => ctx.getEventTimestamp - b.matchedDate.get < 60000).foldLeft(0d)((sum, bet) => sum + bet.betSize)
    val lay = layBets.filter(b => ctx.getEventTimestamp - b.matchedDate.get < 60000).foldLeft(0d)((sum, bet) => sum + bet.betSize)

    if (back < 400000) {
      ctx.addChartValue("delta", (back - lay) / 1000)
      ctx.addChartValue("price", ctx.getBestPrices(runnerId)._1.price)
    }
  }
}