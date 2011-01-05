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

class SimulatorIntegrationTest {

  private val betex = new Betex()
  private val marketEventProcessor = new MarketEventProcessorImpl(betex)
  private val trader = new SimpleTrader()
  private val simulator = new Simulator(marketEventProcessor, betex, 0)

  /**
   * Test scenarios for runSimulation.
   * */

  @Test
  def testNoMarketEvents {
    val marketEventsFile = new File("src/test/resources/marketDataEmpty/10.csv")

    /**Run market simulation.*/
    val marketRiskReport = simulator.runSimulation(Map(11l -> marketEventsFile), trader, p => {})
    assertEquals(0, marketRiskReport.size)
  }

  @Test
  def testCreateMarketEventOnly {
    val marketEventsFile = new File("src/test/resources/marketDataCreateMarketOnly/10.csv")

    /**Run market simulation.*/
    val marketRiskReport = simulator.runSimulation(Map(10l -> marketEventsFile), trader, p => {})
    assertEquals(1, marketRiskReport.size)

    assertEquals(10, marketRiskReport(0).marketId)
    assertEquals("Match Odds", marketRiskReport(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketRiskReport(0).eventName)

    assertEquals(0, marketRiskReport(0).marketExpectedProfit.marketExpectedProfit, 0)
    assertEquals(2, marketRiskReport(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(0, marketRiskReport(0).marketExpectedProfit.runnersIfWin(11), 0)
    assertEquals(0, marketRiskReport(0).marketExpectedProfit.runnersIfWin(12), 0)

    assertEquals(0, marketRiskReport(0).matchedBetsNumber, 0)
    assertEquals(0, marketRiskReport(0).unmatchedBetsNumber, 0)
    
    assertEquals(12345, trader.initTimestamp)
  }

  @Test
  def testOneMatchedBackBetNegativeExpectedProfit {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceLayBet/10.csv")

    /**Run market simulation.*/
    val marketRiskReport = simulator.runSimulation(Map(10l -> marketEventsFile), trader, p => {})
    assertEquals(1, marketRiskReport.size)

    assertEquals(10, marketRiskReport(0).marketId)
    assertEquals("Match Odds", marketRiskReport(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketRiskReport(0).eventName)

    assertEquals(-0.625, marketRiskReport(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketRiskReport(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(2.4, marketRiskReport(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(-2, marketRiskReport(0).marketExpectedProfit.runnersIfWin(12), 0)

    assertEquals(1, marketRiskReport(0).matchedBetsNumber, 0)
    assertEquals(1, marketRiskReport(0).unmatchedBetsNumber, 0)
  }

  @Test
  def testOneMatchedLayBetPositiveExpectedProfit {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceBackBet/10.csv")

    /**Run market simulation.*/
    val marketRiskReport = simulator.runSimulation(Map(10l -> marketEventsFile), trader, p => {})
    assertEquals(1, marketRiskReport.size)

    assertEquals(10, marketRiskReport(0).marketId)
    assertEquals("Match Odds", marketRiskReport(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketRiskReport(0).eventName)

    assertEquals(0.199, marketRiskReport(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketRiskReport(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(-1.6, marketRiskReport(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(2, marketRiskReport(0).marketExpectedProfit.runnersIfWin(12), 0)

    assertEquals(1, marketRiskReport(0).matchedBetsNumber, 0)
    assertEquals(1, marketRiskReport(0).unmatchedBetsNumber, 0)
  }

  @Test
  def testThreeMatchedBackBetsNegativeExpectedProfit {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceAndCancelLayBet/10.csv")

    /**Run market simulation.*/
    val marketRiskReport = simulator.runSimulation(Map(10l -> marketEventsFile), trader, p => {})
    assertEquals(1, marketRiskReport.size)

    assertEquals(10, marketRiskReport(0).marketId)
    assertEquals("Match Odds", marketRiskReport(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketRiskReport(0).eventName)

    assertEquals(-0.607, marketRiskReport(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketRiskReport(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(2.4, marketRiskReport(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(-2, marketRiskReport(0).marketExpectedProfit.runnersIfWin(12), 0)

    assertEquals(3, marketRiskReport(0).matchedBetsNumber, 0)
    assertEquals(1, marketRiskReport(0).unmatchedBetsNumber, 0)

  }

  @Test
  def testOneMatchedBetAFewMatchedBets {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceAFewBets/10.csv")

    /**Run market simulation.*/
    val marketRiskReport = simulator.runSimulation(Map(10l -> marketEventsFile), trader, p => {})
    assertEquals(1, marketRiskReport.size)

    assertEquals(10, marketRiskReport(0).marketId)
    assertEquals("Match Odds", marketRiskReport(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketRiskReport(0).eventName)

    assertEquals(-1.066, marketRiskReport(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketRiskReport(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(15.6, marketRiskReport(0).marketExpectedProfit.runnersIfWin(11), 0.001)
    assertEquals(-14.6, marketRiskReport(0).marketExpectedProfit.runnersIfWin(12), 0.001)

    assertEquals(7, marketRiskReport(0).matchedBetsNumber, 0)
    assertEquals(7, marketRiskReport(0).unmatchedBetsNumber, 0)

    assertEquals(1, trader.initCalledTimes)
    assertEquals(1, trader.afterCalledTimes)
  }

  @Test
  def testOneMatchedBetsOnTwoMarkets {
    val marketEventsFile10 = new File("src/test/resources/twoMarketFiles/10.csv")
    val marketEventsFile20 = new File("src/test/resources/twoMarketFiles/20.csv")

    /**Run market simulation.*/
    val marketRiskReport = simulator.runSimulation(Map(10l -> marketEventsFile10, 20l -> marketEventsFile20), trader, p => {})
    assertEquals(2, marketRiskReport.size)

    assertEquals(20, marketRiskReport(0).marketId)
    assertEquals("Match Odds", marketRiskReport(0).marketName)
    assertEquals("Fulham vs Wigan", marketRiskReport(0).eventName)
    assertEquals(0.6, marketRiskReport(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(1, marketRiskReport(0).matchedBetsNumber, 0)
    assertEquals(1, marketRiskReport(0).unmatchedBetsNumber, 0)

    assertEquals(10, marketRiskReport(1).marketId)
    assertEquals("Match Odds", marketRiskReport(1).marketName)
    assertEquals("Man Utd vs Arsenal", marketRiskReport(1).eventName)
    assertEquals(1.058, marketRiskReport(1).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(2, marketRiskReport(1).matchedBetsNumber, 0)
    assertEquals(2, marketRiskReport(1).unmatchedBetsNumber, 0)

    assertEquals(2, trader.initCalledTimes)
    assertEquals(2, trader.afterCalledTimes)
  }

  @Test
  def testTraderWithTradingChildren {
    val marketEventsFile = new File("src/test/resources/marketDataPlaceLayBet/10.csv")
    val traderUnderTest = new SimpleTraderWithChildren()
    /**Run market simulation.*/
    val marketRiskReport = simulator.runSimulation(Map(10l -> marketEventsFile), traderUnderTest, p => {})

    assertEquals(-0.625, traderUnderTest.getTotalMarketExpectedProfit, 0.001)

    /**All the risk numbers below are equal to zero because master trader is not placing any bets, it's just creating child traders to do so.*/
    assertEquals(1, marketRiskReport.size)
    assertEquals(10, marketRiskReport(0).marketId)
    assertEquals("Match Odds", marketRiskReport(0).marketName)
    assertEquals("Man Utd vs Arsenal", marketRiskReport(0).eventName)

    assertEquals(0, marketRiskReport(0).marketExpectedProfit.marketExpectedProfit, 0)
    assertEquals(2, marketRiskReport(0).marketExpectedProfit.runnersIfWin.size, 0)
    assertEquals(0, marketRiskReport(0).marketExpectedProfit.runnersIfWin(11), 0)
    assertEquals(0, marketRiskReport(0).marketExpectedProfit.runnersIfWin(12), 0)

    assertEquals(0, marketRiskReport(0).matchedBetsNumber, 0)
    assertEquals(0, marketRiskReport(0).unmatchedBetsNumber, 0)

  }
  /**
   * Test scenarios for registerTrader.
   * */

  @Test
  def testRegisterTrader {
    val runners = new Runner(1, "runner 1") :: new Runner(2, "runner 2") :: Nil
    val market = betex.createMarket(1, "market1", "event name", 1, new Date(0), runners)

    val traderCtx1 = simulator.registerTrader(market)
    assertEquals(4, traderCtx1.userId)

    val traderCtx2 = simulator.registerTrader(market)
    assertEquals(5, traderCtx2.userId)
  }
}