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
	 * @param key - runnerId, value - runner probability
	 */
	class MarketExpectedProfit(val marketExpectedProfit:Double,val runnersIfWin:Map[Long,Double],val probabilities:Map[Long,Double]) {

		def ifLose(runnerId:Long):Double = {
				val ifWin = runnersIfWin(runnerId)
				val prob = probabilities(runnerId)
				(marketExpectedProfit - ifWin*prob)/(1-prob)
		}
		
		def ifWin(runnerId:Long):Double = runnersIfWin(runnerId)
		
		override def toString = "MarketExpectedProfit [marketExpectedProfit=%s, runnersIfWin=%s, probabilities=%s]".format(marketExpectedProfit,runnersIfWin,probabilities)
	}
}
trait IExpectedProfitCalculator {

	/** Calculates market expected profit from bets and probabilities
	 * @param bets
	 * @param probabilities Key - runnerId, value - runner probability.
	 * @param commision Commission on winnings in percentage.
	 * @return Market expected profit and ifWin for all market runners @see MarketExpectedProfit
	 */
	def calculate(bets:List[IBet],probabilities:Map[Long,Double],commission:Double): MarketExpectedProfit

}