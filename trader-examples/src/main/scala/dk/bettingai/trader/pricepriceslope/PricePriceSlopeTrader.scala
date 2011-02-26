package dk.bettingai.trader.pricepriceslope

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
/**Places back and lay bets based on price slope (linear regression) and price.
 * 
 * @author korzekwad
 *
 *@param traderId Must be unique for all traders.
 *@param backPriceSlopeSignal Back bet is placed when priceSlope < backPriceSlopeSignal.
 *@param layPriceSlopeSignal Lay bet is placed when priceSlope > layPriceSlopeSignal.
 *@param maxPrice Place back/lay bets if price < maxPrice.
 */
object PricePriceSlopeTrader {
  def apply(traderId: String, backPriceSlopeSignal: Double, layPriceSlopeSignal: Double, maxPrice: Double): PricePriceSlopeTrader = {
    val trader = new PricePriceSlopeTrader()
    trader.traderId = traderId
    trader.backPriceSlopeSignal = backPriceSlopeSignal
    trader.layPriceSlopeSignal = layPriceSlopeSignal
    trader.maxPrice = maxPrice
    trader
  }
}
class PricePriceSlopeTrader extends ITrader {

  var traderId = "pricePriceTrader1"
  var backPriceSlopeSignal = -0.04
  var layPriceSlopeSignal = -0.04
  var maxPrice = 1.77

  private val config = new Configuration()
  config.getEngineDefaults().getThreading().setInternalTimerEnabled(false)

  private val probEventProps = new java.util.HashMap[String, Object]
  probEventProps.put("runnerId", "long")
  probEventProps.put("prob", "double")
  probEventProps.put("timestamp", "long")
  config.addEventType("ProbEvent", probEventProps)

  /**Key market id.*/
  private var epService: mutable.Map[Long, EPServiceProvider] = new mutable.HashMap[Long, EPServiceProvider] with mutable.SynchronizedMap[Long, EPServiceProvider]

  private val expression = "select runnerId, slope from ProbEvent.std:groupwin(runnerId).win:time(120 sec).stat:linest(timestamp,prob, runnerId)"

  /**Key - marketId.*/
  private var stmt: mutable.Map[Long, EPStatement] = new mutable.HashMap[Long, EPStatement] with mutable.SynchronizedMap[Long, EPStatement]

  override def init(ctx: ITraderContext) {
    epService += ctx.marketId -> EPServiceProviderManager.getProvider(traderId + ":" + ctx.marketId, config)
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

      if (!bestPrices._1.price.isNaN && bestPrices._1.price < maxPrice) {
        val matchedBetsBack = List(new Bet(1, 1, 2, bestPrices._1.price, BACK, M, ctx.marketId, runnerId))
        val riskBack = ExpectedProfitCalculator.calculate(matchedBetsBack, probs, ctx.commission)
        if (priceSlope < backPriceSlopeSignal && riskBack.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._1.price, BACK, runnerId)
      }

      if (!bestPrices._2.price.isNaN && bestPrices._2.price < maxPrice) {
        val matchedBetsLay = List(new Bet(1, 1, 2, bestPrices._2.price, LAY, M, ctx.marketId, runnerId))
        val riskLay = ExpectedProfitCalculator.calculate(matchedBetsLay, probs, ctx.commission)
        if (priceSlope > layPriceSlopeSignal && riskLay.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._2.price, LAY, runnerId)
      }
    }

  }

  override def after(ctx: ITraderContext) {
    epService(ctx.marketId).destroy()
  }

  override def toString = "PriceSlopeTrader [id=%s, backSlope=%s, laySlope=%s, maxPrice=%s]".format(traderId, backPriceSlopeSignal, layPriceSlopeSignal, maxPrice)
}