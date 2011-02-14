package dk.bettingai.marketsimulator

import org.junit._
import Assert._
import scala.io._
import java.io._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.betex.Market._
import java.util.Date
import scala.collection._

class SimulatorIntegrationTest {

  private val betex = new Betex()
  private val marketEventProcessor = new MarketEventProcessorImpl(betex)
  private val traders = new SimpleTrader() :: Nil
  private val simulator = new Simulator(marketEventProcessor, betex, 0)

  /**
   * Test scenarios for runSimulation - analysing single trader only.
   * */

  @Test
  def testNoMarketEvents {
    val marketEventsFile = new File("src/test/resources/marketDataEmpty/10.csv")

    /**Run market simulation.*/
    val marketRiskReport = simulator.runSimulation(Map(11l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(0, marketRiskReport.size)
  }

  @Test
  def testCreateMarketEventOnly {
    val marketEventsFile = new File("src/test/resources/marketDataCreateMarketOnly/10.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(Map(10l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(1, marketReports.size)

    assertEquals(10, marketReports(0).marketId)
    assertEquals("Match Odds", marketReports(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketReports(0).eventName)

    assertEquals(1, marketReports(0).traderReports.size)
    assertEquals(0, marketReports(0).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0)
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(0, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0)
    assertEquals(0, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0)
    assertEquals(0, marketReports(0).traderReports(0).matchedBetsNumber, 0)
    assertEquals(0, marketReports(0).traderReports(0).unmatchedBetsNumber, 0)

    assertEquals(12345, traders.head.initTimestamp)
  }

  @Test
  def testOneMatchedBackBetNegativeExpectedProfit {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceLayBet/10.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(Map(10l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(1, marketReports.size)

    assertEquals(10, marketReports(0).marketId)
    assertEquals("Match Odds", marketReports(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketReports(0).eventName)

    assertEquals(1, marketReports(0).traderReports.size)
    assertEquals(-0.625, marketReports(0).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(2.4, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(-2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0)

    assertEquals(1, marketReports(0).traderReports(0).matchedBetsNumber, 0)
    assertEquals(1, marketReports(0).traderReports(0).unmatchedBetsNumber, 0)
  }

  @Test
  def testOneMatchedLayBetPositiveExpectedProfit {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceBackBet/10.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(Map(10l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(1, marketReports.size)

    assertEquals(10, marketReports(0).marketId)
    assertEquals("Match Odds", marketReports(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketReports(0).eventName)

    assertEquals(1, marketReports(0).traderReports.size)
    assertEquals(0.199, marketReports(0).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(-1.6, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0)
    assertEquals(1, marketReports(0).traderReports(0).matchedBetsNumber, 0)
    assertEquals(1, marketReports(0).traderReports(0).unmatchedBetsNumber, 0)
  }

  @Test
  def testThreeMatchedBackBetsNegativeExpectedProfit {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceAndCancelLayBet/10.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(Map(10l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(1, marketReports.size)

    assertEquals(10, marketReports(0).marketId)
    assertEquals("Match Odds", marketReports(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketReports(0).eventName)

    assertEquals(1, marketReports(0).traderReports.size)
    assertEquals(-0.607, marketReports(0).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(2.4, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(-2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0)
    assertEquals(3, marketReports(0).traderReports(0).matchedBetsNumber, 0)
    assertEquals(1, marketReports(0).traderReports(0).unmatchedBetsNumber, 0)

  }

  @Test
  def testOneMatchedBetAFewMatchedBets {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceAFewBets/10.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(Map(10l -> marketEventsFile), traders, (progress: Int) => {}).marketReports
    assertEquals(1, marketReports.size)

    assertEquals(10, marketReports(0).marketId)
    assertEquals("Match Odds", marketReports(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketReports(0).eventName)

    assertEquals(1, marketReports(0).traderReports.size)
    assertEquals(-1.066, marketReports(0).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(15.6, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(-14.6, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0.001)
    assertEquals(7, marketReports(0).traderReports(0).matchedBetsNumber, 0)
    assertEquals(7, marketReports(0).traderReports(0).unmatchedBetsNumber, 0)

    assertEquals(1, traders.head.initCalledTimes)
    assertEquals(1, traders.head.afterCalledTimes)
  }

  @Test
  def testOneMatchedBetsOnTwoMarkets {
    val marketEventsFile10 = new File("src/test/resources/twoMarketFiles/10.csv")
    val marketEventsFile20 = new File("src/test/resources/twoMarketFiles/20.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(Map(10l -> marketEventsFile10, 20l -> marketEventsFile20), traders, (progress: Int) => {}).marketReports

    assertEquals(2, marketReports.size)

    /**Check report for the first market.*/
    assertEquals(1, marketReports(0).traderReports.size)

    assertEquals(10, marketReports(0).marketId)
    assertEquals("Match Odds", marketReports(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketReports(0).eventName)
    assertEquals(1.058, marketReports(0).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketReports(0).traderReports(0).matchedBetsNumber, 0)
    assertEquals(2, marketReports(0).traderReports(0).unmatchedBetsNumber, 0)

    /**Check report for the second market.*/
    assertEquals(1, marketReports(1).traderReports.size)
    assertEquals(20, marketReports(1).marketId)
    assertEquals("Match Odds", marketReports(1).marketName)
    assertEquals("Fulham vs Wigan", marketReports(1).eventName)
    assertEquals(0.6, marketReports(1).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(1, marketReports(1).traderReports(0).matchedBetsNumber, 0)
    assertEquals(1, marketReports(1).traderReports(0).unmatchedBetsNumber, 0)

    assertEquals(2, traders.head.initCalledTimes)
    assertEquals(2, traders.head.afterCalledTimes)

  }

  @Test
  def testOneTraderRealData {
    val marketEventsFile10 = new File("src/test/resources/marketRealDataTwoMarkets/101655610.csv")
    val marketEventsFile20 = new File("src/test/resources/marketRealDataTwoMarkets/101655622.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(Map(101655610l -> marketEventsFile10, 101655622l -> marketEventsFile20), traders, (progress: Int) => {}).marketReports

    assertEquals(2, marketReports.size)

    /**Check report for the first market.*/

    assertEquals(1, marketReports(0).traderReports.size)
    assertEquals(101655610, marketReports(0).marketId)
    assertEquals("1m Hcap", marketReports(0).marketName)
    assertEquals("/GB/Muss 22nd Aug", marketReports(0).eventName)
    assertEquals(-463.572, marketReports(0).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(12393, marketReports(0).traderReports(0).matchedBetsNumber)
    assertEquals(863, marketReports(0).traderReports(0).unmatchedBetsNumber)

    /**Check report for the second market.*/
    assertEquals(1, marketReports(1).traderReports.size)
    assertEquals(101655622, marketReports(1).marketId)
    assertEquals("1m Hcap", marketReports(1).marketName)
    assertEquals("/GB/Muss 22nd Aug - 2", marketReports(1).eventName)
    assertEquals(-194.699, marketReports(1).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(11256, marketReports(1).traderReports(0).matchedBetsNumber)
    assertEquals(672, marketReports(1).traderReports(0).unmatchedBetsNumber)

    assertEquals(2, traders.head.initCalledTimes)
    assertEquals(2, traders.head.afterCalledTimes)
  }

  @Test
  def testTraderWithTradingChildren {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceLayBet/10.csv")
    val traderUnderTest = new SimpleTraderWithChildren() :: Nil
    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(Map(10l -> marketEventsFile), traderUnderTest, (progress: Int) => {}).marketReports

    assertEquals(-0.625, traderUnderTest.head.getTotalMarketExpectedProfit, 0.001)

    /**All the risk numbers below are equal to zero because master trader is not placing any bets, it's just creating child traders to do so.*/
    assertEquals(1, marketReports.size)
    assertEquals(10, marketReports(0).marketId)
    assertEquals("Match Odds", marketReports(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketReports(0).eventName)

    assertEquals(1, marketReports(0).traderReports.size)

    assertEquals(0, marketReports(0).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0)
    assertEquals(2, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(0, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(11), 0)
    assertEquals(0, marketReports(0).traderReports(0).marketExpectedProfit.runnersIfWin(12), 0)

    assertEquals(0, marketReports(0).traderReports(0).matchedBetsNumber, 0)
    assertEquals(0, marketReports(0).traderReports(0).unmatchedBetsNumber, 0)

  }

  /**
   * Test scenarios for runSimulation - analysing multiple traders.
   * */
  @Test
  def testTwoTradersOneMatchedBetsOnTwoMarkets {
    val marketEventsFile10 = new File("src/test/resources/twoMarketFiles/10.csv")
    val marketEventsFile20 = new File("src/test/resources/twoMarketFiles/20.csv")

    val twoTraders = new SimpleTrader() :: new SimpleTrader() :: Nil
    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(Map(10l -> marketEventsFile10, 20l -> marketEventsFile20), twoTraders, (progress: Int) => {}).marketReports

    assertEquals(2, marketReports.size)

    /**Check report for the first market.*/

    val marketReport1 = marketReports(0)
    assertEquals(2, marketReport1.traderReports.size)

    assertEquals(10, marketReport1.marketId)
    assertEquals("Match Odds", marketReport1.marketName)
    assertEquals("Man Utd vs Arsenal", marketReport1.eventName)
    assertEquals(1.058, marketReport1.traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketReport1.traderReports(0).matchedBetsNumber, 0)
    assertEquals(2, marketReport1.traderReports(0).unmatchedBetsNumber, 0)

    assertEquals(1.058, marketReport1.traderReports(1).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketReport1.traderReports(1).matchedBetsNumber, 0)
    assertEquals(2, marketReport1.traderReports(1).unmatchedBetsNumber, 0)

    assertEquals(2, twoTraders(0).initCalledTimes)
    assertEquals(2, twoTraders(0).afterCalledTimes)

    /**Check report for the second market.*/
    val marketReport2 = marketReports(1)
    assertEquals(2, marketReport2.traderReports.size)

    assertEquals(20, marketReport2.marketId)
    assertEquals("Match Odds", marketReport2.marketName)
    assertEquals("Fulham vs Wigan", marketReport2.eventName)
    assertEquals(0.6, marketReport2.traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(1, marketReport2.traderReports(0).matchedBetsNumber, 0)
    assertEquals(1, marketReport2.traderReports(0).unmatchedBetsNumber, 0)
    assertEquals(0.6, marketReport2.traderReports(1).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(1, marketReport2.traderReports(1).matchedBetsNumber, 0)
    assertEquals(1, marketReport2.traderReports(1).unmatchedBetsNumber, 0)

    assertEquals(2, twoTraders(1).initCalledTimes)
    assertEquals(2, twoTraders(1).afterCalledTimes)
  }

  @Test
  def testTwoTradersRealData {
    val marketEventsFile10 = new File("src/test/resources/marketRealDataTwoMarkets/101655610.csv")
    val marketEventsFile20 = new File("src/test/resources/marketRealDataTwoMarkets/101655622.csv")

    val twoTraders = new SimpleTrader() :: new SimpleTrader() :: Nil
    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(Map(101655610l -> marketEventsFile10, 101655622l -> marketEventsFile20), twoTraders, (progress: Int) => {}).marketReports

    assertEquals(2, marketReports.size)

    /**Check report for the first market.*/

    val marketReport1 = marketReports(0)
    assertEquals(2, marketReport1.traderReports.size)

    assertEquals(101655610, marketReport1.marketId)
    assertEquals("1m Hcap", marketReport1.marketName)
    assertEquals("/GB/Muss 22nd Aug", marketReport1.eventName)
    assertEquals(-386.316, marketReport1.traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(12437, marketReport1.traderReports(0).matchedBetsNumber)
    assertEquals(719, marketReport1.traderReports(0).unmatchedBetsNumber)
    assertEquals(-402.047, marketReport1.traderReports(1).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(12379, marketReport1.traderReports(1).matchedBetsNumber)
    assertEquals(752, marketReport1.traderReports(1).unmatchedBetsNumber)

    assertEquals(2, twoTraders(0).initCalledTimes)
    assertEquals(2, twoTraders(0).afterCalledTimes)

    /**Check report for the second market.*/

    val marketReport2 = marketReports(1)
    assertEquals(2, marketReport2.traderReports.size)

    assertEquals(101655622, marketReport2.marketId)
    assertEquals("1m Hcap", marketReport2.marketName)
    assertEquals("/GB/Muss 22nd Aug - 2", marketReport2.eventName)
    assertEquals(-172.720, marketReport2.traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(11238, marketReport2.traderReports(0).matchedBetsNumber)
    assertEquals(595, marketReport2.traderReports(0).unmatchedBetsNumber)
    assertEquals(-176.796, marketReport2.traderReports(1).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(11220, marketReport2.traderReports(1).matchedBetsNumber)
    assertEquals(610, marketReport2.traderReports(1).unmatchedBetsNumber)

    assertEquals(2, twoTraders(1).initCalledTimes)
    assertEquals(2, twoTraders(1).afterCalledTimes)
  }

  /**
   * Test scenarios for registerTrader.
   * */

  @Test
  def testRegisterTrader {
    val runners = new Runner(1, "runner 1") :: new Runner(2, "runner 2") :: Nil
    val market = betex.createMarket(1, "market1", "event name", 1, new Date(0), runners)

    val traderCtx1 = simulator.registerTrader(market)
    assertEquals(3, traderCtx1.userId)

    val traderCtx2 = simulator.registerTrader(market)
    assertEquals(4, traderCtx2.userId)
  }
}