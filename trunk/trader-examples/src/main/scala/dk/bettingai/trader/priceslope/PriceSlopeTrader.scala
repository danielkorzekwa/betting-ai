package dk.bettingai.trader.priceslope

import com.espertech.esper.client._
import com.espertech.esper.client.time._
import dk.bettingai.marketsimulator.trader._
import ITrader._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.PriceUtil._
import scala.collection.JavaConversions._
import dk.bettingai.marketsimulator.risk._
import dk.bettingai.marketsimulator.betex.api._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import scala.collection._

/**Places back and lay bets based on price slope (linear regression)
 * 
 * @author korzekwad
 *
 *@param traderId Must be unique for all traders.
 *@param backPriceSlopeSignal Back bet is placed when priceSlope < backPriceSlopeSignal.
 *@param layPriceSlopeSignal Lay bet is placed when priceSlope > layPriceSlopeSignal.
 */
class PriceSlopeTrader(val traderId: String, val backPriceSlopeSignal: Double, val layPriceSlopeSignal: Double) extends ITrader {

  val config = new Configuration()
  config.getEngineDefaults().getThreading().setInternalTimerEnabled(false)

  val probEventProps = new java.util.HashMap[String, Object]
  probEventProps.put("runnerId", "long")
  probEventProps.put("prob", "double")
  probEventProps.put("timestamp", "long")
  config.addEventType("ProbEvent", probEventProps)

   /**Key market id.*/
  var epService: mutable.Map[Long, EPServiceProvider] = new mutable.HashMap[Long, EPServiceProvider] with mutable.SynchronizedMap[Long, EPServiceProvider]

  val expression = "select runnerId, slope from ProbEvent.std:groupwin(runnerId).win:time(120 sec).stat:linest(timestamp,prob, runnerId)"
  
  /**Key - marketId.*/
  var stmt: mutable.Map[Long, EPStatement] = new mutable.HashMap[Long,EPStatement] with mutable.SynchronizedMap[Long,EPStatement]

  override def init(ctx: ITraderContext) {
	epService(ctx.marketId) = EPServiceProviderManager.getProvider(traderId + ":" + ctx.marketId, config)
    epService(ctx.marketId).initialize()
    stmt(ctx.marketId) = epService(ctx.marketId).getEPAdministrator().createEPL(expression)
  }

  def execute(ctx: ITraderContext) = {

    /**Send new time event to esper.*/
    epService(ctx.marketId).getEPRuntime().sendEvent(new CurrentTimeEvent(ctx.getEventTimestamp))

    /**Sent events to esper.*/
    for (runnerId <- ctx.runners.map(_.runnerId)) {
      val bestPrices = ctx.getBestPrices(runnerId)
      val avgPrice = PriceUtil.avgPrice(bestPrices._1.price -> bestPrices._2.price)
      epService(ctx.marketId).getEPRuntime().sendEvent(Map("runnerId" -> runnerId, "prob" -> 100 / avgPrice, "timestamp" -> ctx.getEventTimestamp / 1000), "ProbEvent")
    }

    /**Pull epl statement and execute trading strategy.*/
    for (event <- stmt(ctx.marketId).iterator) {
      val runnerId = event.get("runnerId").asInstanceOf[Long]
      val priceSlope = -1 * event.get("slope").asInstanceOf[Double] //convert prob slope to price slope
      val bestPrices = ctx.getBestPrices(runnerId)

      val probs = ProbabilityCalculator.calculate(ctx.getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), 1)

      if (!bestPrices._1.price.isNaN) {
        val matchedBetsBack = List(new Bet(1, 1, 2, bestPrices._1.price, BACK, M, ctx.marketId, runnerId))
        val riskBack = ExpectedProfitCalculator.calculate(matchedBetsBack, probs, ctx.commission)
        if (priceSlope < backPriceSlopeSignal && riskBack.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._1.price, BACK, runnerId)
      }

      if (!bestPrices._2.price.isNaN) {
        val matchedBetsLay = List(new Bet(1, 1, 2, bestPrices._2.price, LAY, M, ctx.marketId, runnerId))
        val riskLay = ExpectedProfitCalculator.calculate(matchedBetsLay, probs, ctx.commission)
        if (priceSlope > layPriceSlopeSignal && riskLay.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._2.price, LAY, runnerId)
      }
    }

  }

  override def after(ctx: ITraderContext) {
    epService(ctx.marketId).destroy()
  }

  override def toString = "PriceSlopeTrader [id=%s, backSlope=%s, laySlope=%s]".format(traderId, backPriceSlopeSignal, layPriceSlopeSignal)
}