package dk.bettingai.trader.stat.s1

import org.junit._
import Assert._

/**Run trader implementation.
 * 
 * @author korzekwad
 *
 */
class S1TraderTest {

	@Test def test {
		val traderClass = classOf[S1Trader]
		val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName,"marketDataDir=./src/test/resources/two_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}