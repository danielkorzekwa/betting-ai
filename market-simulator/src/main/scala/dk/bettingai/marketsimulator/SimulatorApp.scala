
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
import scala.collection.JavaConversions._

/**Google visualization imports*/
import com.google.visualization.datasource._
import DataSourceHelper._
import com.google.visualization.datasource.DataSourceRequest
import com.google.visualization.datasource.base.DataSourceException
import com.google.visualization.datasource.base.DataSourceParameters
import com.google.visualization.datasource.base.TypeMismatchException
import com.google.visualization.datasource.datatable.ColumnDescription
import com.google.visualization.datasource.datatable.DataTable
import com.google.visualization.datasource.datatable.TableCell
import com.google.visualization.datasource.datatable.TableRow
import com.google.visualization.datasource.datatable.value.DateTimeValue
import com.google.visualization.datasource.datatable.value.Value
import com.google.visualization.datasource.datatable.value.ValueType
import com.google.visualization.datasource.query.Query
import com.ibm.icu.text.SimpleDateFormat
import com.ibm.icu.util.GregorianCalendar
import com.ibm.icu.util.TimeZone
import com.ibm.icu.util.ULocale

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
		
			/**Commission on winnings that is used when generating expected profit report.*/
		val commission = 0.05;
		val simulator = new Simulator(marketEventProcessor,betex,commission)

		/**Run market simulator.*/
		console.print(" Simulation progress:")
		val time = System.currentTimeMillis
	
		val marketRiskReports = simulator.runSimulation(inputData._1,inputData._2.asInstanceOf[ITrader],p => console.print(" " + p + "%"))

		/**Print market simulation report.*/
		console.print("\nSimulation is finished in %s milliseconds.".format((System.currentTimeMillis-time)))

		console.print("\nSaving simulation html report...")
		generateHtmlReport(marketRiskReports, inputData._3)
		console.print("DONE")

		console.print("\n\nExpected profit report for trader " + inputData._2.getClass.getName + ":")
		console.print("\nCommission on winnings=" + round(commission*100,2) + "%")
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
			val rawData = generateDataTableJson(riskReport.chartLabels,riskReport.chartValues)
			reportHead.append("\n var rawdata%s =%s".format(riskReport.marketId,rawData))
			reportHead.append("\n var data%s = new google.visualization.DataTable(rawdata%s.table);".format(riskReport.marketId,riskReport.marketId))
			reportHead.append("\n var chart%s = new google.visualization.AnnotatedTimeLine(document.getElementById('chart_div%s'));".format(riskReport.marketId,riskReport.marketId))
			reportHead.append("\n chart%s.draw(data%s, {'dateFormat' : 'HH:mm:ss MMMM dd, yyyy', 'legendPosition': 'newRow',displayAnnotations: true,'scaleType': 'maximized'});".format(riskReport.marketId,riskReport.marketId))

			reportBody.append("<br/>%s/%s<br/>".format(riskReport.marketName,riskReport.eventName))
			reportBody.append("<br/><div id='chart_div%s' style='width: 800px; height: 480px;'></div>".format(riskReport.marketId))

		}
		val formmatedReport = simReportTemplate.format(reportHead,reportBody)

		val reportFile = new File(htmlReportDir + "/market_sim_report.html")
		FileUtils.writeStringToFile(reportFile,formmatedReport.toString)

	}

	/**Generate json data string for time series chart.
	 * 
	 * @param chartLabels Labels for all chart series.
	 * @param chartValues Key - time stamp, value - list of values for all series in the same order as labels
	 *  
	 */
	private def generateDataTableJson(chartLabels:List[String], chartValues:List[Tuple2[Long,List[Double]]]):String = {
			val data = new DataTable()
			val timeSeriesColumns = chartLabels.map(label => new ColumnDescription(label, ValueType.NUMBER, label)).toList
			val cd = new ColumnDescription("date", ValueType.DATETIME, "Date") :: timeSeriesColumns
			data.addColumns(cd)

			/**Add rows for all series.*/
			for((timestamp,values) <-chartValues) {
					val row = new TableRow();
				val calendar = com.ibm.icu.util.Calendar.getInstance().asInstanceOf[GregorianCalendar]
				calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
				calendar.setTimeInMillis(TimeZone.getDefault().getOffset(timestamp) + timestamp);
				row.addCell(new TableCell(new DateTimeValue(calendar)));
				for (value <- values) {
					if(!value.isNaN) {
					row.addCell(value);
					}
					else {
						row.addCell(Value.getNullValueFromValueType(ValueType.NUMBER));
					}
				}
				data.addRow(row);
			}
			
			val query = new Query()
			val parameters = new DataSourceParameters("")
			val request = new DataSourceRequest(query, parameters, ULocale.UK)
			val response = DataSourceHelper.generateResponse(data, request)
			response
	}

	private def printMarketReport(marketRiskReports:List[IMarketRiskReport],console:PrintStream) {
		printMarketRiskReport(0,0)
		def printMarketRiskReport(marketReportIndex:Int,expAggrProfit:Double):Unit = {
			if(marketReportIndex < marketRiskReports.size) {
				val marketRiskReport = marketRiskReports(marketReportIndex)
				val newExpAggrProfit = expAggrProfit	+ marketRiskReport.marketExpectedProfit.marketExpectedProfit
				val maxRisk = marketRiskReport.marketExpectedProfit.runnersIfWin.reduceLeft((a,b) => if(a._2 < b._2) a else b)
				val minRisk = marketRiskReport.marketExpectedProfit.runnersIfWin.reduceLeft((a,b) => if(a._2 > b._2) a else b)
				console.print("\n%s %s: %s minProfit/prob=%s/%s maxProfit/prob=%s/%s expProfit=%s expAggrProfit=%s mBets=%s uBets=%s"
						.format(marketRiskReport.marketId,marketRiskReport.marketName,marketRiskReport.eventName,round(maxRisk._2 ,2),
								round(marketRiskReport.marketExpectedProfit.probabilities(maxRisk._1),2),round(minRisk._2 ,2),round(marketRiskReport.marketExpectedProfit.probabilities(minRisk._1),2),
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
