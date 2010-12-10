package dk.bettingai.trader.hillclimbing.price

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator._
import java.io.File

class HillClimbingPriceTraderTest {

	val betex = new Betex()
	val marketEventProcessor = new MarketEventProcessorImpl(betex)
	val simulator = new Simulator(marketEventProcessor,betex)

	val commission = 0.05
	val traderUserId=100
	val historicalDataUserId=1000

	val marketDataDir = new File("./src/test/resources/one_hr_10mins_before_inplay")
	val marketDataSources = Map (marketDataDir.listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f) : _*)
	val trader = new HillClimbingPriceTrader()

	@Test def test {
		
		/**If this trader is run for long enough then it is trapped in a local maximum at price 2.54.*/
		for(i <- 1 to 5) {
			betex.clear()
			simulator.runSimulation(marketDataSources, trader, traderUserId, historicalDataUserId, p => {}, commission)
		}
	}
	
}