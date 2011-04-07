package dk.bettingai.marketsimulator.reporting

import dk.bettingai.marketsimulator.MarketReport
import scala.io.Source

/**
 * Generates google time line charting report.
 *
 * @author korzekwad
 */
object ReportGenerator {
	
  /**
   * Generates google time line charting report in a html format.
   *
   * @param marketReports
   */
  def generateReport(marketReports: List[MarketReport]): String = {
    val in = this.getClass.getResourceAsStream("/sim_report_template.html")
    val simReportTemplate = Source.fromInputStream(in).mkString

    val reportHead = new StringBuilder()
    val reportBody = new StringBuilder()

    for (marketReport <- marketReports) {
      val traderReport = marketReport.traderReports.head
      val rawData = DataTableGenerator.generateDataTableJson(traderReport.chartLabels, traderReport.chartValues)
      reportHead.append("\n var rawdata%s =%s".format(marketReport.marketId, rawData))
      reportHead.append("\n var data%s = new google.visualization.DataTable(rawdata%s.table);".format(marketReport.marketId, marketReport.marketId))
      reportHead.append("\n var chart%s = new google.visualization.AnnotatedTimeLine(document.getElementById('chart_div%s'));".format(marketReport.marketId, marketReport.marketId))
      reportHead.append("\n chart%s.draw(data%s, {'dateFormat' : 'HH:mm:ss MMMM dd, yyyy', 'legendPosition': 'newRow',displayAnnotations: true,'scaleType': 'maximized'});".format(marketReport.marketId, marketReport.marketId))

      reportBody.append("<br/>%s/%s<br/>".format(marketReport.marketName, marketReport.eventName))
      reportBody.append("<br/><div id='chart_div%s' style='width: 800px; height: 480px;'></div>".format(marketReport.marketId))

    }
    val formmatedReport = simReportTemplate.format(reportHead, reportBody)
    formmatedReport
  }
}