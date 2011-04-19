package dk.bettingai.marketsimulator

import dk.bettingai.marketsimulator.trader._
import java.io.File
import scala.collection.immutable.TreeMap

/**This class represents input parameters for Market Simulator Application.
 * 
 * 
 * @author korzekwad
 * 
 * @param marketData Text files with market data that will be used for the simulation.Key - marketId, value - market data file.
 * @param trader Fully classified name of the trader implementation class that the simulation is executed for.
 * @param reportDir Directory the html report is written to.
 * @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion).
 * */
case class UserInputData(marketData: TreeMap[Long, File], trader: ITrader, reportDir: String, bank: Double=2500)