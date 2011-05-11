package dk.bettingai.trader.fsm.simpledelayed

import org.junit._
import Assert._

class SimpleDelayedFsmTraderTest {

	@Test def test {
		val traderClass = classOf[SimpleDelayedFsmTrader]
		val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName,"marketDataDir=./src/test/resources/five_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}