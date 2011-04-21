package dk.bettingai.trader.nn.priceslope

import org.junit._
import Assert._

class PriceSlopeNeuralTraderTest {

	@Test def test {
		val traderClass = classOf[ PriceSlopeNeuralTrader]
		val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName,"bank=100","marketDataDir=./src/test/resources/one_hr_10mins_before_inplay", "traderImpl=" + traderClass.getName)
		dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)	
	}
}