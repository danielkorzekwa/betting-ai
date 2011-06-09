package dk.bettingai.trader.nn.multinomial2

import dk.bettingai.marketsimulator.trader._
import org.encog.neural.networks.BasicNetwork
import dk.betex.api._
import IBet.BetTypeEnum._
import com.espertech.esper.client._
import dk.bettingai.marketsimulator.trader._
import dk.betex._
import dk.betex.PriceUtil._
import scala.collection.JavaConversions._
import dk.bettingai.marketsimulator.risk._
import dk.betex.api._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import scala.collection._
import org.encog.neural.pattern.FeedForwardPattern
import org.encog.engine.network.activation.ActivationTANH
import org.encog.neural.data._
import org.encog.persist.EncogPersistedCollection
import org.encog.normalize._
import org.encog.normalize.input._
import org.encog.normalize.output._
import dk.betex.BetUtil._

/**@see class level comments.*/
object Multinomial2NeuralTrader {

  /**Creates neural network that is used by this trader.*/
  def createNetwork(): BasicNetwork = {
    val pattern = new FeedForwardPattern();
    /**1 - priceToBack, 2 - priceToLay, 3 - priceSlope, 4 - marketExpectedProfit, 5 - ifWin, 6 - ifLose, 7 ifWin-ifLose, 8 - tradedVolumeDelta, 9 - userAvgBackPrice, 10 - userAvgLayPrice*/
    pattern.setInputNeurons(10);
    pattern.addHiddenLayer(20);
    /**1 - back bet, 2 - lay bet, 3 - hedge bet*/
    pattern.setOutputNeurons(3);
    pattern.setActivationFunction(new ActivationTANH());
    val network = pattern.generate();
    network.reset();
    network
  }

  def apply(network: BasicNetwork): Multinomial2NeuralTrader = {
    val trader = new Multinomial2NeuralTrader()
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
case class Multinomial2NeuralTrader extends ITrader {
  val nnResource = new EncogPersistedCollection("./src/main/resources/nn/" + classOf[Multinomial2NeuralTrader].getSimpleName + ".eg")
  var network: BasicNetwork = nnResource.find("nn").asInstanceOf[BasicNetwork]
  val bank = 100d

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
  }

  def execute(ctx: ITraderContext) = {

    val deltaMap = Map(ctx.getEPNStatement("tradedVolumeDelta").iterator.map(event => (event.get("runnerId").asInstanceOf[Long], event.get("delta").asInstanceOf[Double])).toList: _*)

    val userMatchedBets = ctx.getBets(true)
    /**Pull epl statement and execute trading strategy.*/
    for (event <- ctx.getEPNStatement("priceSlope").iterator) {

      val runnerId = event.get("runnerId").asInstanceOf[Long]

      val runnerBets = userMatchedBets.filter(b => b.runnerId == runnerId).partition(b => b.betType == BACK)
      val risk = ctx.risk(bank)
      //   ctx.addChartValue("risk", risk.marketExpectedProfit)
      //   ctx.addChartValue("wealth", risk.wealth)
      //  val allBets = ctx.getBets(false).partition(b => b.betStatus==M)
      //  ctx.addChartValue("mBets",allBets._1.size)
      //  ctx.addChartValue("uBets",allBets._2.size)

      if (true || runnerId == 4207432 || runnerId == 3954418 || runnerId == 3826850 || runnerId == 4806390 || runnerId == 4090291 || runnerId == 4054137) {

        val priceSlope = -1 * event.get("slope").asInstanceOf[Double] //convert prob slope to price slope
        val bestPrices = ctx.getBestPrices(runnerId)

        val probs = ProbabilityCalculator.calculate(ctx.getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), 1)

        val norm = createDataNormalization()
        val rawData = Array(
          1 / bestPrices._1.price,
          1 / bestPrices._2.price,
          priceSlope,
          risk.marketExpectedProfit,
          risk.ifWin(runnerId),
          risk.ifLose(runnerId),
          risk.ifWin(runnerId) - risk.ifLose(runnerId),
          deltaMap(runnerId),
          1 / (if (runnerBets._1.isEmpty) 1 else BetUtil.avgPrice(runnerBets._1)),
          1 / (if (runnerBets._2.isEmpty) 1 else BetUtil.avgPrice(runnerBets._2)))

        val neuralData = norm.buildForNetworkInput(rawData)

        /**Neural Network is not thread safe.*/
        var decision: NeuralData = null
        this.synchronized {
          decision = network.compute(neuralData)
        }

        if (!bestPrices._2.price.isNaN) {
          val matchedBetsBack = List(new Bet(1, 1, 2, bestPrices._2.price, BACK, M, ctx.marketId, runnerId,1000, None))
          val riskBack = ExpectedProfitCalculator.calculate(matchedBetsBack, probs, ctx.commission, bank)
          if (decision.getData(0) > 0 && riskBack.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._2.price, BACK, runnerId)
        }

        if (!bestPrices._1.price.isNaN) {
          val matchedBetsLay = List(new Bet(1, 1, 2, bestPrices._1.price, LAY, M, ctx.marketId, runnerId,1000, None))
          val riskLay = ExpectedProfitCalculator.calculate(matchedBetsLay, probs, ctx.commission, bank)
          if (decision.getData(1) > 0 && riskLay.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._1.price, LAY, runnerId)
        }

        /**hedge.*/
        if (decision.getData(2) > 0) ctx.placeHedgeBet(runnerId)

        //  ctx.addChartValue("" + runnerId, 1 / bestPrices._1.price)
      }
    }

  }

