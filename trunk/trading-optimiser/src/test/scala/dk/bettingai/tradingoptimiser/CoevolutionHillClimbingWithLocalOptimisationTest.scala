package dk.bettingai.tradingoptimiser

import org.junit._
import Assert._
import scala.util.Random
import dk.bettingai.marketsimulator.betex.PriceUtil._
import org.slf4j._
import HillClimbing._
import CoevolutionHillClimbing._

class CoevolutionHillClimbingWithLocalOptimisationTest {

  private val marketData = MarketData("./src/test/resources/one_hr_10mins_before_inplay")

  private val rand = new Random(System.currentTimeMillis)

  private def restart(solution: Solution[PriceTrader]): PriceTrader = {
    val price = priceUp(1 / rand.nextDouble)
    PriceTrader(price)
  }

  private val populationSize = 5
  private val generarationSize = 10

  @Test
  def testOneMarketNoLocalOptimisation {
    val trader = PriceTrader(2.2d)

    val optimiser = new CoevolutionHillClimbing[PriceTrader](marketData, restart, populationSize,10000) with LocalOptimisation[PriceTrader] {
      /**No optimisation.*/
      def optimise(solution: Solution[PriceTrader]): Solution[PriceTrader] = solution
    }

    val bestSolution = optimiser.optimise(trader, generarationSize)
    assertTrue("Expected profit is zero",bestSolution.quality > 0)
    assertTrue("Number of matched bets is zero",bestSolution.matchedBetsNum > 0)

  }

  @Test
  def testOneMarketWithLocalOptimisation {
    val trader = PriceTrader(1.01d)

    val optimiser = new CoevolutionHillClimbing[PriceTrader](marketData, restart, populationSize,1000) with LocalOptimisation[PriceTrader] {
      /**Run optimisation.*/
      def optimise(solution: Solution[PriceTrader]): Solution[PriceTrader] = {
        val logger = LoggerFactory.getLogger(getClass)

        val progress = (progress: Progress[PriceTrader]) => logger.info("OPTIMISATION, Iter number=" + progress.iterationNum + ", bestSoFar=" + progress.bestSoFar + ", currentBest=" + progress.currentBest)
        CoevolutionHillClimbing(marketData, mutate, populationSize,10000).optimise(solution.trader, generarationSize, Option(progress))
      }
    }
    val bestSolution = optimiser.optimise(trader, generarationSize)
    assertTrue(bestSolution.quality != 0)
    assertTrue(bestSolution.matchedBetsNum > 0)
  }
}