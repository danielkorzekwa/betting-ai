package dk.bettingai.trader.ifwinlosedelta

import org.junit._
import Assert._

class IfwinlosedeltaTraderTest {

	@Test def test {
		val traderClass = classOf[IfwinlosedeltaTrader]
		val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName,"marketDataDir=./src/test/resources/two_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}