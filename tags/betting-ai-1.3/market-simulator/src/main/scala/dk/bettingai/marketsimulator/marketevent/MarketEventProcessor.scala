package dk.bettingai.marketsimulator.marketevent


/**Parses market event in a json format and calls appropriate method on a betting exchange.
 * @author korzekwad
 *
 */
trait MarketEventProcessor {

	
	/**Processes market event in a json format and calls appropriate method on a betting exchange.
	 * 
	 * @param marketEvent
	 * @param nextBetId Returns betId for PLACE_BET event
	 * @param userId It is used for bet placement events
	 * 
	 * @return event timestamp
	 */
	def process(marketEvent:String, nextBetId: => Long,userId:Int):Long
	
}