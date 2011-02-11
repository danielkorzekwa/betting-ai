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

    val marketId = 101655622
    val runnerId = 4207432

    def execute(ctx: ITraderContext) = {
      if (ctx.marketId == marketId) {
        val bestPrices = ctx.getBestPrices(runnerId)
        if (bestPrices._1.price > price) ctx.fillBet(2, bestPrices._1.price, BACK, runnerId)
      }
    }

    override def toString = "PriceTrader [price=%s]".format(price)
  }
}

class CoevolutionHillClimbingTest {

  private val log = LoggerFactory.getLogger(getClass)

  private val marketDataDir = new File("./src/test/resources/one_hr_10mins_before_inplay")
  private val marketDataSources = Map(marketDataDir.listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f): _*)

  private val trader = new PriceTrader(2.22)

  private val populationSize = 10
  private val generationNum = 10
  
  private val rand = new Random(System.currentTimeMillis)
  
  @Test
  def test {

    log.info("Initial trader=" + trader)

    val progress = (iter: Int, best: Solution[PriceTrader], current: Solution[PriceTrader]) => println("Iter number=" + iter + ", bestSoFar=" + best + ", currentBest=" + current)

    val mutate = (t: PriceTrader) => new PriceTrader(move(t.price, rand.nextInt(11) - 5))
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataSources, trader, mutate, populationSize, generationNum, progress)

    log.info("Best solution=" + bestSolution)
  }
}