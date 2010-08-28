
package dk.bettingai.marketsimulator

import java.io._
import scala.io._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.trader._
import org.apache.commons.math.util.MathUtils._
import ISimulator._
import org.apache.commons.io.FileUtils
/** Main class for the market simulator.
 * 
 * @author korzekwad
 *
 */
object SimulatorApp  {

	def main(args:Array[String], console: PrintStream) {

		printHeader(console)

		/**Parse input data. Element 1 - marketDataFile, element 2 - traderImplClass, 3 - htmlReportDir*/
		val inputData:Tuple3[Map[Long,File],ITrader,String] = try {
			UserInputParser.parse(args)
		}
		catch {
		case e:Exception => printHelpMessage(console);console.println("\n" + e);return
		}

		console.print("Simulation is started.")

		/**Create market simulator.*/
		val betex = new Betex()
		val marketEventProcessor = new MarketEventProcessorImpl(betex)
		val simulator = new Simulator(marketEventProcessor,betex)

		/**Run market simulator.*/
		console.print(" Simulation progress:")
		val time = System.currentTimeMillis
		val marketRiskReports = simulator.runSimulation(inputData._1,inputData._2.asInstanceOf[ITrader],100,1000,p => console.print(" " + p + "%"))

		/**Print market simulation report.*/
		console.print("\nSimulation is finished in %s milliseconds.".format((System.currentTimeMillis-time)))

		console.print("\nSaving simulation html report...")
		generateHtmlReport(marketRiskReports, inputData._3)
		console.print("DONE")

		console.print("\n\nExpected profit report for trader " + inputData._2.getClass.getName + ":")
		printMarketReport(marketRiskReports,console)
		console.print("\n------------------------------------------------------------------------------------")

		printMarketReportSummary(marketRiskReports, console)
		console.println("")
	}

	private def generateHtmlReport(marketRiskReports:List[IMarketRiskReport], htmlReportDir:String) {
		val in = this.getClass.getResourceAsStream("/sim_report_template.html")
		val simReportTemplate = Source.fromInputStream(in).mkString

		val reportHead = new StringBuilder()
		val reportBody = new StringBuilder()

		for(riskReport <- marketRiskReports) {
			reportHead.append("\n var rawdata%s = {version:'0.6',status:'ok',sig:'963427694',table:{cols:[{id:'date',label:'Date',type:'datetime',pattern:''},{id:'BetFair',label:'BetFair',type:'number',pattern:''},{id:'BetDaq',label:'BetDaq',type:'number',pattern:''}],rows:[{c:[{v:new Date(2010,7,28,0,1,0)},{v:0.0},{v:-100.0}]},{c:[{v:new Date(2010,7,28,0,2,0)},{v:1.0},{v:-99.0}]},{c:[{v:new Date(2010,7,28,0,3,0)},{v:2.0},{v:-98.0}]},{c:[{v:new Date(2010,7,28,0,4,0)},{v:3.0},{v:-97.0}]}]}}"format(riskReport.marketId))
			reportHead.append("\n var data%s = new google.visualization.DataTable(rawdata%s.table);".format(riskReport.marketId,riskReport.marketId))
			reportHead.append("\n var chart%s = new google.visualization.AnnotatedTimeLine(document.getElementById('chart_div%s'));".format(riskReport.marketId,riskReport.marketId))
			reportHead.append("\n chart%s.draw(data%s, {displayAnnotations: true});".format(riskReport.marketId,riskReport.marketId))

			reportBody.append("<br/>%s/%s<br/>".format(riskReport.marketName,riskReport.eventName))
			reportBody.append("<br/><div id='chart_div%s' style='width: 640px; height: 480px;'></div>".format(riskReport.marketId))

		}
		val formmatedReport = simReportTemplate.format(reportHead,reportBody)

		val reportFile = new File(htmlReportDir + "/market_sim_report.html")
		FileUtils.writeStringToFile(reportFile,formmatedReport.toString)

	}

	private def printMarketReport(marketRiskReports:List[IMarketRiskReport],console:PrintStream) {
		printMarketRiskReport(0,0)
		def printMarketRiskReport(marketReportIndex:Int,expAggrProfit:Double):Unit = {
			if(marketReportIndex < marketRiskReports.size) {
				val marketRiskReport = marketRiskReports(marketReportIndex)
				val newExpAggrProfit = expAggrProfit	+ marketRiskReport.marketExpectedProfit.marketExpectedProfit
				val maxRisk = marketRiskReport.marketExpectedProfit.runnersIfWin.reduceLeft((a,b) => if(a._2 < b._2) a else b)
				val minRisk = marketRiskReport.marketExpectedProfit.runnersIfWin.reduceLeft((a,b) => if(a._2 > b._2) a else b)
				console.print("\n%s: %s minProfit/prob=%s/%s maxProfit/prob=%s/%s expProfit=%s expAggrProfit=%s mBets=%s uBets=%s"
						.format(marketRiskReport.marketName,marketRiskReport.eventName,round(maxRisk._2 ,2),
								round(marketRiskReport.marketProbs(maxRisk._1),2),round(minRisk._2 ,2),round(marketRiskReport.marketProbs(minRisk._1),2),
								round(marketRiskReport.marketExpectedProfit.marketExpectedProfit,2),round(newExpAggrProfit,2),
								marketRiskReport.matchedBetsNumber,marketRiskReport.unmatchedBetsNumber))

								/**Recursive call.*/
								printMarketRiskReport(marketReportIndex+1,newExpAggrProfit)
			}
		}
	}

	private def printMarketReportSummary(marketRiskReports:List[IMarketRiskReport],console:PrintStream) {
		val totalExpectedProfit = marketRiskReports.foldLeft(0d)(_ + _.marketExpectedProfit.marketExpectedProfit)
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
		console.println("marketDataDir - Text file with market data that will be used for the simulation.")
		console.println("traderImpl - Fully classified name of the trader implementation class that the simulation will be executed for.")
		console.println("htmlReportDir - Directory the html report is written to. (default = ./)")

	}
	def main(args:Array[String]) {
		main(args,System.out)
	}
}
