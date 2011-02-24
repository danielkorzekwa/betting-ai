package dk.bettingai.trader.pricepriceslope

import org.junit._
import Assert._
import org.slf4j.LoggerFactory
import java.util.Random
import java.io.File
import dk.bettingai.tradingoptimiser._
import dk.bettingai.tradingoptimiser.ICoevolutionHillClimbing._
import dk.bettingai.marketsimulator.betex.PriceUtil._

/**Run trader implementation, full random mutate only.
 * 
 * @author korzekwad
 * 
 * Set more memory and active jmx: 
 * -Xmx512m 
 * -Dcom.sun.management.jmxremote
 * 
 *  bestSoFar=Solution [trader=PriceSlopeTrader [id=trader869, backSlope=-0.04, laySlope=-0.02, maxPrice=3.2], expectedProfit=141.8728123260652, matchedBetsNum=11080.0],
 *  50 markets, 23/02/2011 09:51:02 INFO  dk.bettingai.tradingoptimiser.CoevolutionHillClimbing$ - Iter number=156, bestSoFar=Solution [trader=PriceSlopeTrader [id=trader778, backSlope=-0.03, laySlope=-0.05, maxPrice=3.25], expectedProfit=195.72460059023436, matchedBetsNum=12751.0], currentBest=Solution [trader=PriceSlopeTrader [id=trader778, backSlope=-0.03, laySlope=-0.05, maxPrice=3.25], expectedProfit=195.72460059023436, matchedBetsNum=12751.0]
 *  275 markets, 24/02/2011 08:31:54 INFO  dk.bettingai.tradingoptimiser.CoevolutionHillClimbing$ - Iter number=76, bestSoFar=Solution [trader=PriceSlopeTrader [id=trader43, backSlope=-0.02, laySlope=-0.01, maxPrice=1.81], expectedProfit=106.75496311563737, matchedBetsNum=6450.0], currentBest=Solution [trader=PriceSlopeTrader [id=trader377, backSlope=0.01, laySlope=0.01, maxPrice=1.22], expectedProfit=0.0, matchedBetsNum=0.0] 
 *  
 */
class PricePriceSlopeTraderRandomTest {

  private val log = LoggerFactory.getLogger(getClass)

  val baseTrader = new PricePriceSlopeTrader("baseTrader", -0.21, 0.21, 5)

  private val populationSize = 5
  private val generationNum = 5

  private val rand = new Random(System.currentTimeMillis)

  @Test
  def testTwoMarkets {

    log.info("Initial trader=" + baseTrader)

    var lastTraderId = 1
    def nextTraderId = { lastTraderId += 1; lastTraderId }

    // val marketDataDir = "c:/daniel/marketdatafull"
    val marketDataDir = "./src/test/resources/two_hr_10mins_before_inplay"

    /**Full random mutate only.*/
    val mutate = (solution: Solution[PricePriceSlopeTrader]) => {
      val backPriceSlopeSignal = ((rand.nextInt(11) - 5) * 0.01)
      val layPriceSlopeSignal = ((rand.nextInt(11) - 5) * 0.01)
      val maxPrice = priceUp(1 / rand.nextDouble)
      val trader = new PricePriceSlopeTrader("trader" + nextTraderId, backPriceSlopeSignal, layPriceSlopeSignal, maxPrice)
      trader
    }
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataDir, baseTrader, mutate, populationSize, generationNum)

    log.info("Best solution=" + bestSolution)
  }

}