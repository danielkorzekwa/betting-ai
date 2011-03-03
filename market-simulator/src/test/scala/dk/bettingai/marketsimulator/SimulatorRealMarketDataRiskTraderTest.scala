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
import immutable.TreeMap
import ISimulator._
import SimulatorTest._

class SimulatorRealMarketDataRiskTraderTest {

  private val traders = new SimpleRiskTrader() :: Nil
  private val simulator = Simulator(0)

  /**
   * Test scenarios for runSimulation - analysing single trader only.
   * */
  @Test
  def testOneTraderRealData {
    val marketEventsFile10 = new File("src/test/resources/marketRealDataTwoMarkets/101655610.csv")
    val marketEventsFile20 = new File("src/test/resources/marketRealDataTwoMarkets/101655622.csv")

    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(TreeMap(101655610l -> marketEventsFile10, 101655622l -> marketEventsFile20), traders, (progress: Int) => {}).marketReports

    assertEquals(2, marketReports.size)

    /**Check report for the first market.*/
    assertEquals(1, marketReports(0).traderReports.size)
    assertMarketReport(101655610, "1m Hcap", "/GB/Muss 22nd Aug", marketReports(0))
    assertEquals(-154.300, marketReports(0).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(338, marketReports(0).traderReports(0).matchedBetsNumber)
    assertEquals(876, marketReports(0).traderReports(0).unmatchedBetsNumber)

    /**Check report for the second market.*/
    assertEquals(1, marketReports(1).traderReports.size)
    assertMarketReport(101655622, "1m Hcap", "/GB/Muss 22nd Aug - 2", marketReports(1))
    assertEquals(94.868, marketReports(1).traderReports(0).marketExpectedProfit.marketExpectedProfit, 0.001)
    assertEquals(428, marketReports(1).traderReports(0).matchedBetsNumber)
    assertEquals(875, marketReports(1).traderReports(0).unmatchedBetsNumber)
  }

  /**
   * Test scenarios for runSimulation - analysing multiple traders.
   * */
  @Test
  def testTwoTradersRealData {
    val marketEventsFile10 = new File("src/test/resources/marketRealDataTwoMarkets/101655610.csv")
    val marketEventsFile20 = new File("src/test/resources/marketRealDataTwoMarkets/101655622.csv")

    val twoTraders = new SimpleRiskTrader() :: new SimpleRiskTrader() :: Nil
    /**Run market simulation.*/
    val marketReports = simulator.runSimulation(TreeMap(101655610l -> marketEventsFile10, 101655622l -> marketEventsFile20), twoTraders, (progress: Int) => {}).marketReports

    assertEquals(2, marketReports.size)

    /**Check report for the first market.*/
    val marketReport1 = marketReports(0)
    assertEquals(2, marketReport1.traderReports.size)
    assertMarketReport(101655610, "1m Hcap", "/GB/Muss 22nd Aug", marketReport1)
    assertTraderReport(-159.244, 345, 927, marketReport1.traderReports(0))
    assertEquals(RegisteredTrader(3, twoTraders(0)), marketReport1.traderReports(0).trader)
    assertTraderReport(-159.253, 344, 928, marketReport1.traderReports(1))
    assertEquals(RegisteredTrader(4, twoTraders(1)), marketReport1.traderReports(1).trader)
    
    /**Check report for the second market.*/
    val marketReport2 = marketReports(1)
    assertEquals(2, marketReport2.traderReports.size)
    assertMarketReport(101655622, "1m Hcap", "/GB/Muss 22nd Aug - 2", marketReport2)
    assertTraderReport(71.442, 372, 935, marketReport2.traderReports(0))
    assertEquals(RegisteredTrader(3, twoTraders(0)), marketReport2.traderReports(0).trader)
    assertTraderReport(71.442, 363, 935, marketReport2.traderReports(1))
    assertEquals(RegisteredTrader(4, twoTraders(1)), marketReport2.traderReports(1).trader)
  }
}