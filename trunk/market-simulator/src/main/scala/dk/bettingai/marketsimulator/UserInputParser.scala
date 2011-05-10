package dk.bettingai.marketsimulator

import java.io._
import scala.io._
import dk.bettingai.marketsimulator.trader._
import scala.collection.immutable.TreeMap
import dk.bettingai.marketsimulator.ISimulator._
/**
 * Parses input arguments to the SimulationApp.
 *
 * @author korzekwad
 *
 */
object UserInputParser {

  /**
   * Parses input arguments to the SimlationApp.
   *
   * @param args Array with input arguments.
   * @return Object representing input parameters to Market Simulator Application
   * @throws Illegal argument exception is thrown if any of input argument doesn't exist or is incorrect.
   */
  def parse(args: Array[String]): UserInputData = {
    /**Parse input parameters.*/
    val argsMap: Map[String, String] = argsToMap(args)

    /**Check if required parameters are present.*/
    require(argsMap.contains("marketDataDir") && argsMap.contains("traderImpl"), "The marketDataDir and traderImpl arguments must be provided.")

    /**Load market data file.*/
    val marketDataDir = new File(argsMap("marketDataDir"))
    require(marketDataDir.isDirectory, "The marketDataDir is not a directory:" + argsMap("marketDataDir"))

    val marketDataSources = marketDataDir.listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f)

    /**Load trader implementation class.*/
    val traderClass:Class[ITrader] =
      try {
        val traderClass = Class.forName(argsMap("traderImpl"))
        traderClass.asInstanceOf[Class[ITrader]]
      } catch {
        case e: Exception => throw new IllegalArgumentException("Can't load trader implementation class: " + argsMap("traderImpl") + ". Details: " + e)
      }

    val htmlReportDir = argsMap.getOrElse("htmlReportDir", "./")
    val bank = argsMap.getOrElse("bank", "2500").toDouble
    
     val traderFactory = new TraderFactory[ITrader] {
    	def create() = traderClass.newInstance()
    }
    
    UserInputData(TreeMap(marketDataSources: _*), traderFactory, htmlReportDir, bank)
  }

  /**
   * Map list of arguments to map,
   *
   * @param args
   * @return key - arg name, value - arg value, empty map is returned if can't parse input parameters.
   */
  private def argsToMap(args: Array[String]): Map[String, String] = {
    try {
      if (args.length >= 2) {
        Map(args.map(arg => (arg.split("=")(0), arg.split("=")(1))): _*)
      } else {
        Map()
      }
    } catch {
      case e: Exception => Map()
    }
  }
}