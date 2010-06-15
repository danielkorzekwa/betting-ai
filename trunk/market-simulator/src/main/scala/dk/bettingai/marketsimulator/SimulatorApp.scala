
package dk.bettingai.marketsimulator

import java.io._
import scala.io._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.trader._
import org.apache.commons.math.util.MathUtils._
import ISimulator._

/** Main class for the market simulator.
 * 
 * @author korzekwad
 *
 */
object SimulatorApp  {

	def main(args:Array[String], console: PrintStream) {

		printHeader(console)

		/**Element 1 - marketDataFile, element 2 - traderImplClass*/
		val inputData:Tuple2[Source,ITrader] = try {
			UserInputParser.parse(args)
		}
		catch {
		case e:Exception => printHelpMessage(console);console.println("\n" + e);return
		}

		console.print("Simulation is started.")

		val betex = new Betex()
		val marketEventProcessor = new MarketEventProcessorImpl(betex)
		val traderUserId=100
		val firstBetId=1000
		val simulator = new Simulator(marketEventProcessor,inputData._2.asInstanceOf[ITrader],traderUserId,firstBetId,betex)

		console.print(" Simulation progress:")
		val time = System.currentTimeMillis

		/**Calculate analysis report.*/
		val marketRiskReports = simulator.runSimulation(inputData._1,p => console.print(" " + p + "%"))

		console.print("\nSimulation is finished in %s seconds.".format((System.currentTimeMillis-time)/1000))
		console.print("\n\nExpected profit report for trader " + inputData._2.getClass + ":")

		def printMarketReport(marketReportIndex:Int,expAggrProfit:Double):Unit = {
			if(marketReportIndex < marketRiskReports.size) {
				val marketRiskReport = marketRiskReports(marketReportIndex)
				val newExpAggrProfit = expAggrProfit	+ marketRiskReport.expectedProfit
				console.print("\n%s: %s expProfit=%s expAggrProfit=%s mBets=%s uBets=%s".format(marketRiskReport.marketName,marketRiskReport.eventName,round(marketRiskReport.expectedProfit,2),round(newExpAggrProfit,2),marketRiskReport.matchedBetsNumber,marketRiskReport.unmatchedBetsNumber))
				printMarketReport(marketReportIndex+1,newExpAggrProfit)
			}
		}
		printMarketReport(0,0)

		console.print("\n------------------------------------------------------------------------------------")

		val totalExpectedProfit = marketRiskReports.foldLeft(0d)(_ + _.expectedProfit)
		val aggrMatchedBets = marketRiskReports.foldLeft(0l)(_ + _.matchedBetsNumber)
		val aggrUnmatchedBets = marketRiskReports.foldLeft(0l)(_ + _.unmatchedBetsNumber)
		console.print("\nTotalExpectedProfit=%s TotalMatchedBets=%s TotalUnmachedBets=%s".format(round(totalExpectedProfit,2),aggrMatchedBets,aggrUnmatchedBets))
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
		console.println("Usage:")
		console.println("market_simulator marketData=[market_data_file] traderImpl=[trader_impl_class]\n")
		console.println("marketData - Text file with market data that will be used for the simulation.")
		console.println("traderImpl - Fully classified name of the trader implementation class that the simulation will be executed for.")
	}
	def main(args:Array[String]) {
		main(args,System.out)
	}
}
