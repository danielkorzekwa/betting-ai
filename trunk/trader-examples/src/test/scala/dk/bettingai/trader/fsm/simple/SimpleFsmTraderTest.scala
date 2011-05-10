package dk.bettingai.trader.fsm.simple

import org.junit._
import Assert._

class SimpleFsmTraderTest {

	@Test def test {
		val traderClass = classOf[SimpleFsmTrader]
		val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName,"marketDataDir=./src/test/resources/two_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}