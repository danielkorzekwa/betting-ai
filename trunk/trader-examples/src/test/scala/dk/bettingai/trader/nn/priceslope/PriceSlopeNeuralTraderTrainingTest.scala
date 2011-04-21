package dk.bettingai.trader.nn.priceslope

import org.junit._
import Assert._
import org.encog.neural.networks.training.genetic._
import org.encog.neural.networks.training._
import org.encog.mathutil.randomize.FanInRandomizer
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.BasicNetwork
import dk.bettingai.marketsimulator._
import ISimulator._
import dk.bettingai.tradingoptimiser._
import dk.bettingai.tradingoptimiser.nn._
import org.encog.persist.EncogPersistedCollection
class PriceSlopeNeuralTraderTrainingTest {

  val bank = 100

  //val marketData = MarketData("c:/daniel/marketdata10")
  val marketData = MarketData("./src/test/resources/one_hr_10mins_before_inplay")
  val neuralTraderScore = new NeuralTraderScoreCalc(marketData, network => {PriceSlopeNeuralTrader(network)}, bank)
  val network = PriceSlopeNeuralTrader.createNetwork()

  /**Min population size should be 50.*/
  val train = new NeuralGeneticAlgorithm(network, new FanInRandomizer(), neuralTraderScore, 20, 0.1d, 0.25d)

  val nnResource = new EncogPersistedCollection("./target/nn_" + classOf[PriceSlopeNeuralTrader].getSimpleName + ".eg")
  nnResource.create()

  @Test
  def test {

    var bestScore = Double.MinValue
    for (i <- 1 to 10) {
      train.iteration();
      println("Epoch #" + i + " Score:" + train.getError());
      if (train.getError > bestScore) {
        bestScore = train.getError
        dk.bettingai.marketsimulator.SimulatorApp.run(UserInputData(marketData.data, PriceSlopeNeuralTrader(train.getNetwork), "./target", bank), System.out)
        nnResource.add("nn", train.getNetwork)
      }
    }
  }
}