package dk.bettingai.trader.pricepriceslope

import org.junit._
import Assert._
import org.slf4j.LoggerFactory
import java.util.Random
import java.io.File
import dk.bettingai.tradingoptimiser._
import dk.bettingai.marketsimulator.betex.PriceUtil._
import HillClimbing._
import CoevolutionHillClimbing._
import dk.bettingai.marketsimulator.ISimulator._

/**
 * Run trader implementation.
 *
 * @author korzekwad
 *
 * Set more memory and active jmx:
 * -Xmx512m
 * -Dcom.sun.management.jmxremote
 *
 *  bestSoFar=Solution [trader=PriceSlopeTrader [id=trader4, backSlope=-0.01, laySlope=-0.02, maxPrice=2.84], expectedProfit=181.32852996310174, matchedBetsNum=3253.0]
 * 575 markets, 04/04/2011 14:43:22 INFO  dk.bettingai.tradingoptimiser.CoevolutionHillClimbing - Iter number=11, bestSoFar=Solution [trader=PricePriceSlopeTrader [id=trader42, backSlope=0.034, laySlope=-0.032, maxPrice=19.0,mxNumOfRunners=16,minProfitLoss=-1.0,minTradedVolume=33.0], expectedProfit=854.6538638347496, matchedBetsNum=214527.0], currentBest=Solution [trader=PricePriceSlopeTrader [id=trader52, backSlope=0.035, laySlope=-0.036000000000000004, maxPrice=16.5,mxNumOfRunners=16,minProfitLoss=-1.0,minTradedVolume=33.0], expectedProfit=850.734709829258, matchedBetsNum=213433.0]
 * 575 markets, 04/04/2011 14:53:27 INFO  dk.bettingai.tradingoptimiser.CoevolutionHillClimbing - Iter number=15, bestSoFar=Solution [trader=PricePriceSlopeTrader [id=trader72, backSlope=0.039, laySlope=-0.03799999999999999, maxPrice=18.0,mxNumOfRunners=16,minProfitLoss=-1.0,minTradedVolume=33.0], expectedProfit=892.4477424653546, matchedBetsNum=217883.0], currentBest=Solution [trader=PricePriceSlopeTrader [id=trader72, backSlope=0.039, laySlope=-0.03799999999999999, maxPrice=18.0,mxNumOfRunners=16,minProfitLoss=-1.0,minTradedVolume=33.0], expectedProfit=892.4477424653546, matchedBetsNum=217883.0]
 * 575 markets, 04/04/2011 15:58:24 INFO  dk.bettingai.tradingoptimiser.CoevolutionHillClimbing - Iter number=16, bestSoFar=Solution [trader=PricePriceSlopeTrader [id=trader78, backSlope=0.025, laySlope=-0.035, maxPrice=17.0,mxNumOfRunners=15,minProfitLoss=-1.0,minTradedVolume=-4.0], expectedProfit=1388.7164063415646, matchedBetsNum=373276.0], currentBest=Solution [trader=PricePriceSlopeTrader [id=trader78, backSlope=0.025, laySlope=-0.035, maxPrice=17.0,mxNumOfRunners=15,minProfitLoss=-1.0,minTradedVolume=-4.0], expectedProfit=1388.7164063415646, matchedBetsNum=373276.0]
 * 575 markets, 04/04/2011 16:54:08 INFO  dk.bettingai.tradingoptimiser.CoevolutionHillClimbing - Iter number=27, bestSoFar=Solution [trader=PricePriceSlopeTrader [id=trader82, backSlope=0.021, laySlope=-0.037000000000000005, maxPrice=18.5,mxNumOfRunners=16,minProfitLoss=-3.0,minTradedVolume=-1.0], expectedProfit=1708.2890660279404, matchedBetsNum=460422.0], currentBest=Solution [trader=PricePriceSlopeTrader [id=trader134, backSlope=0.025, laySlope=-0.035, maxPrice=19.5,mxNumOfRunners=17,minProfitLoss=-2.0,minTradedVolume=-9.0], expectedProfit=1274.7823365350766, matchedBetsNum=430224.0]
 *
 */
class PricePriceSlopeTraderTest {

  private val log = LoggerFactory.getLogger(getClass)

  /**Number of matched bets for this trader will be zero, it's to test escaping from local maximum.*/
  val trader = PricePriceSlopeTrader("baseTrader", 0.02, -0.03, 9.6d, 16, -1d, 33d)

  private val populationSize = 5
  private val generationNum = 5

  private val rand = new Random(System.currentTimeMillis)

  @Test
  def testTwoMarkets {

    log.info("Initial trader=" + trader)

    var lastTraderId = 1
    def nextTraderId = { lastTraderId += 1; lastTraderId }

    val bank = 1000
    // val marketData = MarketData("c:/daniel/marketdataall")
    val marketData = MarketData("./src/test/resources/two_hr_10mins_before_inplay")

    val mutate = (solution: Solution[PricePriceSlopeTrader]) => {
      val backPriceSlopeSignal = solution.trader.backPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
      val layPriceSlopeSignal = solution.trader.layPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
      val maxPrice = move(solution.trader.maxPrice, rand.nextInt(11) - 5)
      val maxNumOfWinners = solution.trader.maxNumOfRunners + rand.nextInt(3) - 1
      val minProfitLoss = solution.trader.minProfitLoss + rand.nextInt(10) - 5
      val minTradedVolume = solution.trader.minTradedVolume + rand.nextInt(20) - 10
    
       new TraderFactory[PricePriceSlopeTrader]() {
    	  def create() =    PricePriceSlopeTrader("trader" + nextTraderId, backPriceSlopeSignal, layPriceSlopeSignal, maxPrice, maxNumOfWinners, minProfitLoss, minTradedVolume)
      } 
      //solution.trader
    }
    val bestSolution = CoevolutionHillClimbing(marketData, mutate, populationSize, bank).optimise(trader, generationNum)

    log.info("Best solution=" + bestSolution)
  }

}