package dk.bettingai.marketsimulator.perf

import org.junit._
import Assert._
import java.io._ 
import java.util.zip._
import dk.bettingai.marketsimulator._

class SimulatorAppPerformanceTest {

	@Test def testRealMarketDataAndNopTrader() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketData=src/test/resources/marketDataPerfTest.csv","traderImpl=dk.bettingai.marketsimulator.trader.NopTrader"),new PrintStream(consoleStream))
		println(new String(consoleStream.toByteArray))
	}

	@Test @Ignore def testRealMarketDataAndSimpleTrader() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketData=src/test/resources/marketDataPerfTest.csv","traderImpl=dk.bettingai.marketsimulator.trader.SimpleTrader"),new PrintStream(consoleStream))
		println(new String(consoleStream.toByteArray))
	}
}


