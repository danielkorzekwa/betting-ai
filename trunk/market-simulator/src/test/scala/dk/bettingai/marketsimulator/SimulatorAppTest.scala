package dk.bettingai.marketsimulator

import org.junit._
import Assert._
import java.io._ 
import java.util.zip._
class SimulatorAppTest {

	/** Check against exceptions only.
	 * 
	 */
	@Test def test = SimulatorApp.main(Array("marketData=src/test/resources/marketDataEmpty.csv","traderImpl=dk.bettingai.marketsimulator.trader.NopTrader"))

	@Test def testWrongNumberOfInputParameters() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("incorrect parameters"),new PrintStream(consoleStream))
		
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray), new String(consoleStream.toByteArray).contains("Wrong input parameters"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Usage"))
	}
	
	@Test def testWrongMarketDataParameter() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketDatasss=marketData.csv","traderImpl=dk.bettingai.trader.SimpleTraderImpl"),new PrintStream(consoleStream))
		
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Wrong input parameters"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Usage"))
	}
	
	@Test def testWrongTraderImplParameter() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketData=marketData.csv","traderImpldfdfd=dk.bettingai.trader.SimpleTraderImpl"),new PrintStream(consoleStream))
		
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Wrong input parameters"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Usage"))
	}
	
		@Test def testWrongFormatOfParameters() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketDatamarketData.csv","traderImpldfdfddk.bettingai.trader.SimpleTraderImpl"),new PrintStream(consoleStream))
		
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Wrong input parameters"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Usage"))
	}
	
	@Test def testNotExistingMarketDataFile() {
			val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketData=/tmp/blabla/marketdata.csv","traderImpl=dk.bettingai.trader.SimpleTraderImpl"),new PrintStream(consoleStream))
		
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Market data file not found: /tmp/blabla/marketdata.csv"))
	}
	
	@Test def testTraderImplNotFound() {
			val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketData=src/test/resources/marketDataEmpty.csv","traderImpl=com.dk.bettingai.blabla.SimpleTraderImpl"),new PrintStream(consoleStream))
		
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Can't load trader implementation class: com.dk.bettingai.blabla.SimpleTraderImpl. Details: java.lang.ClassNotFoundException: com.dk.bettingai.blabla.SimpleTraderImpl"))
	}
	
		@Test def testTraderImplNoEmptyConstructor() {
			val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketData=src/test/resources/marketDataEmpty.csv","traderImpl=dk.bettingai.marketsimulator.mock.TraderWithoutEmptyConstructor"),new PrintStream(consoleStream))
		
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Can't load trader implementation class: dk.bettingai.marketsimulator.mock.TraderWithoutEmptyConstructor. Details: java.lang.InstantiationException: dk.bettingai.marketsimulator.mock.TraderWithoutEmptyConstructor"))
	}
	
	@Test def testCorrectInputParameters() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("marketData=src/test/resources/marketDataEmpty.csv","traderImpl=dk.bettingai.marketsimulator.trader.NopTrader"),new PrintStream(consoleStream))
		
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is started"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is finished in"))
	}
}


