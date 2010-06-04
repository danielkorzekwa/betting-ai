package dk.bettingai.marketsimulator.risk

import IRiskAnalyser._
import dk.bettingai.marketsimulator.betex.api._

/**This class represents a component that supports risk analysis on a betting exchange.
 * 
 * @author korzekwad
 *
 */
object RiskAnalyser {
	class MarketRiskReport(val marketId:Long,val marketName:String,val eventName:String,val expectedProfit:Double,val matchedBetsNumber:Long,val unmatchedBetsNumber:Long) extends IMarketRiskReport {

		override def toString() = "MarketRiskReport [marketId=%s, marketName=%s, eventName=%s, expectedProfit=%s, matchedBetsNumber=%s, unmatchedBetsNumber=%s]".format(marketId,marketName,eventName,expectedProfit,matchedBetsNumber,unmatchedBetsNumber)
	}
}

class RiskAnalyser(betex:IBetex,probabilityCalc: IProbabilityCalculator, expectedProfitCalc: IExpectedProfitCalculator) extends IRiskAnalyser {

	/**Calculates market expected profit based on all bets that are placed by a given user on all betting exchange markets.
	 * 
	 * @param userId The user that the market expected profit is calculated for. 
	 * Only bets placed by this user are taken into account for calculation.
	 * 
	 * @return
	 */
	def calculteMarketRiskReports(userId:Long): List[IMarketRiskReport] = throw new UnsupportedOperationException("Not implemented.")
}