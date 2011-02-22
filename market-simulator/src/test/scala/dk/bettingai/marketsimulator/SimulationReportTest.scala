package dk.bettingai.marketsimulator

import org.junit._
import Assert._
import ISimulator._
import dk.bettingai.marketsimulator.trader.SimpleTrader
import dk.bettingai.marketsimulator.risk.MarketExpectedProfit
class SimulationReportTest {

  private var simulationReport: SimulationReport = _

  @Before
  def setUp {
    val marketExpectedProfit1 = new MarketExpectedProfit(34, Map(), Map())
    val traderReports1 = TraderReport(RegisteredTrader(100, new SimpleTrader()), marketExpectedProfit1, 3, 6, Nil, Nil) :: Nil
    val marketReport1 = MarketReport(1, "market 2", "event", traderReports1)

    val marketExpectedProfit2 = new MarketExpectedProfit(24, Map(), Map())
    val traderReports2 = TraderReport(RegisteredTrader(100, new SimpleTrader()), marketExpectedProfit2, 2, 5, Nil, Nil) :: Nil
    val marketReport2 = MarketReport(1, "market 1", "event", traderReports2)
    
    val marketReports = marketReport1 :: marketReport2 :: Nil
    simulationReport = SimulationReport(marketReports)
  }

  @Test
  def testTotalExpectedProfit {
    assertEquals(58, simulationReport.totalExpectedProfit(100), 0)
  }
  @Test(expected = classOf[NoSuchElementException])
  def testTotalExpectedProfitUserNotFound {
    simulationReport.totalExpectedProfit(-999)
  }

  @Test
  def testTotalMatchedBetsNum {
    assertEquals(5, simulationReport.totalMatchedBetsNum(100), 0)
  }
  @Test(expected = classOf[NoSuchElementException])
  def testTotalMatchedBetsNumUserNotFound {
    simulationReport.totalMatchedBetsNum(-999)
  }

  @Test
  def testTotalUnmatchedBetsNum {
    assertEquals(11, simulationReport.totalUnmatchedBetsNum(100), 0)
  }
  @Test(expected = classOf[NoSuchElementException])
  def testTotalUnmatchedBetsNumUserNotFound {
    simulationReport.totalUnmatchedBetsNum(-999)

  }
}