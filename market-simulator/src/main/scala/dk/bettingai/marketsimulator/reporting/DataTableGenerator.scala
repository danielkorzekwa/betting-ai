package dk.bettingai.marketsimulator.reporting

import scala.collection.JavaConversions._

/**Google visualization imports*/
import com.google.visualization.datasource._
import DataSourceHelper._
import com.google.visualization.datasource.DataSourceRequest
import com.google.visualization.datasource.base.DataSourceParameters
import com.google.visualization.datasource.datatable.ColumnDescription
import com.google.visualization.datasource.datatable.DataTable
import com.google.visualization.datasource.datatable.TableCell
import com.google.visualization.datasource.datatable.TableRow
import com.google.visualization.datasource.datatable.value.DateTimeValue
import com.google.visualization.datasource.datatable.value.Value
import com.google.visualization.datasource.datatable.value.ValueType
import com.google.visualization.datasource.query.Query
import com.ibm.icu.util.GregorianCalendar
import com.ibm.icu.util.TimeZone
import com.ibm.icu.util.ULocale

/**
 * Generates data table json object in a format of google visualisation api.
 *
 * @author korzekwad
 *
 */
object DataTableGenerator {

  /**
   * Generates json data string for time series chart.
   *
   * @param chartLabels Labels for all chart series.
   * @param chartValues Key - time stamp, value - list of values for all series in the same order as labels
   *
   */
  def generateDataTableJson(chartLabels: List[String], chartValues: List[Tuple2[Long, List[Double]]]): String = {
    val data = new DataTable()
    val timeSeriesColumns = chartLabels.map(label => new ColumnDescription(label, ValueType.NUMBER, label)).toList
    val cd = new ColumnDescription("date", ValueType.DATETIME, "Date") :: timeSeriesColumns
    data.addColumns(cd)

    /**Add rows for all series.*/
    for ((timestamp, values) <- chartValues) {
      val row = new TableRow();
      val calendar = com.ibm.icu.util.Calendar.getInstance().asInstanceOf[GregorianCalendar]
      calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
      calendar.setTimeInMillis(TimeZone.getDefault().getOffset(timestamp) + timestamp);
      row.addCell(new TableCell(new DateTimeValue(calendar)));
      for (value <- values) {
        if (!value.isNaN) {
          row.addCell(value);
        } else {
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
}