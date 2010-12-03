package dk.bettingai.trader.tradedvolumedeltashare

import org.junit._
import Assert._

/**Run trader implementation.
 * 
 * @author korzekwad
 *
 */
class TradedVolumeDeltaShareTraderTest {

	@Test def test {
		val traderClass = classOf[TradedVolumeDeltaShareTrader]
		val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName,"marketDataDir=./src/test/resources/two_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}