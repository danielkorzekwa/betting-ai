
package dk.bettingai.marketsimulator

import java.io._
import dk.bettingai.marketsimulator.trader._
import org.apache.commons.math.util.MathUtils._
import org.apache.commons.io.FileUtils
import scala.collection._
import immutable.TreeMap
import scala.annotation._
import dk.bettingai.marketsimulator.reporting._
import dk.bettingai.marketsimulator.ISimulator._
import com.ibm.icu.text.SimpleDateFormat

/**
 * Main class for the market simulator.
 *
 * @author korzekwad
 *
 */
object SimulatorApp {

  def main(args: Array[String], console: PrintStream) {

    printHeader(console)

    /**Parse input data.*/
    val inputData: UserInputData = try {
      UserInputParser.parse(args)
    } catch {
      case e: Exception => printHelpMessage(console); console.println("\n" + e); return
    }

    run(inputData,console)
  }

  def run(inputData: UserInputData, console: PrintStream) {

    console.print("Simulation is started.")
   
    /**Create market simulator.*/
    /**Commission on winnings that is used when generating expected profit report.*/
    val commission = 0.05;
    /**Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)*/
    val simulator = Simulator(commission,inputData.bank)

    /**Run market simulator.*/
    console.print(" Simulation progress:")
    val time = System.currentTimeMillis

    val marketRiskReports = simulator.runSimulation(inputData.marketData, inputData.traderFactory :: Nil, (progress: Int) => console.print(" " + progress + "%"))

    /**Print market simulation report.*/
    console.print("\nSimulation is finished in %s milliseconds.".format((System.currentTimeMillis - time)))

    console.print("\nSaving simulation html report...")
    generateHtmlReport(marketRiskReports.marketReports, inputData.reportDir)
    console.print("DONE")

    console.print("\n\nExpected profit report for trader " + inputData.traderFactory.create().getClass.getName + ":")
    console.print("\nCommission on winnings=" + round(commission * 100, 2) + "%")
    console.print("\nAmount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)=" + inputData.bank)
    printMarketReport(marketRiskReports.marketReports, console)
    console.print("\n------------------------------------------------------------------------------------")

    printMarketReportSummary(marketRiskReports, console)
    console.println("")
  }

  private def generateHtmlReport(marketReports: List[MarketReport], htmlReportDir: String) {
    val formmatedReport = ReportGenerator.generateReport(marketReports)
    val reportFile = new File(htmlReportDir + "/market_sim_report.html")
    FileUtils.writeStringToFile(reportFile, formmatedReport.toString)
  }

  private def printMarketReport(marketReports: List[MarketReport], console: PrintStream) {
    val marketReportsSize = marketReports.size
    printMarketRiskReport(0, 0)

    @tailrec
    def printMarketRiskReport(marketReportIndex: Int, expAggrProfit: Double): Unit = {
      val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

      if (marketReportIndex < marketReportsSize) {
        val marketRiskReport = marketReports(marketReportIndex)
        val traderReport = marketRiskReport.traderReports.head
        val newExpAggrProfit = expAggrProfit + traderReport.marketExpectedProfit.marketExpectedProfit
        val maxRisk = traderReport.marketExpectedProfit.runnersIfWin.reduceLeft((a, b) => if (a._2 < b._2) a else b)
        val minRisk = traderReport.marketExpectedProfit.runnersIfWin.reduceLeft((a, b) => if (a._2 > b._2) a else b)
        console.print("\n%s, %s, %s: %s minProfit/prob=%s/%s maxProfit/prob=%s/%s expProfit=%s expAggrProfit=%s mBets=%s uBets=%s"
          .format(marketRiskReport.marketId, df.format(marketRiskReport.marketTime), marketRiskReport.marketName, marketRiskReport.eventName, round(maxRisk._2, 2),
            round(traderReport.marketExpectedProfit.probabilities(maxRisk._1), 2), round(minRisk._2, 2), round(traderReport.marketExpectedProfit.probabilities(minRisk._1), 2),
            round(traderReport.marketExpectedProfit.marketExpectedProfit, 2), round(newExpAggrProfit, 2),
            traderReport.matchedBetsNumber, traderReport.unmatchedBetsNumber))

        /**Recursive call.*/
        printMarketRiskReport(marketReportIndex + 1, newExpAggrProfit)
      }
    }
  }

  private def printMarketReportSummary(simulationReport: SimulationReport, console: PrintStream) {

    simulationReport.marketReports match {
      case Nil => console.print("\nTotalExpectedProfit=%s TotalMatchedBets=%s TotalUnmachedBets=%s".format(0, 0, 0))
      case x :: xs => {
        /**SimulatorApp run simulation for one trader only.*/
        val userId = x.traderReports.head.trader.userId
        def totalExpectedProfit = simulationReport.totalExpectedProfit(userId)
         def totalWealth = simulationReport.totalWealth(userId)
        def aggrMatchedBets = simulationReport.totalMatchedBetsNum(userId)
        def aggrUnmatchedBets = simulationReport.totalUnmatchedBetsNum(userId)

        console.print("\nTotalExpectedProfit=%s Wealth=%s TotalMatchedBets=%s TotalUnmachedBets=%s".format(round(totalExpectedProfit, 2),round(totalWealth, 2), aggrMatchedBets, aggrUnmatchedBets))
      }
    }
  }

  private def printHeader(console: PrintStream) {
    console.println("")
    console.println("***********************************************************************************")
    console.println("*Market Simulator Copyright 2010 Daniel Korzekwa(http://danmachine.com)           *")
    console.println("*Project homepage: http://code.google.com/p/betting-ai/                           *")
    console.println("*Licenced under Apache License 2.0(http://www.apache.org/licenses/LICENSE-2.0)    *")
    console.println("***********************************************************************************")
    console.println("")
  }

  private def printHelpMessage(console: PrintStream) {
    console.println("Usage:")
    console.println("market_simulator marketData=[market_data_file] traderImpl=[trader_impl_class]\n")
    console.println("marketDataDir - Text files with market data that will be used for the simulation.")
    console.println("traderImpl - Fully classified name of the trader implementation class that the simulation is executed for.")
    console.println("htmlReportDir - Directory the html report is written to. (default = ./)")
    console.println("bank - Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion). (default = 2500)")

  }
  def main(args: Array[String]) {
    main(args, System.out)
  }
}
