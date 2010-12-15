package dk.bettingai.trader.probslope

import scala.collection.JavaConversions._
import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import dk.bettingai.marketsimulator.trader._
import ITrader._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.PriceUtil._
import com.espertech.esper.client._
import com.espertech.esper.client.time._

/**This trader draws chart with slope (linear regression) for price implied probability for all market runners.*/
class ProbSlopeTrader extends ITrader {

  val config = new Configuration()
  config.getEngineDefaults().getThreading().setInternalTimerEnabled(false)

  val tradedVolumeEventProps = new java.util.HashMap[String, Object]
  tradedVolumeEventProps.put("runnerId", "long")
  tradedVolumeEventProps.put("prob", "double")
  tradedVolumeEventProps.put("timestamp", "long")
  config.addEventType("ProbEvent", tradedVolumeEventProps)

  val epService: EPServiceProvider = EPServiceProviderManager.getDefaultProvider(config)

  val expression = "select runnerId, slope from ProbEvent.std:groupwin(runnerId).win:time(120 sec).stat:linest(timestamp,prob, runnerId)"
  var stmt: EPStatement = null

  override def init(ctx: ITraderContext) {
    epService.initialize()
    stmt = epService.getEPAdministrator().createEPL(expression)
  }

  def execute(ctx: ITraderContext) = {
    /**Send new time event to esper.*/
    epService.getEPRuntime().sendEvent(new CurrentTimeEvent(ctx.getEventTimestamp))

    /**Sent events to esper.*/
    for (runnerId <- ctx.runners.map(_.runnerId)) {
      val bestPrices = ctx.getBestPrices(runnerId)
      val avgPrice = PriceUtil.avgPrice(bestPrices._1.price -> bestPrices._2.price)
      epService.getEPRuntime().sendEvent(Map("runnerId" -> runnerId, "prob" -> 100 / avgPrice, "timestamp" -> ctx.getEventTimestamp / 1000), "ProbEvent")
    }
    /**Pull epl statement and draw chart.*/
    for (event <- stmt.iterator) ctx.addChartValue("tv" + event.get("runnerId"), event.get("slope").asInstanceOf[Double])
   
  }
}