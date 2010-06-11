
package dk.bettingai.marketsimulator

import java.io._
import scala.io._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.trader._
import org.apache.commons.math.util.MathUtils._

/** Main class for the market simulator.
 * 
 * @author korzekwad
 *
 */
object SimulatorApp  {

	def main(args:Array[String], console: PrintStream) {

		printHeader(console)

		/**Parse input parameters.*/
		val argsMap:Map[String,String] = argsToMap(args)

		/**Check if required parameters are present.*/
		require(argsMap.contains("marketData") && argsMap.contains("traderImpl"),{printHelpMessage(console);return})

		/**Load market data file.*/
		require(new File(argsMap("marketData")).exists,{console.println("Market data file not found: " + argsMap("marketData"));return})

		val marketDataFile = Source.fromFile(new File(argsMap("marketData")))

		/**Load trader implementation class.*/
		val traderImpl = 
			try {
				Class.forName(argsMap("traderImpl")).newInstance()
			}
		catch{
		case e:Exception => console.println("Can't load trader implementation class: " + argsMap("traderImpl") + ". Details: " + e); return
		}

		console.print("Simulation is started.")

		val betex = new Betex()
		val marketEventProcessor = new MarketEventProcessorImpl(betex)
		val traderUserId=100
		val firstBetId=1000
		val simulator = new Simulator(marketEventProcessor,traderImpl.asInstanceOf[ITrader],traderUserId,firstBetId,betex)

		console.print(" Simulation progress:")

		val time = System.currentTimeMillis
		val marketDataFileSize = marketDataFile.size
		var marketDataFileSizeRead=0
		var currentProgress=0
		for(marketEvent <- marketDataFile.reset.getLines()) {
			marketDataFileSizeRead = marketDataFileSizeRead + marketEvent.size
			currentProgress=(marketDataFileSizeRead*100)/marketDataFileSize
			console.print(" " + currentProgress + "%")
			simulator.process(marketEvent)
			simulator.callTrader
		}

		/**Calculate analysis report.*/
		val marketRiskReports = simulator.calculateRiskReport

		if(currentProgress<100) console.print(" 100%")

		console.print("\nSimulation is finished in %s seconds.".format((System.currentTimeMillis-time)/1000))
		console.print("\n\nExpected profit report for trader " + argsMap("traderImpl") + ":")

		var expAggrProfit=0d
		for(r <- marketRiskReports) {
			expAggrProfit = expAggrProfit	+ r.expectedProfit
			console.print("\n%s: %s expProfit=%s expAggrProfit=%s mBets=%s uBets=%s".format(r.marketName,r.eventName,round(r.expectedProfit,2),round(expAggrProfit,2),r.matchedBetsNumber,r.unmatchedBetsNumber))
		}
		console.print("\n------------------------------------------------------------------------------------")

		val totalExpectedProfit = marketRiskReports.foldLeft(0d)(_ + _.expectedProfit)
		val aggrMatchedBets = marketRiskReports.foldLeft(0l)(_ + _.matchedBetsNumber)
		val aggrUnmatchedBets = marketRiskReports.foldLeft(0l)(_ + _.unmatchedBetsNumber)
		console.print("\nTotalExpectedProfit=%s TotalMatchedBets=%s TotalUnmachedBets=%s".format(round(totalExpectedProfit,2),aggrMatchedBets,aggrUnmatchedBets))
	}

	/**Map list of arguments to map, 
	 * 
	 * @param args
	 * @return key - arg name, value - arg value, empty map is returned if can't parse input parameters.
	 */
	private def argsToMap(args:Array[String]):Map[String,String] = {
			try {
				if(args.length==2) {
					Map(args.map(arg => (arg.split("=")(0),arg.split("=")(1))): _*)
				}
				else {
					Map()
				}
			}
			catch{
			case e:Exception => Map()
			}
	}

	private def printHeader(console:PrintStream) {
		console.println("")
		console.println("***********************************************************************************")
		console.println("*Market Simulator Copyright 2010 Daniel Korzekwa(http://danmachine.com)           *")      
		console.println("*Project homepage: http://code.google.com/p/betting-ai/                           *")
		console.println("*Licenced under Apache License 2.0(http://www.apache.org/licenses/LICENSE-2.0)    *")
		console.println("***********************************************************************************")
		console.println("")
	}

	private def printHelpMessage(console:PrintStream) {
		console.println("Wrong input parameters.\n")
		console.println("Usage:")
		console.println("market_simulator marketData=[market_data_file] traderImpl=[trader_impl_class]\n")
		console.println("marketData - Text file with market data that will be used for the simulation.")
		console.println("traderImpl - Fully classified name of the trader implementation class that the simulation will be executed for.")
	}
	def main(args:Array[String]) {
		main(args,System.out)
	}
}
