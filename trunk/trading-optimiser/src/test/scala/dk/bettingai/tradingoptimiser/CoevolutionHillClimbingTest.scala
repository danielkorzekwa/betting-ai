package dk.bettingai.tradingoptimiser

import org.junit._
import java.io.File
import dk.bettingai.marketsimulator.trader._
import ITrader._
import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import CoevolutionHillClimbingTest._
import ICoevolutionHillClimbing._
import org.slf4j.LoggerFactory
import dk.bettingai.marketsimulator.betex.PriceUtil._
import java.util.Random

object CoevolutionHillClimbingTest {
  class PriceTrader(val price: Double) extends ITrader {

    val market1Id = 101655622
    val market1RunnerId = 4207432

    val market2Id = 101655610
    val market2RunnerId = 3364827

    def execute(ctx: ITraderContext) = {

      if (ctx.marketId == market1Id) {
        val bestPrices = ctx.getBestPrices(market1RunnerId)
        if (bestPrices._1.price > price) ctx.fillBet(2, bestPrices._1.price, BACK, market1RunnerId)
      }

      if (ctx.marketId == market2Id) {
        val bestPrices = ctx.getBestPrices(market2RunnerId)
        if (bestPrices._1.price > price) ctx.fillBet(2, bestPrices._1.price, BACK, market2RunnerId)
      }

    }

    override def toString = "PriceTrader [price=%s]".format(price)
  }
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
    val marketDataSources = Map(marketDataDir.listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f): _*)
    val progress = (iter: Int, best: Solution[PriceTrader], current: Solution[PriceTrader]) => log.info("Iter number=" + iter + ", bestSoFar=" + best + ", currentBest=" + current)
    val mutate = (solution: Solution[PriceTrader]) => new PriceTrader(move(solution.trader.price, rand.nextInt(11) - 5))
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataSources, trader, mutate, populationSize, generationNum, progress)

    log.info("Best solution=" + bestSolution)
  }

  @Test
  def testTwoMarkets {

    log.info("Initial trader=" + trader)

    val marketDataDir = new File("./src/test/resources/two_hr_10mins_before_inplay")
    val marketDataSources = Map(marketDataDir.listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f): _*)
    val progress = (iter: Int, best: Solution[PriceTrader], current: Solution[PriceTrader]) => log.info("Iter number=" + iter + ", bestSoFar=" + best + ", currentBest=" + current)
    val mutate = (solution: Solution[PriceTrader]) => new PriceTrader(move(solution.trader.price, rand.nextInt(11) - 5))
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataSources, trader, mutate, populationSize, generationNum, progress)

    log.info("Best solution=" + bestSolution)
  }
}