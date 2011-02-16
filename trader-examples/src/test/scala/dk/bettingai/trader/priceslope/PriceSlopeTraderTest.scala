package dk.bettingai.trader.priceslope

import org.junit._
import Assert._
import org.slf4j.LoggerFactory
import java.util.Random
import java.io.File
import dk.bettingai.tradingoptimiser._
import dk.bettingai.tradingoptimiser.ICoevolutionHillClimbing._

/**Run trader implementation.
 * 
 * @author korzekwad
 *
 */
class PriceSlopeTraderTest {

  private val log = LoggerFactory.getLogger(getClass)

  val trader = new PriceSlopeTrader("baseTrader", -0.01, 0.01)

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
    val progress = (iter: Int, best: Solution[PriceSlopeTrader], current: Solution[PriceSlopeTrader]) => log.info("Iter number=" + iter + ", bestSoFar=" + best + ", currentBest=" + current)
    val mutate = (t: PriceSlopeTrader) => {
      val backPriceSlopeSignal = t.backPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
      val layPriceSlopeSignal = t.layPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
      new PriceSlopeTrader("trader" + nextTraderId, backPriceSlopeSignal, layPriceSlopeSignal)
    }
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataSources, trader, mutate, populationSize, generationNum, progress)

    log.info("Best solution=" + bestSolution)
  }

}