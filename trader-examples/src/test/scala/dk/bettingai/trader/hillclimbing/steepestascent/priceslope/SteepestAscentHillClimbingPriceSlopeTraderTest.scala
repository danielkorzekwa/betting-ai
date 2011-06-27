package dk.bettingai.trader.hillclimbing.steepestascent.priceslope

import org.junit._
import Assert._
import dk.betex._
import dk.bettingai.marketsimulator._
import java.io.File
import scala.collection.immutable.TreeMap
import dk.bettingai.marketsimulator.ISimulator._

/**Run market simulation for SteepestAscentHillClimbingPriceSlopeTrader in n number of iterations.
 * 
 * @author korzekwad
 *
 */
class SteepestAscentHillClimbingPriceSlopeTraderTest {

	val betex = new Betex()
	val commission = 0.05
	val simulator = new Simulator(betex,commission,1000)

	val marketDataDir = new File("./src/test/resources/one_hr_10mins_before_inplay")
	val marketDataSources = TreeMap (marketDataDir.listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f) : _*)
	
	val traderFactory = new TraderFactory[SteepestAscentHillClimbingPriceSlopeTrader] {
		def create() = new SteepestAscentHillClimbingPriceSlopeTrader()
	}

	@Test def test {
		
		for(i <- 1 to 10) {
			betex.clear()
			simulator.runSimulation(marketDataSources, traderFactory :: Nil, p => {})
		}
	}
	
}