package dk.bettingai.marketsimulator.risk

import dk.bettingai.marketsimulator.betex.api._
import scala.collection._

/**This trait represents a function that calculates market expected profit from bets and probabilities.
 * 
 * @author korzekwad
 *
 */
trait IExpectedProfitCalculator {

	/** Calculates market expected profit from bets and probabilities
	 * @param bets
	 * @param probabilities Key - runnerId, value - runner probability.
	 * @param commision Commission on winnings in percentage.
	 * @return Market expected profit and ifWin for all market runners @see MarketExpectedProfit
	 */
	def calculate(bets:List[IBet],probabilities:Map[Long,Double],commission:Double): MarketExpectedProfit

}