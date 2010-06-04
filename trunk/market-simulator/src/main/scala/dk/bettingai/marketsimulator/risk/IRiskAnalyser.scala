package dk.bettingai.marketsimulator.risk

import IRiskAnalyser._
/**This trait represents a component that supports risk analysis on a betting exchange.
 * 
 * @author korzekwad
 *
 */
object IRiskAnalyser {

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
trait IRiskAnalyser {

	/**Calculates market expected profit based on all bets that are placed by a given user on all betting exchange markets.
	 * 
	 * @param userId The user that the market expected profit is calculated for. 
	 * Only bets placed by this user are taken into account for calculation.
	 * 
	 * @return
	 */
	def calculteMarketRiskReports(userId:Long): List[IMarketRiskReport]
}