package dk.bettingai.tradingoptimiser

import scala.collection.immutable.TreeMap
import java.io.File

/**
 * Represents event driven data files for betting exchange markets.
 *
 * @author korzekwad
 *
 */
object MarketData {

  /** @param marketDataDir Directory, which contains market data for simulation.*/
  def apply(marketDataDir: String): MarketData = {
    val marketDataSources = TreeMap(new File(marketDataDir).listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f): _*)
    new MarketData(marketDataSources)
  }

  def apply(): MarketData = new MarketData(new TreeMap())
}

/**@param data Market events for betting exchange markets. Key - marketId, value - market events*/
case class MarketData(data: TreeMap[Long, File])