package dk.bettingai.trader.tradedvolumedeltaprim

import org.junit._
import Assert._

/**Run trader implementation.
 * 
 * @author korzekwad
 *
 */
class TradedVolumeDeltaPrimTraderTest {

	@Test def test {
		val traderClass = classOf[TradedVolumeDeltaPrimTrader]
		val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName,"marketDataDir=./src/test/resources/two_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}