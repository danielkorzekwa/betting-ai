package dk.bettingai.marketsimulator

import org.junit._
import Assert._
import scala.io._
import java.io._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.trader._

class SimulatorIntegrationTest {

	private val betex = new Betex()
	private val marketEventProcessor = new MarketEventProcessorImpl(betex)
	private val trader = new SimpleTrader()
	private val traderUserId=100
	private val simulator = new Simulator(marketEventProcessor,trader,100,1000,betex)

	@Test def testNoMarketEvents {
		val marketEventsFile = Source.fromFile(new File("src/test/resources/marketDataEmpty.csv"))

		/**Process market events.*/
		for(marketEvent <- marketEventsFile.getLines()) {
			simulator.process(marketEvent)
			simulator.callTrader
		}

		/**Calculate analysis report.*/
		val marketRiskReport = simulator.calculateRiskReport
		assertEquals(0,marketRiskReport.size)
	}

	@Test def testCreateMarketEventOnly {
		val marketEventsFile = Source.fromFile(new File("src/test/resources/marketDataCreateMarketOnly.csv"))

		/**Process market events.*/
		for(marketEvent <- marketEventsFile.getLines()) {
			simulator.process(marketEvent)
			simulator.callTrader
		}

		/**Calculate analysis report.*/
		val marketRiskReport = simulator.calculateRiskReport
		assertEquals(1,marketRiskReport.size)
		
		assertEquals(10,marketRiskReport(0).marketId)
		assertEquals("Match Odds",marketRiskReport(0).marketName)
		assertEquals("Man Utd vs Arsenal",marketRiskReport(0).eventName)
		assertEquals(0,marketRiskReport(0).expectedProfit,0)
		assertEquals(0,marketRiskReport(0).matchedBetsNumber,0)
		assertEquals(0,marketRiskReport(0).unmatchedBetsNumber,0)
	}
	
	@Test @Ignore def testCreateMarketEventAndPlaceBetEvents {fail("Not implemented")}
}