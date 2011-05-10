package dk.bettingai.trader.nn.multinomial2

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
import dk.bettingai.marketsimulator.ISimulator._
import dk.bettingai.marketsimulator.ISimulator._

class Multinomial2NeuralTraderTrainingTest {

  val bank = 50

  //val marketData = MarketData("c:/daniel/marketdata10")
  val marketData = MarketData("./src/test/resources/five_hr_10mins_before_inplay")
  def getTraderFactory(network: BasicNetwork) = new TraderFactory[Multinomial2NeuralTrader] {
    def create() = Multinomial2NeuralTrader(network)
  }
  val neuralTraderScore = new NeuralTraderScoreCalc(marketData, network => { getTraderFactory(network) }, bank)
  val network = Multinomial2NeuralTrader.createNetwork()

  /**Min population size should be 50.*/
  val train = new NeuralGeneticAlgorithm(network, new FanInRandomizer(), neuralTraderScore, 10, 0.1d, 0.25d)
  // val train = new NeuralSimulatedAnnealing(network,neuralTraderScore,10,2,100)
  val nnResource = new EncogPersistedCollection("./target/nn_" + classOf[Multinomial2NeuralTrader].getSimpleName + ".eg")
  nnResource.create()

  @Test
  def test {

    var bestScore = Double.MinValue
    for (i <- 1 to 5) {
      train.iteration();
      println("Epoch #" + i + " Score:" + train.getError());
      if (train.getError > bestScore) {
        bestScore = train.getError
        val traderFactory = new TraderFactory[Multinomial2NeuralTrader] {
          def create() = Multinomial2NeuralTrader(train.getNetwork)
        }
        dk.bettingai.marketsimulator.SimulatorApp.run(UserInputData(marketData.data, traderFactory, "./target", bank), System.out)
        nnResource.add("nn", train.getNetwork)
      }
    }
  }
}