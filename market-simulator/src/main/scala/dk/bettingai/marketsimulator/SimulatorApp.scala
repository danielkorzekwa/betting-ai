
package dk.bettingai.marketsimulator

import java.io._

/** Main class for the market simulator.
 * 
 * @author korzekwad
 *
 */
object SimulatorApp  {

	def main(args:Array[String], console: PrintStream) {

		printHeader(console)
		
		/**Wrong number of input arguments.*/
		if(args.length!=2) {
			printHelpMessage(console);
			return
		}

		/**Wrong input arguments.*/
		val argNames = args.map(arg => arg.split("=")(0))
		if(!argNames.contains("marketData") || !argNames.contains("traderImpl")) {
			printHelpMessage(console);
			return
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
