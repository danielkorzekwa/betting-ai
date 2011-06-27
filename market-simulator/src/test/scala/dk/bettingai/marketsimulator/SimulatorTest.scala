package dk.bettingai.marketsimulator

import org.junit._
import Assert._
import scala.io._
import java.io._
import dk.betex.api._
import dk.betex._
import dk.betex.eventcollector.eventprocessor._
import dk.bettingai.marketsimulator.trader._
import examples._
import dk.betex.Market._
import java.util.Date
import scala.collection._
import ISimulator._
import SimulatorTest._
import immutable.TreeMap

object SimulatorTest {
  def assertMarketReport(marketId: Long, marketName: String, eventName: String, actualMarketReport: MarketReport) {
    assertEquals(marketId, actualMarketReport.marketId)
    assertEquals(marketName, actualMarketReport.marketName)
    assertEquals(eventName, actualMarketReport.eventName)
  }
  def assertTraderReport(expectedProfit: Double, matchedBetsNum: Int, unmatchedBetsNumber: Int, actualTraderReport: TraderReport) {
    assertEquals(expectedProfit, actualTraderReport.marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(matchedBetsNum, actualTraderReport.matchedBetsNumber, 0)
    assertEquals(unmatchedBetsNumber, actualTraderReport.unmatchedBetsNumber, 0)
  }
}

class SimulatorTest {

  private val betex = new Betex()
  private val traderFactory = new ISimulator.TraderFactory[ITrader] {
    def create() = new SimpleTrader()
  }
  private val traders = traderFactory :: Nil
  private val simulator = new Simulator(betex, 0, 1000)

  /**
   * Test scenarios for runSimulation - analysing single trader only.
   */

