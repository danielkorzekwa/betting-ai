package dk.bettingai.trader.stat.s1

import com.espertech.esper.client._
import com.espertech.esper.client.time._
import dk.bettingai.marketsimulator.trader._
import dk.betex._
import dk.betex.PriceUtil._
import scala.collection.JavaConversions._
import dk.betting.risk.prob._
import dk.betting.risk.liability._
import dk.betex.api._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import scala.collection._

class S1Trader extends ITrader {

  override def init(ctx: ITraderContext) {

    def getEventTypes(): Map[String, Map[String, Object]] = {
      val probEventProps = new java.util.HashMap[String, Object]
      probEventProps.put("runnerId", "long")
      probEventProps.put("prob", "double")
      probEventProps.put("timestamp", "long")

      val tradedVolumeEventProps = new java.util.HashMap[String, Object]
      tradedVolumeEventProps.put("runnerId", "long")
      tradedVolumeEventProps.put("tradedVolume", "double")
      tradedVolumeEventProps.put("timestamp", "long")
      Map("ProbEvent" -> probEventProps, "TradedVolumeEvent" -> tradedVolumeEventProps)
    }

    def getEPLStatements(): Map[String, String] = {
      val priceSlopeEPL = "select runnerId, slope from ProbEvent.std:groupwin(runnerId).win:time(120 sec).stat:linest(timestamp,prob, runnerId)"
      val tradedVolumeDeltaEPL = "select runnerId,(last(tradedVolume)-first(tradedVolume))/(last(timestamp)-first(timestamp)) as delta from TradedVolumeEvent.win:time(120 sec) group by runnerId"
      Map("priceSlope" -> priceSlopeEPL, "tradedVolumeDelta" -> tradedVolumeDeltaEPL)
    }

    def publish(epServiceProvider: EPServiceProvider) {
      for (runnerId <- ctx.runners.map(_.runnerId)) {
        val bestPrices = ctx.getBestPrices(runnerId)
        val avgPrice = PriceUtil.avgPrice(bestPrices._1.price -> bestPrices._2.price)
        epServiceProvider.getEPRuntime().sendEvent(Map("runnerId" -> runnerId, "prob" -> 100 / avgPrice, "timestamp" -> ctx.getEventTimestamp / 1000), "ProbEvent")
        epServiceProvider.getEPRuntime().sendEvent(Map("runnerId" -> runnerId, "tradedVolume" -> ctx.getTotalTradedVolume(runnerId), "timestamp" -> ctx.getEventTimestamp / 1000), "TradedVolumeEvent")
      }

    }

    ctx.registerEPN(getEventTypes, getEPLStatements, publish)

  }

  def execute(ctx: ITraderContext) = {

    val deltaMap = Map(ctx.getEPNStatement("tradedVolumeDelta").iterator.map(event => (event.get("runnerId").asInstanceOf[Long], event.get("delta").asInstanceOf[Double])).toList: _*)

    val runnerId = ctx.runners.head.runnerId
    for (r1 <- ctx.runners.filter(r => r.runnerId != runnerId)) {
      ctx.addChartValue(runnerId + ":" + r1.runnerId, 1 / ctx.getBestPrices(r1.runnerId)._1.price + 1 / ctx.getBestPrices(runnerId)._1.price)
    }

  }

}