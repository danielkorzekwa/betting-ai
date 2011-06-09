package dk.bettingai.trader.pricepriceslope

import org.junit._
import Assert._
import org.slf4j.LoggerFactory
import java.util.Random
import java.io.File
import dk.bettingai.tradingoptimiser._
import dk.betex.PriceUtil._
import HillClimbing._
import CoevolutionHillClimbing._
import dk.bettingai.marketsimulator.ISimulator._

/**
 * Run trader implementation, full random mutate only.
 *
 * @author korzekwad
 *
 * Set more memory and active jmx:
 * -Xmx512m
 * -Dcom.sun.management.jmxremote
 *
 */
class PricePriceSlopeTraderRandomWithOptimisationTest {

  private val log = LoggerFactory.getLogger(getClass)

  val baseTrader = PricePriceSlopeTrader("baseTrader", -0.21, 0.21, 5, 20, -10, 10)

  private val populationSize = 5
  private val generationNum = 5

  private val rand = new Random(System.currentTimeMillis)

  @Test
  def testTwoMarkets {

    log.info("Initial trader=" + baseTrader)

    var lastTraderId = 1
    def nextTraderId = { lastTraderId += 1; lastTraderId }

    val bank = 1000
    // val marketData = MarketData("c:/daniel/marketdataall")
    val marketData = MarketData("./src/test/resources/two_hr_10mins_before_inplay")

    /**Full random mutate only.*/
    val restart = (solution: Solution[PricePriceSlopeTrader]) => {
      val backPriceSlopeSignal = ((rand.nextInt(11) - 5) * 0.01)
      val layPriceSlopeSignal = ((rand.nextInt(11) - 5) * 0.01)
      val maxPrice = priceUp(1 / rand.nextDouble)
      val maxNumOfRunners = 3 + rand.nextInt(20)
      val minProfitLoss = -rand.nextInt(3000)
      val minTradedVolume = rand.nextInt(1000)
      new TraderFactory[PricePriceSlopeTrader]() {
        def create() = PricePriceSlopeTrader("trader" + nextTraderId, backPriceSlopeSignal, layPriceSlopeSignal, maxPrice, maxNumOfRunners, minProfitLoss, minTradedVolume)
      }
    }

    val optimiser = new CoevolutionHillClimbing(marketData, restart, populationSize, bank) with LocalOptimisation[PricePriceSlopeTrader] {
      def optimise(solution: Solution[PricePriceSlopeTrader]): Solution[PricePriceSlopeTrader] = {
        val logger = LoggerFactory.getLogger(getClass)
        val generationSize = 2
        val progress = (progress: Progress[PricePriceSlopeTrader]) => logger.info("OPTIMISATION, Iter number=" + progress.iterationNum + ", bestSoFar=" + progress.bestSoFar + ", currentBest=" + progress.currentBest)

        val mutate = (solution: Solution[PricePriceSlopeTrader]) => {
          val backPriceSlopeSignal = solution.trader.backPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
          val layPriceSlopeSignal = solution.trader.layPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
          val maxPrice = move(solution.trader.maxPrice, rand.nextInt(11) - 5)

          new TraderFactory[PricePriceSlopeTrader]() {
            def create() = PricePriceSlopeTrader("trader" + nextTraderId, backPriceSlopeSignal, layPriceSlopeSignal, maxPrice, solution.trader.maxNumOfRunners, solution.trader.minProfitLoss, solution.trader.minTradedVolume)
          }
        }

        CoevolutionHillClimbing(marketData, mutate, populationSize, bank).optimise(solution.trader, generationSize, Option(progress))
      }
    }
    val bestSolution = optimiser.optimise(baseTrader, generationNum)

    log.info("Best solution=" + bestSolution)
  }

}