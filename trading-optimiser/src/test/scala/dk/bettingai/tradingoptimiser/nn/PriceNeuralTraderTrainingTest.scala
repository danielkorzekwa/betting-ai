package dk.bettingai.tradingoptimiser.nn

import org.junit._
import Assert._
import org.encog.neural.networks.training.genetic._
import org.encog.neural.pattern.FeedForwardPattern
import org.encog.neural.networks.training._
import org.encog.neural.networks.training._
import org.encog.mathutil.randomize.FanInRandomizer
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.pattern.FeedForwardPattern
import org.encog.engine.network.activation.ActivationTANH
import org.encog.neural.networks.BasicNetwork
import dk.bettingai.marketsimulator.trader._
import org.encog.neural.data.basic._
import dk.betex.api._
import IBet.BetTypeEnum._
import dk.bettingai.marketsimulator._
import ISimulator._
import dk.bettingai.tradingoptimiser._
import dk.bettingai.marketsimulator.ISimulator._

/**
 * Train neural network to trade on a market using a single price variable only.
 *
 * @author korzekwad
 */
class PriceNeuralTraderTrainingTest {

  @Test
  def test {

	val bank = 50
	  
    val marketData = MarketData("./src/test/resources/one_hr_10mins_before_inplay")
    def getFactory(network:BasicNetwork) =  new TraderFactory[NeuralTrader] {
          def create() = new NeuralTrader(network)
        }
    val neuralTraderScore = new NeuralTraderScoreCalc(marketData, network => getFactory(network),1000)
    val network = NeuralTrader.createNetwork()

    val train = new NeuralGeneticAlgorithm(network, new FanInRandomizer(), neuralTraderScore, 10, 0.1d, 0.25d)

    var bestScore = Double.MinValue
    for (i <- 1 to 5) {
      train.iteration();
      println("Epoch #" + i + " Score:" + train.getError());
      if (train.getError > bestScore) {
        bestScore = train.getError
         val traderFactory = new TraderFactory[NeuralTrader] {
          def create() = NeuralTrader(train.getNetwork)
        }
        dk.bettingai.marketsimulator.SimulatorApp.run(UserInputData(marketData.data, traderFactory, "./target", bank), System.out)
      }
    }
  }

  private object NeuralTrader {

    def createNetwork(): BasicNetwork = {
      val pattern = new FeedForwardPattern();
      pattern.setInputNeurons(1);
      pattern.addHiddenLayer(5);
      pattern.setOutputNeurons(1);
      pattern.setActivationFunction(new ActivationTANH());
      val network = pattern.generate();
      network.reset();
      network
    }
  }

  private case class NeuralTrader(network: BasicNetwork) extends ITrader {

    val market1Id = 101655622
    val market1RunnerId = 4207432

    def execute(ctx: ITraderContext) {

      if (ctx.marketId == market1Id) {
        val bestPrices = ctx.getBestPrices(market1RunnerId)

        val neuralData = new BasicNeuralData(Array(bestPrices._1.price))
        val output = network.compute(neuralData).getData(0)
        if (output > 0) ctx.fillBet(2, bestPrices._1.price, BACK, market1RunnerId)
      }

    }
  }
}

