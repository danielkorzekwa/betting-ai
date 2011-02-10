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
  }
}

class CoevolutionHillClimbingTest {

	private val marketDataDir = new File("./src/test/resources/one_hr_10mins_before_inplay")
	private val marketDataSources = Map (marketDataDir.listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f) : _*)
	
	private val trader = new PriceTrader(2.22)
	
	val populationSize = 10
	val maxGenerationNum = 10
	@Test def test {
		 
		val progress = (iter:Int,best:Solution[PriceTrader],current:Solution[PriceTrader]) => println("Iter number=" + iter + ", best=" + best.price + ",current=" + current.price)
		val bestSolution = CoevolutionHillClimbing.optimise(marketDataSources,trader, (t:PriceTrader) => t, populationSize, maxGenerationNum, progress)
		
		println("Best solution price=" + bestSolution.price)	
	}
}