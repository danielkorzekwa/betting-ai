package dk.bettingai.marketsimulator.risk

import dk.bettingai.marketsimulator.betex.api._
import IBet.BetTypeEnum._
import IExpectedProfitCalculator._

/**This object represents a function that calculates market expected profit from bets and probabilities.
 * 
 * @author korzekwad
 *
 */
object ExpectedProfitCalculator extends IExpectedProfitCalculator{

	/** Calculates market expected profit from bets and probabilities
	 * @param bets
	 * @param probabilities Key - runnerId, value - runner probability.
	 * @return Market expected profit and ifWin for all market runners @see MarketExpectedProfit
	 */
	def calculate(bets:List[IBet],probabilities:Map[Long,Double]): MarketExpectedProfit = {
		if(bets.isEmpty) 0
		
		/**Check input parameters.*/
		bets.foreach(bet => {
			require(bet.marketId == bets(0).marketId,"All bets have to be on the same market:  marketId %s != marketId %s".format(bet.marketId,bets(0).marketId))
			require(probabilities.contains(bet.runnerId),"Runner probability doesn't exist for runnerId=" + bet.runnerId)
		})
		
		/**Lay bet is a back bet with a negative stake.*/
		def betSize(bet:IBet):Double = if(bet.betType==BACK) bet.betSize else -bet.betSize
		
		/**Key - runnerId, value - sum of bet payouts for all runner bets. Bet payout = betsize*betprice*/
		val runnerPayoutMap = bets.groupBy(_.runnerId).map(entry => entry._1 -> entry._2.foldLeft(0d)((a,b)=> a + betSize(b)*b.betPrice))
		
		/**Sum of expected payouts for all bets. Expected payout for a bet = betSize*betPrice*runnerProbability*/
		val totalExpectedPayout = runnerPayoutMap.map(entry => entry._2*probabilities(entry._1)).foldLeft(0d)(_ + _)
		/**Sum of all stakes for all bets.*/
		val totalStake = bets.foldLeft(0d)(_ + betSize(_))
		
		val runnersIfwin = runnerPayoutMap.map(entry => entry._1 -> (entry._2-totalStake))
		
		val missingIfWins = (probabilities.keys.toList -- runnersIfwin.keys.toList).map(_ -> -totalStake)
		/**Calculate market expected profit.*/
		new MarketExpectedProfit(totalExpectedPayout - totalStake,runnersIfwin ++ missingIfWins)
	}
	
		/**Calculate avg weighted price.
	 * 
	 * @param bets
	 * @return
	 */
	def avgPrice(bets:List[IBet]):Double = bets.foldLeft(0d)((sum,bet)=> sum + bet.betPrice*bet.betSize) /bets.foldLeft(0d)(_ + _.betSize)
}