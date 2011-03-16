package dk.bettingai.tradingoptimiser

import scala.collection.immutable.TreeMap
import java.io._

/**Represents event driven data for betting exchange markets.
 * 
 * @author korzekwad
 *
 */
object MarketData {
  /**Directory, which contains market data for simulation.*/
  def apply(marketDataDir: String): MarketData = {
    val marketDataSources = TreeMap(new File(marketDataDir).listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f): _*)
    new MarketData(marketDataSources)
  }
}

/**
 * @param data Market events for betting exchange markets. Key - marketId, value - market events
 * 
 * @author korzekwad
 *
 */
case class MarketData(data: TreeMap[Long, File])