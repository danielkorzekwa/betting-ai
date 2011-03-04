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

    val marketDataDir ="./src/test/resources/two_hr_10mins_before_inplay"
    val mutate = (solution: Solution[PriceSlopeTrader]) => {
      val backPriceSlopeSignal = solution.trader.backPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
      val layPriceSlopeSignal = solution.trader.layPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
      new PriceSlopeTrader("trader" + nextTraderId, backPriceSlopeSignal, layPriceSlopeSignal)
    }
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataDir, trader, mutate, populationSize, generationNum)

    log.info("Best solution=" + bestSolution)
  }

}