package dk.bettingai.marketsimulator

import ISimulator._

/**This trait represents a simulator that processes market events, analyses trader implementation and returns analysis report for trader implementation.
 * 
 * @author korzekwad
 *
 */
object ISimulator {
	trait IMarketRiskReport {
		val marketId:Long
		val marketName:String
		val eventName:String
		/**Market expected profit based on bets and market probabilities.*/
		val expectedProfit:Double
		val matchedBetsNumber:Long
		val unmatchedBetsNumber:Long
	}
}
trait ISimulator {

	/** Processes market event in a json format and calls appropriate method on a betting exchange.
	 * 
	 * @param marketEvent 
	 */
	def process(marketEvent:String)
	
	/** Triggers trader implementation for all markets on a betting exchange, so it can take appropriate bet placement decisions.
	 * 
	 */
	def callTrader
	
	/**Calculates market expected profit based on all bets that are placed by the trader implementation on all betting exchange markets.
	 * 
	 * @return
	 */
	def calculateRiskReport:List[IMarketRiskReport]
}