  private def createDataNormalization(): DataNormalization = {
    val norm = new DataNormalization()
    val toBackInput = new BasicInputField()
    val toLayInput = new BasicInputField()
    val priceSlopeInput = new BasicInputField()
    val marketExpectedProfitInput = new BasicInputField()
    val ifWinInput = new BasicInputField()
    val ifLoseInput = new BasicInputField()
    val ifWinIfLoseDeltaInput = new BasicInputField()
    val tradedVolumeInput = new BasicInputField()
    val userAvgBackPriceInput = new BasicInputField()
    val userAvgLayPriceInput = new BasicInputField()
    norm.addInputField(toBackInput)
    norm.addInputField(toLayInput)
    norm.addInputField(priceSlopeInput)
    norm.addInputField(marketExpectedProfitInput)
    norm.addInputField(ifWinInput)
    norm.addInputField(ifLoseInput)
    norm.addInputField(ifWinIfLoseDeltaInput)
    norm.addInputField(tradedVolumeInput)
    norm.addInputField(userAvgBackPriceInput)
    norm.addInputField(userAvgLayPriceInput)

    norm.addOutputField(new OutputFieldDirect(toBackInput))
    norm.addOutputField(new OutputFieldDirect(toLayInput))
    norm.addOutputField(new OutputFieldDirect(priceSlopeInput))
    norm.addOutputField(new OutputFieldRangeMapped(marketExpectedProfitInput, -0.9, 0.9))
    norm.addOutputField(new OutputFieldRangeMapped(ifWinInput, -0.9, 0.9))
    norm.addOutputField(new OutputFieldRangeMapped(ifLoseInput, -0.9, 0.9))
    norm.addOutputField(new OutputFieldRangeMapped(ifWinIfLoseDeltaInput, -0.9, 0.9))
    norm.addOutputField(new OutputFieldRangeMapped(tradedVolumeInput, 0, 0.9))
    norm.addOutputField(new OutputFieldDirect(userAvgBackPriceInput))
    norm.addOutputField(new OutputFieldDirect(userAvgLayPriceInput))

    marketExpectedProfitInput.setMin(-100)
    marketExpectedProfitInput.setMax(100)
    ifWinInput.setMin(-200)
    ifWinInput.setMax(200)
    ifLoseInput.setMin(-200)
    ifLoseInput.setMax(200)
    ifWinIfLoseDeltaInput.setMin(-200)
    ifWinIfLoseDeltaInput.setMax(200)
    tradedVolumeInput.setMin(0)
    tradedVolumeInput.setMax(1000)

    norm
  }

  override def toString = "Multinomial2NeuralTrader [nn=%s]".format(network)
}