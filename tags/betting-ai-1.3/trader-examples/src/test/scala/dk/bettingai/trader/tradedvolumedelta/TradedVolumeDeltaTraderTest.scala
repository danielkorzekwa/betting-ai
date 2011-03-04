package dk.bettingai.trader.tradedvolumedelta

import org.junit._
import Assert._

/**Run trader implementation.
 * 
 * @author korzekwad
 *
 */
class TradedVolumeDeltaTraderTest {

	@Test def test {
		val traderClass = classOf[TradedVolumeDeltaTrader]
		val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName,"marketDataDir=./src/test/resources/two_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}