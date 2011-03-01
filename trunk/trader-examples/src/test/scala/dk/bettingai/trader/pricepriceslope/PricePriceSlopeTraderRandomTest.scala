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
 *  275 markets  25/02/2011 07:08:27 INFO  dk.bettingai.tradingoptimiser.CoevolutionHillClimbing$ - Iter number=73, bestSoFar=Solution [trader=PriceSlopeTrader [id=trader304, backSlope=-0.04, laySlope=-0.04, maxPrice=1.77], expectedProfit=138.8839350498075, matchedBetsNum=6266.0], currentBest=Solution [trader=PriceSlopeTrader [id=trader366, backSlope=0.01, laySlope=-0.03, maxPrice=1.05], expectedProfit=0.0, matchedBetsNum=0.0] 
 *  275 markets  25/02/2011 07:08:27 INFO  dk.bettingai.tradingoptimiser.CoevolutionHillClimbing$ - Iter number=?, bestSoFar=Solution [trader=PriceSlopeTrader [id=trader?, backSlope=-0.03, laySlope=-+0.02, maxPrice=4.3, maxNumOfWinners=13], expectedProfit=190, matchedBetsNum=50000.0]
 *  275 markets  27/02/2011 07:22:28 INFO  dk.bettingai.tradingoptimiser.CoevolutionHillClimbing$ - Iter number=106, bestSoFar=Solution [trader=PriceSlopeTrader [id=trader100, backSlope=-0.04, laySlope=-0.04, maxPrice=1.78,numOfRunners=5], expectedProfit=215.44786786476382, matchedBetsNum=4126.0], currentBest=Solution [trader=PriceSlopeTrader [id=trader530, backSlope=0.0, laySlope=-0.01, maxPrice=1.16,numOfRunners=10], expectedProfit=0.0, matchedBetsNum=0.0] 
 *  50 markets  01/03/2011 11:19:49 INFO  dk.bettingai.tradingoptimiser.CoevolutionHillClimbing$ - Iter number=829, bestSoFar=Solution [trader=PriceSlopeTrader [id=trader820, backSlope=-0.05, laySlope=-0.03, maxPrice=3.75,mxNumOfRunners=6,minProfitLoss=-62.0,minTradedVolume=405.0], expectedProfit=9.600873255130312, matchedBetsNum=282.0], currentBest=Solution [trader=PriceSlopeTrader [id=trader4144, backSlope=-0.04, laySlope=-0.03, maxPrice=1.46,mxNumOfRunners=11,minProfitLoss=-1.0,minTradedVolume=917.0], expectedProfit=0.062050340423681453, matchedBetsNum=2.0] 
 *
 */
class PricePriceSlopeTraderRandomTest {

  private val log = LoggerFactory.getLogger(getClass)

  val baseTrader = PricePriceSlopeTrader("baseTrader", -0.21, 0.21, 5,20,-10,10)

  private val populationSize = 5
  private val generationNum = 5

  private val rand = new Random(System.currentTimeMillis)

  @Test
  def testTwoMarkets {

    log.info("Initial trader=" + baseTrader)

    var lastTraderId = 1
    def nextTraderId = { lastTraderId += 1; lastTraderId }

  //  val marketDataDir = "c:/daniel/marketdatafull"
    val marketDataDir = "./src/test/resources/two_hr_10mins_before_inplay"

    /**Full random mutate only.*/
    val mutate = (solution: Solution[PricePriceSlopeTrader]) => {
      val backPriceSlopeSignal = ((rand.nextInt(11) - 5) * 0.01)
      val layPriceSlopeSignal = ((rand.nextInt(11) - 5) * 0.01)
      val maxPrice = priceUp(1 / rand.nextDouble)
      val maxNumOfRunners = 3 + rand.nextInt(20)
      val minProfitLoss = rand.nextInt(100) - 100
      val minTradedVolume = rand.nextInt(1000)
      val trader = PricePriceSlopeTrader("trader" + nextTraderId, backPriceSlopeSignal, layPriceSlopeSignal, maxPrice,maxNumOfRunners,minProfitLoss,minTradedVolume)
      trader
    }
    val bestSolution = CoevolutionHillClimbing.optimise(marketDataDir, baseTrader, mutate, populationSize, generationNum)

    log.info("Best solution=" + bestSolution)
  }

}