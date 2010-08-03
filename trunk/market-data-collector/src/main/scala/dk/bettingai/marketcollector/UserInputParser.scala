package dk.bettingai.marketcollector

/**Parses input arguments to the MarketDataCollector application.
 * 
 * @author korzekwad
 *
 */
object UserInputParser {

	/**Parses input arguments to the SimlationApp.
	 * 
	 * @param args Array with input arguments.
	 * @return Key - param name, Value - param value
	 * @throws Illegal argument exception is thrown if any of input argument doesn't exist or is incorrect.
	 */
	def parse(args:Array[String]):Map[String,String] = {
		/**Parse input parameters.*/
		val argsMap:Map[String,String] = argsToMap(args)

		/**Check if required parameters are present.*/
		require(argsMap.contains("marketDataDir") && argsMap.contains("bfUser") && argsMap.contains("bfPassword") && argsMap.contains("bfProductId")
				&& argsMap.contains("collectionInterval") && argsMap.contains("discoveryInterval") && argsMap.contains("startInMinutesFrom")
				&& argsMap.contains("startInMinutesTo"),"All input arguments must be provided.")
		argsMap
	}

	/**Map list of arguments to map, 
	 * 
	 * @param args
	 * @return key - arg name, value - arg value, empty map is returned if can't parse input parameters.
	 */
	private def argsToMap(args:Array[String]):Map[String,String] = {
		try {
			Map(args.map(arg => (arg.split("=")(0),arg.split("=")(1))): _*)
		}
		catch{
		case e:Exception => Map()
		}
	}
}