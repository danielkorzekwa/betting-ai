package dk.bettingai.trader.tradedvolumedeltaprim

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

/** Creates time series chart with second derivative of runner traded volume with respect to time.
 * 
 * @author korzekwad
 *  
 */
class TradedVolumeDeltaPrimTrader extends ITrader {
  val config = new Configuration()
  config.getEngineDefaults().getThreading().setInternalTimerEnabled(false)

  val tradedVolumeEventProps = new java.util.HashMap[String, Object]
  tradedVolumeEventProps.put("runnerId", "long")
  tradedVolumeEventProps.put("tradedVolume", "double")
  tradedVolumeEventProps.put("timestamp", "long")
  config.addEventType("TradedVolumeEvent", tradedVolumeEventProps)

  /**Key market id.*/
  var epService: mutable.Map[Long, EPServiceProvider] = new mutable.HashMap[Long, EPServiceProvider] with mutable.SynchronizedMap[Long, EPServiceProvider]

  /**It is called once for every analysed market.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
   * */
  override def init(ctx: ITraderContext) {

    epService(ctx.marketId) = EPServiceProviderManager.getProvider("" + ctx.marketId, config)
    epService(ctx.marketId).initialize()

    val deltaExpr = "insert into TradedVolumeDeltaEvent select runnerId,timestamp,(last(tradedVolume)-first(tradedVolume))/(last(timestamp)-first(timestamp)) as tradedVolumeDelta from TradedVolumeEvent.win:time(120 sec) group by runnerId"
    epService(ctx.marketId).getEPAdministrator().createEPL(deltaExpr)

    val deltaPrimExpr = "select runnerId,(last(tradedVolumeDelta)-first(tradedVolumeDelta))/(last(timestamp)-first(timestamp)) as tradedVolumeDeltaPrim from TradedVolumeDeltaEvent.win:time(120 sec) group by runnerId"
    val statement = epService(ctx.marketId).getEPAdministrator().createEPL(deltaPrimExpr)
    statement.addListener(new UpdateListener {
      def update(newEvents: Array[EventBean], oldEvents: Array[EventBean]) {
        ctx.addChartValue("tv" + newEvents(0).get("runnerId"), newEvents(0).get("tradedVolumeDeltaPrim").asInstanceOf[Double])
      }
    })
  }

  /**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
   */

  def execute(ctx: ITraderContext) = {
    epService(ctx.marketId).getEPRuntime().sendEvent(new CurrentTimeEvent(ctx.getEventTimestamp))
    for (runnerId <- ctx.runners.map(_.runnerId))
      epService(ctx.marketId).getEPRuntime().sendEvent(Map("runnerId" -> runnerId, "tradedVolume" -> ctx.getRunnerTradedVolume(runnerId).totalTradedVolume, "timestamp" -> ctx.getEventTimestamp / 1000), "TradedVolumeEvent")
  }
}