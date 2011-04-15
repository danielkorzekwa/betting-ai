package dk.bettingai.trader.nn.multinomial

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

class MultiNomailNeuralTraderTest {

  @Test
  def test {

   // val marketData = MarketData("c:/daniel/marketdata50")
    val marketData = MarketData("./src/test/resources/two_hr_10mins_before_inplay")
    val neuralTraderScore = new NeuralTraderScoreCalc(marketData, network => MultiNomialNeuralTrader(network))
    val network = MultiNomialNeuralTrader.createNetwork()

    /**Min population size should be 50.*/
    val train = new NeuralGeneticAlgorithm(network, new FanInRandomizer(), neuralTraderScore, 10, 0.1d, 0.25d)

    var bestScore = Double.MinValue
    for (i <- 1 to 5) {
      train.iteration();
      println("Epoch #" + i + " Score:" + train.getError());
      if (train.getError > bestScore) {
        bestScore = train.getError
        dk.bettingai.marketsimulator.SimulatorApp.run(marketData.data, MultiNomialNeuralTrader(train.getNetwork), System.out, "./target")
      }
    }
  }

}