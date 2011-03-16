package dk.bettingai.tradingoptimiser

import org.junit._
import Assert._
import java.io.File
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import CoevolutionHillClimbingTest._
import org.slf4j.LoggerFactory
import dk.bettingai.marketsimulator.betex.PriceUtil._
import java.util.Random
import scala.collection.immutable.TreeMap

object CoevolutionHillClimbingTest {
 
}

class CoevolutionHillClimbingTest {

  private val log = LoggerFactory.getLogger(getClass)

  val trader = new PriceTrader(2.22)

  private val populationSize = 10
  private val generationNum = 10

  private val rand = new Random(System.currentTimeMillis)

  @Test
  def testOneMarket {

    log.info("Initial trader=" + trader)

    val marketDataDir = new File("./src/test/resources/one_hr_10mins_before_inplay")
    val marketDataSources = TreeMap(marketDataDir.listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f): _*)
    val progress = (iter: Int, best: Solution[PriceTrader], current: Solution[PriceTrader]) => log.info("Iter number=" + iter + ", bestSoFar=" + best + ", currentBest=" + current)
    val mutate = (solution: Solution[PriceTrader]) => new PriceTrader(move(solution.trader.price, rand.nextInt(11) - 5))
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataSources, trader, mutate, populationSize, generationNum, progress)

    log.info("Best solution=" + bestSolution)
  }

  @Test
  def testTwoMarkets {

    log.info("Initial trader=" + trader)

    val marketDataDir = "./src/test/resources/two_hr_10mins_before_inplay"
    val mutate = (solution: Solution[PriceTrader]) => new PriceTrader(move(solution.trader.price, rand.nextInt(11) - 5))
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataDir, trader, mutate, populationSize, generationNum)

    log.info("Best solution=" + bestSolution)
  }

  @Test
  def testNoMarketData {
    log.info("Initial trader=" + trader)

    val marketDataSources: TreeMap[Long, File] = TreeMap()
    val progress = (iter: Int, best: Solution[PriceTrader], current: Solution[PriceTrader]) => log.info("Iter number=" + iter + ", bestSoFar=" + best + ", currentBest=" + current)
    val mutate = (solution: Solution[PriceTrader]) => new PriceTrader(move(solution.trader.price, rand.nextInt(11) - 5))
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataSources, trader, mutate, populationSize, generationNum, progress)
    assertEquals(Solution(trader, Double.MinValue, 0), bestSolution)

    log.info("Best solution=" + bestSolution)
  }

  @Test
  def testEmptyMarketFiles {
    log.info("Initial trader=" + trader)

    val marketDataDir = "./src/test/resources/empty_market_files"
    val mutate = (solution: Solution[PriceTrader]) => new PriceTrader(move(solution.trader.price, rand.nextInt(11) - 5))
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataDir, trader, mutate, populationSize, generationNum)
    assertEquals(Solution(trader, Double.MinValue, 0), bestSolution)

    log.info("Best solution=" + bestSolution)
  }
}