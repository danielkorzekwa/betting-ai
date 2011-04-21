package dk.bettingai.trader.nn.priceslope

import dk.bettingai.marketsimulator.trader._
import org.encog.neural.data.basic._
import org.encog.neural.networks.BasicNetwork
import dk.bettingai.marketsimulator.betex.api._
import IBet.BetTypeEnum._
import com.espertech.esper.client._
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.PriceUtil._
import scala.collection.JavaConversions._
import dk.bettingai.marketsimulator.risk._
import dk.bettingai.marketsimulator.betex.api._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import scala.collection._
import org.encog.neural.pattern.FeedForwardPattern
import org.encog.engine.network.activation.ActivationTANH
import org.encog.neural.data._
import org.encog.persist.EncogPersistedCollection

/**@see class level comments.*/
object PriceSlopeNeuralTrader {

  /**Creates neural network that is used by this trader.*/
  def createNetwork(): BasicNetwork = {
    val pattern = new FeedForwardPattern();
    /**1 - priceToBack, 2 - priceToLay, 3 - priceSlope, 4 - marketExpectedProfit*/
    pattern.setInputNeurons(4);
    pattern.addHiddenLayer(5);
    /**1 - back bet, 2 - lay bet, 3 - hedge bet*/
    pattern.setOutputNeurons(3);
    pattern.setActivationFunction(new ActivationTANH());
    val network = pattern.generate();
    network.reset();
    network
  }
  
  def apply(network:BasicNetwork):PriceSlopeNeuralTrader = {
	  val trader = new PriceSlopeNeuralTrader()
	   trader.network = network
	   trader
  }

}

/**
 * This trader uses neural network for taking bet placement decisions, which is seeded with a single price slope variable.
 *
 * @author korzekwad
 *
 */
case class PriceSlopeNeuralTrader extends ITrader {
  val nnResource = new EncogPersistedCollection("./src/main/resources/nn/" + classOf[PriceSlopeNeuralTrader].getSimpleName + ".eg")
  var network: BasicNetwork = nnResource.find("nn").asInstanceOf[BasicNetwork]
  val bank = 100d

  override def init(ctx: ITraderContext) {

    def getEventTypes(): Map[String, Map[String, Object]] = {
      val probEventProps = new java.util.HashMap[String, Object]
      probEventProps.put("runnerId", "long")
      probEventProps.put("prob", "double")
      probEventProps.put("timestamp", "long")
      Map("ProbEvent" -> probEventProps)
    }

    def getEPLStatements(): Map[String, String] = {
      val priceSlopeEPL = "select runnerId, slope from ProbEvent.std:groupwin(runnerId).win:time(120 sec).stat:linest(timestamp,prob, runnerId)"
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

      val risk = ctx.risk(bank)
      ctx.addChartValue("risk",risk.marketExpectedProfit)
      ctx.addChartValue("wealth",risk.wealth)
      
        if (runnerId == 4207432 || runnerId == 3954418 || runnerId == 3826850 || runnerId == 4806390) {
     // if (runnerId == 3954418) {
        val priceSlope = -1 * event.get("slope").asInstanceOf[Double] //convert prob slope to price slope
        val bestPrices = ctx.getBestPrices(runnerId)

        val probs = ProbabilityCalculator.calculate(ctx.getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), 1)

        val neuralData = new BasicNeuralData(Array(
        		bestPrices._1.price,
        		bestPrices._2.price,
        		priceSlope,
        		risk.marketExpectedProfit)
        		)

        /**Neural Network is not thread safe.*/
        var decision: NeuralData = null
        this.synchronized {
          decision = network.compute(neuralData)
        }

        if (!bestPrices._1.price.isNaN) {
          val matchedBetsBack = List(new Bet(1, 1, 2, bestPrices._1.price, BACK, M, ctx.marketId, runnerId, None))
          val riskBack = ExpectedProfitCalculator.calculate(matchedBetsBack, probs, ctx.commission, bank)
          if (decision.getData(0) > 0 && riskBack.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._1.price, BACK, runnerId)
        }

        if (!bestPrices._2.price.isNaN) {
          val matchedBetsLay = List(new Bet(1, 1, 2, bestPrices._2.price, LAY, M, ctx.marketId, runnerId, None))
          val riskLay = ExpectedProfitCalculator.calculate(matchedBetsLay, probs, ctx.commission, bank)
          if (decision.getData(1) > 0 && riskLay.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._2.price, LAY, runnerId)
        }
        
        /**hedge.*/
        if(decision.getData(2) > 0) ctx.placeHedgeBet(runnerId)

        ctx.addChartValue("" + runnerId, 1/bestPrices._1.price)
      }
    }

  }

  override def toString = "PriceSlopeNeuralTrader [nn=%s]".format(network)
}