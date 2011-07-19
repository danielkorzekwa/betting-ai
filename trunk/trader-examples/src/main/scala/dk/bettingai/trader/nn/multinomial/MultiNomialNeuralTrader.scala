package dk.bettingai.trader.nn.multinomial

import com.espertech.esper.client._
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
import org.encog.neural.pattern.FeedForwardPattern
import org.encog.engine.network.activation.ActivationTANH
import org.encog.neural.data._
import org.encog.neural.data.basic._
import org.encog.neural.networks.BasicNetwork

/**This trader uses multiple variables and neural network to take trading decisions.*/
object MultiNomialNeuralTrader {

  /**Creates neural network that is used by this trader.*/
  def createNetwork(): BasicNetwork = {
    val pattern = new FeedForwardPattern();

    /**
     * 1 - numOfRunners
     * 2 - priceSlope
     * 3 - traded volume delta
     * 4 - priceToBack
     * 5 - priceToLay
     * 6 - ifWin
     * 7 - ifLose
     */
    pattern.setInputNeurons(7);
    pattern.addHiddenLayer(5);
    /**
     * 1 - place back bet
     * 2 - place lay bet
     */
    pattern.setOutputNeurons(2);
    pattern.setActivationFunction(new ActivationTANH());
    val network = pattern.generate();
    network.reset();
    network
  }

}
case class MultiNomialNeuralTrader(network: BasicNetwork) extends ITrader {

  val bank=1000d
	
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

    val risk = ctx.risk(bank)
    ctx.addChartValue("expected", risk.marketExpectedProfit)
    val deltaMap = Map(ctx.getEPNStatement("tradedVolumeDelta").iterator.map(event => (event.get("runnerId").asInstanceOf[Long], event.get("delta").asInstanceOf[Double])).toList: _*)

    /**Pull epl statement and execute trading strategy.*/
    for (event <- ctx.getEPNStatement("priceSlope").iterator) {
      val runnerId = event.get("runnerId").asInstanceOf[Long]
      val priceSlope = -1 * event.get("slope").asInstanceOf[Double] //convert prob slope to price slope
      val bestPrices = ctx.getBestPrices(runnerId)
      val probs = ProbabilityCalculator.calculate(ctx.getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), 1)

      val numOfRunnersValue = numOfRunners(ctx.marketId).toDouble
      val neuralData = new BasicNeuralData(Array(
        numOfRunnersValue,
        priceSlope,
        deltaMap(runnerId),
        bestPrices._1.price,
        bestPrices._2.price,
        risk.ifWin(runnerId),
        risk.ifLose(runnerId)))

      /**Neural Network is not thread safe.*/
      var decision: NeuralData = null
      this.synchronized {
        decision = network.compute(neuralData)
      }

      if (!bestPrices._1.price.isNaN) {
        val matchedBetsBack = List(new Bet(1, 1, 2, bestPrices._1.price, BACK, M, ctx.marketId, runnerId,1000, None))
        val riskBack = ExpectedProfitCalculator.calculate(matchedBetsBack, probs, ctx.commission,bank)
        if (decision.getData(0) > 0 && riskBack.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._1.price, BACK, runnerId)
      }

      if (!bestPrices._2.price.isNaN) {
        val matchedBetsLay = List(new Bet(1, 1, 2, bestPrices._2.price, LAY, M, ctx.marketId, runnerId,1000, None))
        val riskLay = ExpectedProfitCalculator.calculate(matchedBetsLay, probs, ctx.commission,bank)
        if (decision.getData(1) > 0 && riskLay.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._2.price, LAY, runnerId)
      }
    }
  }

}