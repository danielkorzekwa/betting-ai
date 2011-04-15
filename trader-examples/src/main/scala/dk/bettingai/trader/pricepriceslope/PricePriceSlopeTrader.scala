package dk.bettingai.trader.pricepriceslope

import com.espertech.esper.client._
import com.espertech.esper.client.time._
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.PriceUtil._
import scala.collection.JavaConversions._
import dk.bettingai.marketsimulator.risk._
import dk.bettingai.marketsimulator.betex.api._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import scala.collection._
/**
 * Places back and lay bets based on price slope (linear regression) and price.
 *
 * @author korzekwad
 *
 * @param traderId Must be unique for all traders.
 * @param backPriceSlopeSignal Back bet is placed when priceSlope < backPriceSlopeSignal.
 * @param layPriceSlopeSignal Lay bet is placed when priceSlope > layPriceSlopeSignal.
 * @param maxPrice Place back/lay bets if price < maxPrice.
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

  val bank=1000d
  var traderId = "pricePriceTrader1"
  var backPriceSlopeSignal = 0.02
  var layPriceSlopeSignal = -0.03
  var maxPrice = 9.6d
  var maxNumOfRunners = 16
  var minProfitLoss = -1d
  var minTradedVolume = 33d

  /**key - marketId.*/
  var numOfRunners: mutable.Map[Long, Int] = new mutable.HashMap[Long, Int] with mutable.SynchronizedMap[Long, Int]

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
    numOfRunners(ctx.marketId) = ctx.runners.size
  }

  def execute(ctx: ITraderContext) = {

    if (numOfRunners(ctx.marketId) < maxNumOfRunners) {

      val risk = ctx.risk(bank)
      ctx.addChartValue("expected", risk.marketExpectedProfit)
      val deltaMap = Map(ctx.getEPNStatement("tradedVolumeDelta").iterator.map(event => (event.get("runnerId").asInstanceOf[Long], event.get("delta").asInstanceOf[Double])).toList: _*)

      /**Pull epl statement and execute trading strategy.*/
      for (event <- ctx.getEPNStatement("priceSlope").iterator) {
        val runnerId = event.get("runnerId").asInstanceOf[Long]
        if (deltaMap(runnerId) > minTradedVolume) {

          val priceSlope = -1 * event.get("slope").asInstanceOf[Double] //convert prob slope to price slope
          val bestPrices = ctx.getBestPrices(runnerId)

          val probs = ProbabilityCalculator.calculate(ctx.getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), 1)

          if (!bestPrices._2.price.isNaN && bestPrices._2.price < maxPrice && risk.ifLose(runnerId) > minProfitLoss) {
            val matchedBetsBack = List(new Bet(1, 1, 2, bestPrices._2.price, BACK, M, ctx.marketId, runnerId,None))
            val riskBack = ExpectedProfitCalculator.calculate(matchedBetsBack, probs, ctx.commission,bank)
            if (priceSlope < backPriceSlopeSignal && riskBack.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._2.price, BACK, runnerId)
          }

          if (!bestPrices._1.price.isNaN && bestPrices._1.price < maxPrice && risk.ifWin(runnerId) > minProfitLoss) {
            val matchedBetsLay = List(new Bet(1, 1, 2, bestPrices._1.price, LAY, M, ctx.marketId, runnerId,None))
            val riskLay = ExpectedProfitCalculator.calculate(matchedBetsLay, probs, ctx.commission,bank)
            if (priceSlope > layPriceSlopeSignal && riskLay.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._1.price, LAY, runnerId)
          }

        }

      }
    }
  }

  override def toString = "PricePriceSlopeTrader [id=%s, backSlope=%s, laySlope=%s, maxPrice=%s,mxNumOfRunners=%s,minProfitLoss=%s,minTradedVolume=%s]".format(traderId, backPriceSlopeSignal, layPriceSlopeSignal, maxPrice, maxNumOfRunners, minProfitLoss, minTradedVolume)
}