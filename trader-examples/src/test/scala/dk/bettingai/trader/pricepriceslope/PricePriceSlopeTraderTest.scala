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
 * Set more memory and active jmx: 
 * -Xmx512m 
 * -Dcom.sun.management.jmxremote
 * 
 *  bestSoFar=Solution [trader=PriceSlopeTrader [id=trader4, backSlope=-0.01, laySlope=-0.02, maxPrice=2.84], expectedProfit=181.32852996310174, matchedBetsNum=3253.0]
 *
 */
class PricePriceSlopeTraderTest {

  private val log = LoggerFactory.getLogger(getClass)

  /**Number of matched bets for this trader will be zero, it's to test escaping from local maximum.*/
  val trader = PricePriceSlopeTrader("baseTrader", -0.21, 0.21, 5,20,-10,10)

  private val populationSize = 5
  private val generationNum = 5

  private val rand = new Random(System.currentTimeMillis)

  @Test
  def testTwoMarkets {

    log.info("Initial trader=" + trader)

    var lastTraderId = 1
    def nextTraderId = { lastTraderId += 1; lastTraderId }

   // val marketDataDir = "c:/daniel/marketdata"
    val marketDataDir = "./src/test/resources/two_hr_10mins_before_inplay"

    val mutate = (solution: Solution[PricePriceSlopeTrader]) => {

      /**If numbers of matched bets = 0 then initialise trader with random values to escape from local maximum.*/
      if (solution.matchedBetsNum > 0) {
        val backPriceSlopeSignal = solution.trader.backPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
        val layPriceSlopeSignal = solution.trader.layPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
        val maxPrice = move(solution.trader.maxPrice, rand.nextInt(11) - 5)
        PricePriceSlopeTrader("trader" + nextTraderId, backPriceSlopeSignal, layPriceSlopeSignal, maxPrice,2,-10,10)
      } else {
        val backPriceSlopeSignal = ((rand.nextInt(11) - 5) * 0.01)
        val layPriceSlopeSignal = ((rand.nextInt(11) - 5) * 0.01)
        val maxPrice = priceUp(1 / rand.nextDouble)
        val trader = PricePriceSlopeTrader("trader" + nextTraderId, backPriceSlopeSignal, layPriceSlopeSignal, maxPrice,20,-10,10)
        log.info("Escaping from local maximum (number of matched bets=0). " + trader)
        trader
      }
    }
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataDir, trader, mutate, populationSize, generationNum)

    log.info("Best solution=" + bestSolution)
  }

}