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
	
	/** Triggers trader implementation so it can analyse markets on a betting exchange and take appropriate bet placement decisions.
	 * 
	 */
	def callTrader
	
	/**Calculates market expected profit based on all bets that are placed by a given user on all betting exchange markets.
	 * 
	 * @param userId The user that the market expected profit is calculated for. 
	 * Only bets placed by this user are taken into account for calculation.
	 * 
	 * @return
	 */
	def calculateRiskReport(userId:Long):List[IMarketRiskReport]
}