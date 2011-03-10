package dk.bettingai.marketsimulator

import org.junit._
import Assert._
import java.io._ 
import java.util.zip._
class SimulatorAppTest {

	/** Check against exceptions only.
	 * 
	 */
	@Test def test = SimulatorApp.main(Array("htmlReportDir=./target","marketDataDir=src/test/resources/marketDataEmpty","traderImpl=dk.bettingai.marketsimulator.trader.examples.NopTrader"))

	@Test def testWrongInputParameters() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("incorrect parameters"),new PrintStream(consoleStream))

		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Market Simulator Copyright 2010 Daniel Korzekwa(http://danmachine.com)"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Usage"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("java.lang.IllegalArgumentException: requirement failed: The marketDataDir and traderImpl arguments must be provided."))
	}

	@Test def testCorrectInputParametersEmptyEventData() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("htmlReportDir=./target","marketDataDir=src/test/resources/marketDataEmpty","traderImpl=dk.bettingai.marketsimulator.trader.examples.NopTrader"),new PrintStream(consoleStream))

		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is started"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is finished in"))
		assertFalse("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("expProfit"))
	}

	@Test def testSimpleTraderAndPlaceBetEvent() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("htmlReportDir=./target","marketDataDir=src/test/resources/marketDataPlaceAndCancelLayBet","traderImpl=dk.bettingai.marketsimulator.trader.examples.SimpleTrader"),new PrintStream(consoleStream))

		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is started"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is finished in"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Match Odds: Man Utd vs Arsenal minProfit/prob=-2.0/0.68 maxProfit/prob=2.28/0.32 expProfit=-0.65 expAggrProfit=-0.65 mBets=3 uBets=1"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("TotalExpectedProfit=-0.65 TotalMatchedBets=3 TotalUnmachedBets=1"))

	}

	@Test def testSimpleTraderAndBetsForTwoMarkets() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("htmlReportDir=./target","marketDataDir=src/test/resources/twomarketfiles","traderImpl=dk.bettingai.marketsimulator.trader.examples.SimpleTrader"),new PrintStream(consoleStream))
		println(new String(consoleStream.toByteArray))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is started"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is finished in"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Match Odds: Man Utd vs Arsenal minProfit/prob=0.38/0.34 maxProfit/prob=1.33/0.66 expProfit=1.01 expAggrProfit=1.01 mBets=2 uBets=2"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Match Odds: Fulham vs Wigan minProfit/prob=-0.8/0.5 maxProfit/prob=1.9/0.5 expProfit=0.55 expAggrProfit=1.56 mBets=1 uBets=1"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("TotalExpectedProfit=1.56 TotalMatchedBets=3 TotalUnmachedBets=3"))
	}
	
	@Test def testSimpleTraderAndRealData() {
		val consoleStream = new ByteArrayOutputStream()
		SimulatorApp.main(Array("htmlReportDir=./target","marketDataDir=src/test/resources/marketDataPerfTest","traderImpl=dk.bettingai.marketsimulator.trader.examples.SimpleTrader"),new PrintStream(consoleStream))
		println(new String(consoleStream.toByteArray))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is started"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("Simulation is finished in"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("5f Mdn Stks: /GB/Ponte 15th Aug minProfit/prob=-2022.44/0.14 maxProfit/prob=20348.58/0.01 expProfit=-363.24 expAggrProfit=-363.24 mBets=12879 uBets=1096"))
		assertTrue("Wrong output:\n" + new String(consoleStream.toByteArray),new String(consoleStream.toByteArray).contains("TotalExpectedProfit=-363.24 TotalMatchedBets=12879 TotalUnmachedBets=1096"))
	}
}


