package dk.bettingai.trader.wealthtest

import org.junit._
import Assert._

class WealthTestTraderTest {

	@Test def test {
		val traderClass = classOf[WealthTestTrader]
		val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName,"marketDataDir=./src/test/resources/two_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}