package dk.bettingai.trader.ifwinlosedelta

import org.junit._
import java.util.Random
import org.slf4j.LoggerFactory
import dk.bettingai.tradingoptimiser._
import HillClimbing._
import CoevolutionHillClimbing._

class IfwinlosedeltaTraderRandomSearchTest {

  val log = LoggerFactory.getLogger(getClass)
  val baseTrader = IfwinlosedeltaTrader(0d, 0d)

  private val populationSize = 5
  private val generationNum = 5

  private val rand = new Random(System.currentTimeMillis)

  @Test
  def testTwoMarkets {

    log.info("Initial trader=" + baseTrader)

    //val marketDataDir = "c:/daniel/marketdata"
     val marketData = MarketData("./src/test/resources/two_hr_10mins_before_inplay")

    /**Full random mutate only.*/
    val mutate = (solution: Solution[IfwinlosedeltaTrader]) => {
      val backSignal = (rand.nextInt(600) - 300)
      val laySignal = (rand.nextInt(600) - 300)
      val trader = IfwinlosedeltaTrader(backSignal, laySignal)
      trader
    }
    val bestSolution = CoevolutionHillClimbing(marketData,mutate,populationSize).optimise(baseTrader, generationNum)

    log.info("Best solution=" + bestSolution)
  }
}