package dk.bettingai.marketsimulator.marketevent


/**Parses market event in a json format and calls appropriate method on a betting exchange.
 * @author korzekwad
 *
 */
trait MarketEventProcessor {

	/**Parses market event in a json format and calls appropriate method on a betting exchange.
	 * 
	 * @param marketEvent
	 */
	def process(marketEvent:String)
	
}