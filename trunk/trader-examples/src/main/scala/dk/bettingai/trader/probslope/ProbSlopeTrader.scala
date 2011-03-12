package dk.bettingai.trader.probslope

import scala.collection.JavaConversions._
import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.PriceUtil._
import com.espertech.esper.client._
import com.espertech.esper.client.time._
import scala.collection._

/**This trader draws chart with slope (linear regression) for price implied probability for all market runners.*/
class ProbSlopeTrader extends ITrader {

  override def init(ctx: ITraderContext) {

    def getEventTypes(): Map[String, Map[String, Object]] = {
      val probEventProps = new java.util.HashMap[String, Object]
      probEventProps.put("runnerId", "long")
      probEventProps.put("prob", "double")
      probEventProps.put("timestamp", "long")
      Map("ProbEvent" -> probEventProps)
    }
    
    def getEPLStatements(): Map[String, String] = {
      val expression = "select runnerId, slope from ProbEvent.std:groupwin(runnerId).win:time(120 sec).stat:linest(timestamp,prob, runnerId)"
      Map("probSlope" -> expression)
    }

    def publish(epServiceProvider: EPServiceProvider) {
      /**Sent events to esper.*/
      for (runnerId <- ctx.runners.map(_.runnerId)) {
        val bestPrices = ctx.getBestPrices(runnerId)
        val avgPrice = PriceUtil.avgPrice(bestPrices._1.price -> bestPrices._2.price)
        epServiceProvider.getEPRuntime().sendEvent(Map("runnerId" -> runnerId, "prob" -> 100 / avgPrice, "timestamp" -> ctx.getEventTimestamp / 1000), "ProbEvent")
      }
    }

    ctx.registerEPN(getEventTypes, getEPLStatements, publish)
  }

  def execute(ctx: ITraderContext) = {
    /**Pull epl statement and draw chart.*/
    for (event <- ctx.getEPNStatement("probSlope").iterator) ctx.addChartValue("tv" + event.get("runnerId"), event.get("slope").asInstanceOf[Double])
  }

}