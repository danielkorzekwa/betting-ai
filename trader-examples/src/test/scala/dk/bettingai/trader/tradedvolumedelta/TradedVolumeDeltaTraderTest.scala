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
		val inputArgs = Array("marketDataDir=" + getClass.getResource("").getFile, "traderImpl=dk.bettingai.trader.tradedvolumedelta.TradedVolumeDeltaTrader")
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}