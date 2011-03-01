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
  def apply(traderId: String, backPriceSlopeSignal: Double, layPriceSlopeSignal: Double, maxPrice: Double, maxNumOfRunners: Int, minProfitLoss: Double, minTradedVolume: Double): PricePriceSlopeTrader = {
    val trader = new PricePriceSlopeTrader()
    trader.traderId = traderId
    trader.backPriceSlopeSignal = backPriceSlopeSignal
    trader.layPriceSlopeSignal = layPriceSlopeSignal
    trader.maxPrice = maxPrice
    trader.maxNumOfRunners = maxNumOfRunners
    trader.minProfitLoss = minProfitLoss
    trader.minTradedVolume = minTradedVolume
    trader
  }
}
class PricePriceSlopeTrader extends ITrader {

  var traderId = "pricePriceTrader1"
  var backPriceSlopeSignal = -0.03
  var layPriceSlopeSignal = -0.04
  var maxPrice = 3.05
  var maxNumOfRunners = 6
  var minProfitLoss = -45d
  var minTradedVolume = 522d

  private val config = new Configuration()
  config.getEngineDefaults().getThreading().setInternalTimerEnabled(false)

  private val probEventProps = new java.util.HashMap[String, Object]
  probEventProps.put("runnerId", "long")
  probEventProps.put("prob", "double")
  probEventProps.put("timestamp", "long")
  config.addEventType("ProbEvent", probEventProps)

  private val tradedVolumeEventProps = new java.util.HashMap[String, Object]
  tradedVolumeEventProps.put("runnerId", "long")
  tradedVolumeEventProps.put("tradedVolume", "double")
  tradedVolumeEventProps.put("timestamp", "long")
  config.addEventType("TradedVolumeEvent", tradedVolumeEventProps)

  /**Key market id.*/
  private var epService: mutable.Map[Long, EPServiceProvider] = new mutable.HashMap[Long, EPServiceProvider] with mutable.SynchronizedMap[Long, EPServiceProvider]

  private val expression = "select runnerId, slope from ProbEvent.std:groupwin(runnerId).win:time(120 sec).stat:linest(timestamp,prob, runnerId)"
  private val exprTradedVolume = "select runnerId,(last(tradedVolume)-first(tradedVolume))/(last(timestamp)-first(timestamp)) as delta from TradedVolumeEvent.win:time(120 sec) group by runnerId"

  /**Key - marketId.*/
  private var stmt: mutable.Map[Long, EPStatement] = new mutable.HashMap[Long, EPStatement] with mutable.SynchronizedMap[Long, EPStatement]

  /**Key - marketId.*/
  private var stmt2: mutable.Map[Long, EPStatement] = new mutable.HashMap[Long, EPStatement] with mutable.SynchronizedMap[Long, EPStatement]

  var numOfRunners: mutable.Map[Long, Int] = new mutable.HashMap[Long, Int] with mutable.SynchronizedMap[Long, Int]

  override def init(ctx: ITraderContext) {
    epService(ctx.marketId) = EPServiceProviderManager.getProvider(traderId + ":" + ctx.marketId, config)
    epService(ctx.marketId).initialize()
    stmt(ctx.marketId) = epService(ctx.marketId).getEPAdministrator().createEPL(expression)
    stmt2(ctx.marketId) = epService(ctx.marketId).getEPAdministrator().createEPL(exprTradedVolume)

    numOfRunners(ctx.marketId) = ctx.runners.size
  }

  def execute(ctx: ITraderContext) = {

    if (numOfRunners(ctx.marketId) < maxNumOfRunners) {

      /**Send new time event to esper.*/
      epService(ctx.marketId).getEPRuntime().sendEvent(new CurrentTimeEvent(ctx.getEventTimestamp))

      /**Sent events to esper.*/
      for (runnerId <- ctx.runners.map(_.runnerId)) {
        val bestPrices = ctx.getBestPrices(runnerId)
        val avgPrice = PriceUtil.avgPrice(bestPrices._1.price -> bestPrices._2.price)
        epService(ctx.marketId).getEPRuntime().sendEvent(Map("runnerId" -> runnerId, "prob" -> 100 / avgPrice, "timestamp" -> ctx.getEventTimestamp / 1000), "ProbEvent")
        epService(ctx.marketId).getEPRuntime().sendEvent(Map("runnerId" -> runnerId, "tradedVolume" -> ctx.getRunnerTradedVolume(runnerId).totalTradedVolume, "timestamp" -> ctx.getEventTimestamp / 1000), "TradedVolumeEvent")
      }

      val risk = ctx.risk

      val deltaMap = Map(stmt2(ctx.marketId).iterator.map(event => (event.get("runnerId").asInstanceOf[Long], event.get("delta").asInstanceOf[Double])).toList: _*)
      /**Pull epl statement and execute trading strategy.*/
      for (event <- stmt(ctx.marketId).iterator) {
        val runnerId = event.get("runnerId").asInstanceOf[Long]
        if (deltaMap(runnerId) > minTradedVolume) {

          val priceSlope = -1 * event.get("slope").asInstanceOf[Double] //convert prob slope to price slope
          val bestPrices = ctx.getBestPrices(runnerId)

          val probs = ProbabilityCalculator.calculate(ctx.getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), 1)

          if (!bestPrices._1.price.isNaN && bestPrices._1.price < maxPrice && risk.ifLose(runnerId) > minProfitLoss) {
            val matchedBetsBack = List(new Bet(1, 1, 2, bestPrices._1.price, BACK, M, ctx.marketId, runnerId))
            val riskBack = ExpectedProfitCalculator.calculate(matchedBetsBack, probs, ctx.commission)
            if (priceSlope < backPriceSlopeSignal && riskBack.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._1.price, BACK, runnerId)
          }

          if (!bestPrices._2.price.isNaN && bestPrices._2.price < maxPrice && risk.ifWin(runnerId) > minProfitLoss) {
            val matchedBetsLay = List(new Bet(1, 1, 2, bestPrices._2.price, LAY, M, ctx.marketId, runnerId))
            val riskLay = ExpectedProfitCalculator.calculate(matchedBetsLay, probs, ctx.commission)
            if (priceSlope > layPriceSlopeSignal && riskLay.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._2.price, LAY, runnerId)
          }

        }

      }
    }
  }

  override def after(ctx: ITraderContext) {
    epService(ctx.marketId).destroy()
  }

  override def toString = "PricePriceSlopeTrader [id=%s, backSlope=%s, laySlope=%s, maxPrice=%s,mxNumOfRunners=%s,minProfitLoss=%s,minTradedVolume=%s]".format(traderId, backPriceSlopeSignal, layPriceSlopeSignal, maxPrice, maxNumOfRunners, minProfitLoss, minTradedVolume)
}