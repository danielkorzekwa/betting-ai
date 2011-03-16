package dk.bettingai.tradingoptimiser

import org.junit._
import scala.util.Random
import dk.bettingai.marketsimulator.betex.PriceUtil._
import org.slf4j._

class CoevolutionHillClimbingWithRestartsTest {

  private val marketData = MarketData("./src/test/resources/one_hr_10mins_before_inplay")

  private val rand = new Random(System.currentTimeMillis)

  private def restart(solution: Solution[PriceTrader]): PriceTrader = {
    val price = priceUp(1 / rand.nextDouble)
    PriceTrader(price)
  }

  private def mutate(solution: Solution[PriceTrader]): PriceTrader = {
    new PriceTrader(move(solution.trader.price, rand.nextInt(11) - 5))
  }

  private val populationSize = 5
  private val generarationSize = 10

  @Test
  def testOneMarketNoLocalOptimisation {
    val trader = PriceTrader(1.01d)

    /**No optimisation.*/
    def optimise(solution: Solution[PriceTrader]): Solution[PriceTrader] = solution

    val best = new CoevolutionHillClimbingWithRestarts().optimise(marketData, trader, restart _, populationSize, generarationSize, optimise _, None)

  }

  @Test
  def testOneMarketWithLocalOptimisation {
    val trader = PriceTrader(1.01d)

    val populationSize = 5
    val generarationSize = 10

    /**Run optimisation.*/
    def optimise(solution: Solution[PriceTrader]): Solution[PriceTrader] = {
      val logger = LoggerFactory.getLogger(getClass)

      val progress = (iter: Int, best: Solution[PriceTrader], current: Solution[PriceTrader]) => logger.info("OPT Iter number=" + iter + ", bestSoFar=" + best + ", currentBest=" + current)
      CoevolutionHillClimbing.optimise(marketData.data, solution.trader, mutate _, populationSize, generarationSize, progress)
    }

    val best = new CoevolutionHillClimbingWithRestarts().optimise(marketData, trader, restart _, populationSize, generarationSize, optimise _)
  }
}