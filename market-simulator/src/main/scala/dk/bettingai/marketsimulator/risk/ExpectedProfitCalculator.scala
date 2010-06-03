package dk.bettingai.marketsimulator.risk

import dk.bettingai.marketsimulator.betex.api._
import IBet.BetTypeEnum._

/**This object represents a function that calculates market expected profit from bets and probabilities.
 * 
 * @author korzekwad
 *
 */
object ExpectedProfitCalculator extends IExpectedProfitCalculator{

	/** Calculates market expected profit from bets and probabilities. Probabilities must be normalised.
	 * @param bets Bets on the same market
	 * @param probabilities Key - runnerId, value - runner probability.
	 * @return
	 */
	def calculate(bets:List[IBet],probabilities:Map[Long,Double]): Double = {
		if(bets.isEmpty) 0
		
		/**Check input parameters.*/
		bets.foreach(bet => {
			require(bet.betId == bets(0).betId,"All bets have to be on the same market:  marketId %s != marketId %s".format(bet.betId,bets(0)))
			require(probabilities.contains(bet.runnerId),"Runner probability doesn't exist for runnerId=" + bet.runnerId)
		})
		
		/**Lay bet is a back bet with a negative stake.*/
		def betSize(bet:IBet):Double = if(bet.betType==BACK) bet.betSize else -bet.betSize
		
		/**Sum of expected payouts for all bets. Expected payout for a bet = betSize*betPrice*runnerProbability*/
		val totalExpectedPayout = bets.foldLeft(0d)((sum:Double,bet:IBet) => sum + betSize(bet)*bet.betPrice*probabilities(bet.runnerId))
		
		/**Sum of all stakes for all bets.*/
		val totalStake = bets.foldLeft(0d)(_ + betSize(_))
		
		/**Calculate market expected profit.*/
		totalExpectedPayout - totalStake
	}
}