package dk.bettingai.trader.priceslope

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
 * Places back and lay bets based on price slope (linear regression)
 *
 * @author korzekwad
 *
 * @param traderId Must be unique for all traders.
 * @param backPriceSlopeSignal Back bet is placed when priceSlope < backPriceSlopeSignal.
 * @param layPriceSlopeSignal Lay bet is placed when priceSlope > layPriceSlopeSignal.
 */
class PriceSlopeTrader(val traderId: String, val backPriceSlopeSignal: Double, val layPriceSlopeSignal: Double) extends ITrader {

  val bank=1000d
	
  override def init(ctx: ITraderContext) {

    def getEventTypes(): Map[String, Map[String, Object]] = {
      val probEventProps = new java.util.HashMap[String, Object]
      probEventProps.put("runnerId", "long")
      probEventProps.put("prob", "double")
      probEventProps.put("timestamp", "long")
      Map("ProbEvent" -> probEventProps)
    }

    def getEPLStatements(): Map[String, String] = {
      val priceSlopeEPL = "select runnerId, slope from ProbEvent.std:groupwin(runnerId).win:time(30 sec).stat:linest(timestamp,prob, runnerId)"
      Map("priceSlope" -> priceSlopeEPL)
    }

    def publish(epServiceProvider: EPServiceProvider) {
      for (runnerId <- ctx.runners.map(_.runnerId)) {
        val bestPrices = ctx.getBestPrices(runnerId)
        val avgPrice = PriceUtil.avgPrice(bestPrices._1.price -> bestPrices._2.price)
        epServiceProvider.getEPRuntime().sendEvent(Map("runnerId" -> runnerId, "prob" -> 100 / avgPrice, "timestamp" -> ctx.getEventTimestamp / 1000), "ProbEvent")
      }
    }

    ctx.registerEPN(getEventTypes, getEPLStatements, publish) 
  }

  def execute(ctx: ITraderContext) = {

    /**Pull epl statement and execute trading strategy.*/
    for (event <- ctx.getEPNStatement("priceSlope").iterator) {
      val runnerId = event.get("runnerId").asInstanceOf[Long]
      val priceSlope = -1 * event.get("slope").asInstanceOf[Double] //convert prob slope to price slope
      val bestPrices = ctx.getBestPrices(runnerId)

      val probs = ProbabilityCalculator.calculate(ctx.getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), 1)

      if (!bestPrices._1.price.isNaN) {
        val matchedBetsBack = List(new Bet(1, 1, 2, bestPrices._1.price, BACK, M, ctx.marketId, runnerId, 1000,None))
        val riskBack = ExpectedProfitCalculator.calculate(matchedBetsBack, probs, ctx.commission,bank)
        if (priceSlope < backPriceSlopeSignal && riskBack.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._1.price, BACK, runnerId)
      }

      if (!bestPrices._2.price.isNaN) {
        val matchedBetsLay = List(new Bet(1, 1, 2, bestPrices._2.price, LAY, M, ctx.marketId, runnerId, 1000,None))
        val riskLay = ExpectedProfitCalculator.calculate(matchedBetsLay, probs, ctx.commission,bank)
        if (priceSlope > layPriceSlopeSignal && riskLay.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._2.price, LAY, runnerId)
      }
    }

  }
  
  override def toString = "PriceSlopeTrader [id=%s, backSlope=%s, laySlope=%s]".format(traderId, backPriceSlopeSignal, layPriceSlopeSignal)
}