package dk.bettingai.trader.stat.s2

import dk.bettingai.trader._
import scala.collection._
import dk.bettingai.marketsimulator.trader._
import dk.betex.api._
import dk.betex._
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
  //  val runnerId = 3954418

  //	val runnerId = 1476041
  //	val runnerId = 3364827
  	//val runnerId = 3836058
  //	val runnerId = 2426396

  var backBets = mutable.ListBuffer[IBet]()
  var layBets = mutable.ListBuffer[IBet]()

  var uBackBets = mutable.ListBuffer[IBet]()
  var uLayBets = mutable.ListBuffer[IBet]()

  /**Tuples[amountCancelled,timestamp]*/
  var cancelledBets = mutable.ListBuffer[Tuple2[Long,IBet]]()

  override def init(ctx: ITraderContext) {

    def listener(bet: IBet): Unit = {

      if (bet.matchedDate.isEmpty) {

        bet.betType match {
          case BACK => if (bet.betPrice == ctx.getBestPrices(bet.runnerId)._2.price) uBackBets += bet
          case LAY => if (bet.betPrice == ctx.getBestPrices(bet.runnerId)._1.price) uLayBets += bet
        }
      }

    }

    def mListener(bet: IBet): Unit = {
      val matchedDelay = bet.matchedDate.get - bet.placedDate
      if (matchedDelay == 0) {

        bet.betType match {
          case BACK => backBets += bet
          case LAY => layBets += bet
        }
      }
    }

    def cancelListener(bet: IBet): Unit = {
      if(bet.runnerId==runnerId) cancelledBets += ctx.getEventTimestamp -> bet
    }
    ctx.addUnmatchedBetsListener(bet => bet.runnerId == runnerId, listener)
    ctx.addMatchedBetsListener(bet => bet.runnerId == runnerId, mListener)
    ctx.addCancelledBetsListener(cancelListener)
  }

  def execute(ctx: ITraderContext) {

    val back = backBets.filter(b => ctx.getEventTimestamp - b.matchedDate.get < 60000).foldLeft(0d)((sum, bet) => sum + bet.betSize)
    val lay = layBets.filter(b => ctx.getEventTimestamp - b.matchedDate.get < 60000).foldLeft(0d)((sum, bet) => sum + bet.betSize)

    val uBack = uBackBets.filter(b => ctx.getEventTimestamp - b.placedDate < 60000).foldLeft(0d)((sum, bet) => sum + bet.betSize)
    val uLay = uLayBets.filter(b => ctx.getEventTimestamp - b.placedDate < 60000).foldLeft(0d)((sum, bet) => sum + bet.betSize)

    val cBack = cancelledBets.filter{case (timestamp,b) => ctx.getEventTimestamp - timestamp < 60000 && b.betType==BACK && b.betPrice <= ctx.getBestPrices(b.runnerId)._2.price}.foldLeft(0d)((sum, bet) => sum + bet._2.betSize)
    val cLay = cancelledBets.filter{case (timestamp,b) => ctx.getEventTimestamp - timestamp < 60000&& b.betType==LAY && b.betPrice >= ctx.getBestPrices(b.runnerId)._1.price}.foldLeft(0d)((sum, bet) => sum + bet._2.betSize)

    if (back < 400000 && uLay < 30000) {
      ctx.addChartValue("b", (back) / 1000)
      ctx.addChartValue("l", (lay) / 1000)
      ctx.addChartValue("ub", (uBack) / 1000)
      ctx.addChartValue("ul", (uLay) / 1000)
      ctx.addChartValue("cb", (cBack) / 1000)
      ctx.addChartValue("cl", (cLay) / 1000)
      ctx.addChartValue("pb", ctx.getBestPrices(runnerId)._1.price)
      ctx.addChartValue("pl", ctx.getBestPrices(runnerId)._2.price)
    }
  }
}