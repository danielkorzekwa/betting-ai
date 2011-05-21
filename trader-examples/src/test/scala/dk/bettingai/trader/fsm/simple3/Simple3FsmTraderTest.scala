package dk.bettingai.trader.fsm.simple3

import org.junit._
import Assert._

class Simple3FsmTraderTest {

  @Test
  def test {
    val traderClass = classOf[Simple3FsmTrader]
    val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName, "marketDataDir=./src/test/resources/one_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
    dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)
  }
}