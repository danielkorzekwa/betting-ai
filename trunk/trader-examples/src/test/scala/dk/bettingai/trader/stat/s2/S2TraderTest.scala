package dk.bettingai.trader.stat.s2

import org.junit._
import Assert._

/**Run trader implementation.
 * 
 * @author korzekwad
 *
 */
class S2TraderTest {

	@Test def test {
		val traderClass = classOf[S2Trader]
		val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName,"marketDataDir=./src/test/resources/one_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}