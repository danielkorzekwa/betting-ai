
package dk.bettingai.marketsimulator

import java.io._
import scala.io._
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
		if(!argsMap.contains("marketData") || !argsMap.contains("traderImpl")) {
			printHelpMessage(console)
			return
		}

		/**Load market data file.*/
		if(!new File(argsMap("marketData")).exists) {
			console.println("Market data file not found: " + argsMap("marketData"))
			return
		}
		val marketDataFile = Source.fromFile(new File(argsMap("marketData")))

		/**Load trader implementation class.*/
		val traderImplClass = {
			try {
				Class.forName(argsMap("traderImpl")).newInstance()
			}
			catch{
			case e:Exception => console.println("Can't load trader implementation class: " + argsMap("traderImpl") + ". Details: " + e); return
			}
		}

		console.println("""Simulation is started.Simulation progress: 1% 2% 3% 4% 5% 6%
				.......................................................
				.................................................................................................................
				..................................100%" +
				Simulation is finished in 0 sec.
				Expected profit report for trader com.dk.bettingai.trader.SimpleTraderImpl:
				Man Utd vs Arsenal: Match Odds expProfit=3 expAggrProfit=3  mBets=1 uBets=1
				-------------------------------------------------------------------------------------
		TotalExpectedProfit=3 TotalMatchedBets=1 TotalUnmachedBets=0""")

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
