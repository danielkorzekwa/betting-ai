package dk.bettingai.trader.fsm.simple2

import org.junit._
import Assert._

class Simple2FsmTraderTest {

	@Test def test {
		val traderClass = classOf[Simple2FsmTrader]
		val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName,"marketDataDir=./src/test/resources/five_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}