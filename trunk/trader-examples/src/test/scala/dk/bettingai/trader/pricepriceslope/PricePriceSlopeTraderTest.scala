package dk.bettingai.trader.pricepriceslope

import org.junit._
import Assert._
import org.slf4j.LoggerFactory
import java.util.Random
import java.io.File
import dk.bettingai.tradingoptimiser._
import dk.bettingai.tradingoptimiser.ICoevolutionHillClimbing._
import dk.bettingai.marketsimulator.betex.PriceUtil._

/**Run trader implementation.
 * 
 * @author korzekwad
 *
 */
class PricePriceSlopeTraderTest {

  private val log = LoggerFactory.getLogger(getClass)

  val trader = new PricePriceSlopeTrader("baseTrader", -0.01, 0.01, 5)

  private val populationSize = 5
  private val generationNum = 5

  private val rand = new Random(System.currentTimeMillis)

  @Test
  def testTwoMarkets {

    log.info("Initial trader=" + trader)

    var lastTraderId = 1
    def nextTraderId = { lastTraderId += 1; lastTraderId }

    val marketDataDir = new File("./src/test/resources/two_hr_10mins_before_inplay")
    val marketDataSources = Map(marketDataDir.listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f): _*)
    val progress = (iter: Int, best: Solution[PricePriceSlopeTrader], current: Solution[PricePriceSlopeTrader]) => log.info("Iter number=" + iter + ", bestSoFar=" + best + ", currentBest=" + current)
    val mutate = (t: PricePriceSlopeTrader) => {
      val backPriceSlopeSignal = t.backPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
      val layPriceSlopeSignal = t.layPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
      val maxPrice = move(t.maxPrice, rand.nextInt(11) - 5)
      new PricePriceSlopeTrader("trader" + nextTraderId, backPriceSlopeSignal, layPriceSlopeSignal, maxPrice)
    }
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataSources, trader, mutate, populationSize, generationNum, progress)

    log.info("Best solution=" + bestSolution)
  }

}