  @Test
  def testNoMarketEvents {
    val marketEventsFile = new File("src/test/resources/marketDataEmpty/10.csv")

    /**Run market simulation.*/
    val marketRiskReport = simulator.runSimulation(TreeMap(11l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(0, marketRiskReport.size)
  }

  @Test
  def testCreateMarketEventOnly {
    val marketEventsFile = new File("src/test/resources/marketDataCreateMarketOnly/10.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(TreeMap(10l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(1, marketReports.size)
    assertMarketReport(10, "Match Odds", "Man Utd vs Arsenal", marketReports(0))

    assertEquals(1, marketReports(0).traderReports.size)
    assertTraderReport(0, 0, 0, marketReports(0).traderReports(0))
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(0, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0)
    assertEquals(0, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0)

    assertEquals(12345, marketReports(0).traderReports(0).trader.trader.asInstanceOf[SimpleTrader].initTimestamp)
  }

  @Test
  def testOneMatchedBackBetNegativeExpectedProfit {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceLayBet/10.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(TreeMap(10l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(1, marketReports.size)
    assertMarketReport(10, "Match Odds", "Man Utd vs Arsenal", marketReports(0))

    assertEquals(1, marketReports(0).traderReports.size)
    assertTraderReport(-0.625, 1, 1, marketReports(0).traderReports(0))
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(2.4, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(-2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0)

    assertEquals(RegisteredTrader(3, marketReports(0).traderReports(0).trader.trader), marketReports(0).traderReports(0).trader)
  }

  @Test
  def testOneMatchedLayBetPositiveExpectedProfit {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceBackBet/10.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(TreeMap(10l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(1, marketReports.size)
    assertMarketReport(10, "Match Odds", "Man Utd vs Arsenal", marketReports(0))

    assertEquals(1, marketReports(0).traderReports.size)
    assertTraderReport(0.199, 1, 1, marketReports(0).traderReports(0))
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(-1.6, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0)
  }

  @Test
  def testThreeMatchedBackBetsNegativeExpectedProfit {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceAndCancelLayBet/10.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(TreeMap(10l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(1, marketReports.size)
    assertMarketReport(10, "Match Odds", "Man Utd vs Arsenal", marketReports(0))

    assertEquals(1, marketReports(0).traderReports.size)
    assertTraderReport(-0.607, 3, 1, marketReports(0).traderReports(0))
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(2.4, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(-2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0)

  }

  @Test
  def testOneMatchedBetAFewMatchedBets {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceAFewBets/10.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(TreeMap(10l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(1, marketReports.size)
    assertMarketReport(10, "Match Odds", "Man Utd vs Arsenal", marketReports(0))

    assertEquals(1, marketReports(0).traderReports.size)
    assertTraderReport(-1.066, 7, 7, marketReports(0).traderReports(0))
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(15.6, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(-14.6, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0.001)

    assertEquals(1, marketReports(0).traderReports(0).trader.trader.asInstanceOf[SimpleTrader].initCalledTimes.get)
    assertEquals(1, marketReports(0).traderReports(0).trader.trader.asInstanceOf[SimpleTrader].afterCalledTimes.get)
  }

  @Test
  def testOneMatchedBetsOnTwoMarkets {
    val marketEventsFile10 = new File("src/test/resources/twoMarketFiles/10.csv")
    val marketEventsFile20 = new File("src/test/resources/twoMarketFiles/20.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(TreeMap(10l -> marketEventsFile10, 20l -> marketEventsFile20), traders, (progress: Int) => {}).marketReports

    assertEquals(2, marketReports.size)

    /**Check report for the first market.*/
    assertMarketReport(10, "Match Odds", "Man Utd vs Arsenal", marketReports(0))
    assertEquals(1, marketReports(0).traderReports.size)
    assertTraderReport(1.058, 2, 2, marketReports(0).traderReports(0))

    /**Check report for the second market.*/
    assertMarketReport(20, "Match Odds", "Fulham vs Wigan", marketReports(1))
    assertEquals(1, marketReports(1).traderReports.size)
    assertTraderReport(0.6, 1, 1, marketReports(1).traderReports(0))

    assertEquals(1, marketReports(0).traderReports(0).trader.trader.asInstanceOf[SimpleTrader].initCalledTimes.get)
    assertEquals(1, marketReports(0).traderReports(0).trader.trader.asInstanceOf[SimpleTrader].afterCalledTimes.get)
    assertEquals(1, marketReports(1).traderReports(0).trader.trader.asInstanceOf[SimpleTrader].initCalledTimes.get)
    assertEquals(1, marketReports(1).traderReports(0).trader.trader.asInstanceOf[SimpleTrader].afterCalledTimes.get)

  }

  @Test
  def testTraderWithTradingChildren {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceLayBet/10.csv")
    val traderFactory = new ISimulator.TraderFactory[ITrader] {
      def create() = new SimpleTraderWithChildren()
    }
    val traderUnderTest = traderFactory :: Nil
    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(TreeMap(10l -> marketEventsFile), traderUnderTest, (progress: Int) => {}).marketReports

    assertEquals(-0.625, marketReports(0).traderReports(0).trader.trader.asInstanceOf[SimpleTraderWithChildren].getTotalMarketExpectedProfit, 0.001)

    /**All the risk numbers below are equal to zero because master trader is not placing any bets, it's just creating child traders to do so.*/
    assertEquals(1, marketReports.size)
    assertMarketReport(10, "Match Odds", "Man Utd vs Arsenal", marketReports(0))

    assertEquals(1, marketReports(0).traderReports.size)
    assertTraderReport(0, 0, 0, marketReports(0).traderReports(0))
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(0, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0)
    assertEquals(0, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0)
  }

  /**It's testing market-slave parallel market simulation. Throwing exception by a trader should not block market simulation.*/
  @Test
  def testTraderThrowingException {
    val marketEventsFile10 = new File("src/test/resources/twoMarketFiles/10.csv")
    val marketEventsFile20 = new File("src/test/resources/twoMarketFiles/20.csv")

    val traderFactory = new ISimulator.TraderFactory[ITrader] {
      def create() = new ITrader {
        def execute(ctx: ITraderContext) = throw new RuntimeException("Trader error - it's just for testing purpose.")
      }
    }

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(TreeMap(10l -> marketEventsFile10, 20l -> marketEventsFile20), traderFactory :: Nil, (progress: Int) => {}).marketReports

    assertEquals(0, marketReports.size)
  }

  /**
   * Test scenarios for runSimulation - analysing multiple traders.
   */
  @Test
  def testTwoTradersOneMatchedBetsOnTwoMarkets {
    val marketEventsFile10 = new File("src/test/resources/twoMarketFiles/10.csv")
    val marketEventsFile20 = new File("src/test/resources/twoMarketFiles/20.csv")

    val traderFactory = new ISimulator.TraderFactory[ITrader] {
      def create() = new SimpleTrader()
    }

    val twoTraders = traderFactory :: traderFactory :: Nil
    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(TreeMap(10l -> marketEventsFile10, 20l -> marketEventsFile20), twoTraders, (progress: Int) => {}).marketReports

    assertEquals(2, marketReports.size)

    /**Check report for the first market.*/

    val marketReport1 = marketReports(0)
    assertMarketReport(10, "Match Odds", "Man Utd vs Arsenal", marketReport1)

    assertEquals(2, marketReport1.traderReports.size)
    assertTraderReport(1.058, 2, 2, marketReport1.traderReports(0))
    assertEquals(RegisteredTrader(3, marketReports(0).traderReports(0).trader.trader), marketReport1.traderReports(0).trader)
    assertTraderReport(1.058, 2, 2, marketReport1.traderReports(1))

    assertEquals(RegisteredTrader(4, marketReports(0).traderReports(1).trader.trader), marketReport1.traderReports(1).trader)
    assertEquals(1, marketReports(0).traderReports(0).trader.trader.asInstanceOf[SimpleTrader].initCalledTimes.get)
    assertEquals(1, marketReports(0).traderReports(0).trader.trader.asInstanceOf[SimpleTrader].afterCalledTimes.get)
    assertEquals(1, marketReports(1).traderReports(0).trader.trader.asInstanceOf[SimpleTrader].initCalledTimes.get)
    assertEquals(1, marketReports(1).traderReports(0).trader.trader.asInstanceOf[SimpleTrader].afterCalledTimes.get)

    /**Check report for the second market.*/
    val marketReport2 = marketReports(1)
    assertMarketReport(20, "Match Odds", "Fulham vs Wigan", marketReport2)

    assertEquals(2, marketReport2.traderReports.size)
    assertTraderReport(0.6, 1, 1, marketReport2.traderReports(0))
    assertEquals(RegisteredTrader(3, marketReports(1).traderReports(0).trader.trader), marketReport2.traderReports(0).trader)
    assertTraderReport(0.6, 1, 1, marketReport2.traderReports(1))
    assertEquals(RegisteredTrader(4, marketReports(1).traderReports(1).trader.trader), marketReport2.traderReports(1).trader)
    
    assertEquals(1, marketReports(0).traderReports(1).trader.trader.asInstanceOf[SimpleTrader].initCalledTimes.get)
    assertEquals(1, marketReports(0).traderReports(1).trader.trader.asInstanceOf[SimpleTrader].afterCalledTimes.get)
    assertEquals(1, marketReports(1).traderReports(1).trader.trader.asInstanceOf[SimpleTrader].initCalledTimes.get)
    assertEquals(1, marketReports(1).traderReports(1).trader.trader.asInstanceOf[SimpleTrader].afterCalledTimes.get)
  }

  @Test
  def testCheckProgressBar {

    val marketEventsFile10 = new File("src/test/resources/twoMarketFiles/10.csv")
    val marketEventsFile20 = new File("src/test/resources/twoMarketFiles/20.csv")
    val empyMarketFile1 = new File("src/test/resources/marketDataEmpty/10.csv")

    val traderFactory = new ISimulator.TraderFactory[ITrader] {
      def create() = new SimpleTrader()
    }

    val twoTraders = traderFactory :: traderFactory :: Nil

    var progressSum = 0l
    val progressBar = (progress: Int) => { progressSum += progress; progressSum += progressSum * 2; println("market simulation progress=" + progress + "%") }
    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(
      TreeMap(1l -> empyMarketFile1,
        2l -> empyMarketFile1,
        3l -> empyMarketFile1,
        4l -> empyMarketFile1,
        10l -> marketEventsFile10,
        20l -> marketEventsFile20), twoTraders, progressBar).marketReports

    assertEquals(2, marketReports.size)
    assertEquals(26562, progressSum)
  }

  /**
   * Test scenarios for registerTrader.
   */

  @Test
  def testRegisterTrader {
    val runners = new Runner(1, "runner 1") :: new Runner(2, "runner 2") :: Nil
    val market = betex.createMarket(1, "market1", "event name", 1, new Date(0), runners)

    val traderCtx1 = simulator.registerTrader(market, null)
    assertEquals(3, traderCtx1.userId)

    val traderCtx2 = simulator.registerTrader(market, null)
    assertEquals(4, traderCtx2.userId)
  }

}