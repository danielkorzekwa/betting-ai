package dk.bettingai.marketsimulator

import java.io._
import scala.io._
import dk.bettingai.marketsimulator.trader._

/**Parses input arguments to the SimulationApp.
 * 
 * @author korzekwad
 *
 */
object UserInputParser {
	
	/**Parses input arguments to the SimlationApp.
	 * 
	 * @param args Array with input arguments.
	 * @return Element 1 - marketDataFile, element 2 - traderImplClass
	 * @throws Illegal argument exception is thrown if any of input argument doesn't exist or is incorrect.
	 */
  def parse(args:Array[String]):Tuple2[Source,ITrader] = {
  	/**Parse input parameters.*/
		val argsMap:Map[String,String] = argsToMap(args)

		/**Check if required parameters are present.*/
		require(argsMap.contains("marketData") && argsMap.contains("traderImpl"),"The marketData and traderImpl arguments must be provided.")

		/**Load market data file.*/
		require(new File(argsMap("marketData")).exists,"The marketData file not found:" + argsMap("marketData"))

		val marketDataFile = Source.fromFile(new File(argsMap("marketData")))

		/**Load trader implementation class.*/
		val traderImpl = 
			try {
				Class.forName(argsMap("traderImpl")).newInstance()
			}
		catch{
		case e:Exception => throw new IllegalArgumentException("Can't load trader implementation class: " + argsMap("traderImpl") + ". Details: " + e)
		}
		
		marketDataFile->traderImpl.asInstanceOf[ITrader]
  }
  
  /**Map list of arguments to map, 
	 * 
	 * @param args
	 * @return key - arg name, value - arg value, empty map is returned if can't parse input parameters.
	 */
	private def argsToMap(args:Array[String]):Map[String,String] = {
			try {
				if(args.length==2) {
					Map(args.map(arg => (arg.split("=")(0),arg.split("=")(1))): _*)
				}
				else {
					Map()
				}
			}
			catch{
			case e:Exception => Map()
			}
	}
}