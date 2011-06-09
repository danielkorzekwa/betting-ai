package dk.bettingai.tradingoptimiser

import org.junit._
import Assert._
import dk.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import org.slf4j.LoggerFactory
import dk.betex.PriceUtil._
import java.util.Random
import HillClimbing._
import CoevolutionHillClimbing._
import dk.bettingai.marketsimulator.ISimulator._

class CoevolutionHillClimbingTest {

  private val log = LoggerFactory.getLogger(getClass)

  val trader = new PriceTrader(2.22)

  private val populationSize = 10
  private val generationNum = 10

  private val rand = new Random(System.currentTimeMillis)

  @Test
  def testOneMarket {

    log.info("Initial trader=" + trader)

    val marketData = MarketData("./src/test/resources/one_hr_10mins_before_inplay")
    val progress = (progress: Progress[PriceTrader]) => log.info("Iter number=" + progress.iterationNum + ", bestSoFar=" + progress.bestSoFar + ", currentBest=" + progress.currentBest)
    val mutate = (solution: Solution[PriceTrader]) => new TraderFactory[PriceTrader]() {
      val price = move(solution.trader.price, rand.nextInt(11) - 5)
      def create() = new PriceTrader(price)
    }

    val bestSolution = CoevolutionHillClimbing(marketData, mutate, populationSize, 1000).optimise(trader, generationNum, Option(progress))
    assertTrue(bestSolution.quality != 0)
    assertTrue(bestSolution.matchedBetsNum > 0)
    log.info("Best solution=" + bestSolution)
  }

  @Test
  def testTwoMarkets {

    log.info("Initial trader=" + trader)

    val marketData = MarketData("./src/test/resources/two_hr_10mins_before_inplay")
    val mutate = (solution: Solution[PriceTrader]) => new TraderFactory[PriceTrader]() {
      val price = move(solution.trader.price, rand.nextInt(11) - 5)
      def create() = new PriceTrader(price)
    }
    val bestSolution = CoevolutionHillClimbing(marketData, mutate, populationSize, 10000).optimise(trader, generationNum)
    assertTrue(bestSolution.quality != 0)
    assertTrue(bestSolution.matchedBetsNum != 0)
    log.info("Best solution=" + bestSolution)
  }

  @Test
  def testNoMarketData {
    log.info("Initial trader=" + trader)

    val marketData = MarketData()
    val progress = (progress: Progress[PriceTrader]) => log.info("Iter number=" + progress.iterationNum + ", bestSoFar=" + progress.bestSoFar + ", currentBest=" + progress.currentBest)
    val mutate = (solution: Solution[PriceTrader]) => new TraderFactory[PriceTrader]() {
      val price = move(solution.trader.price, rand.nextInt(11) - 5)
      def create() = new PriceTrader(price)
    }
    val bestSolution = CoevolutionHillClimbing(marketData, mutate, populationSize, 1000).optimise(trader, generationNum, Option(progress))
    assertEquals(0, bestSolution.quality, 0)
    assertEquals(0, bestSolution.matchedBetsNum, 0)

    log.info("Best solution=" + bestSolution)
  }

  @Test
  def testEmptyMarketFiles {
    log.info("Initial trader=" + trader)

    val marketData = MarketData("./src/test/resources/empty_market_files")
    val mutate = (solution: Solution[PriceTrader]) => new TraderFactory[PriceTrader]() {
      val price = move(solution.trader.price, rand.nextInt(11) - 5)
      def create() = new PriceTrader(price)
    }
    val bestSolution = CoevolutionHillClimbing(marketData, mutate, populationSize, 1000).optimise(trader, generationNum)
    assertEquals(0, bestSolution.quality, 0)
    assertEquals(0, bestSolution.matchedBetsNum, 0)

    log.info("Best solution=" + bestSolution)
  }
}