package dk.bettingai.marketsimulator.risk

import dk.bettingai.marketsimulator.betex.api._

/**This trait represents a function that calculates market expected profit from bets and probabilities.
 * 
 * @author korzekwad
 *
 */
trait IExpectedProfitCalculator {
	
	/** Calculates market expected profit from bets and probabilities
	 * @param bets
	 * @param probabilities Key - runnerId, value - runner probability.
	 * @return
	 */
	def calculate(bets:List[IBet],probabilities:Map[Long,Double]): Double
}