package dk.bettingai.marketsimulator.risk

import dk.bettingai.marketsimulator.betex.api._
import IExpectedProfitCalculator._

/**This trait represents a function that calculates market expected profit from bets and probabilities.
 * 
 * @author korzekwad
 *
 */
object IExpectedProfitCalculator {

	/**
	 * @param marketExpcetedProfit Market expected profit from bets and probabilities 
	 * @param runnersIfWin If wins values for all market runners. IfWin = What was the market profit if the runner would win.
	 */
	class MarketExpectedProfit(val marketExpectedProfit:Double,val runnersIfWin:Map[Long,Double]) {
		override def toString = "MarketExpectedProfit [marketExpectedProfit=%s, runnersIfWin=%s]".format(marketExpectedProfit,runnersIfWin)
	}
}
trait IExpectedProfitCalculator {

	/** Calculates market expected profit from bets and probabilities
	 * @param bets
	 * @param probabilities Key - runnerId, value - runner probability.
	 * @return Market expected profit and ifWin for all market runners @see MarketExpectedProfit
	 */
	def calculate(bets:List[IBet],probabilities:Map[Long,Double]): MarketExpectedProfit

}