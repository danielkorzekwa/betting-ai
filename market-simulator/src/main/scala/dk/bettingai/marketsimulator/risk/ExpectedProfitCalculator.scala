package dk.bettingai.marketsimulator.risk

import dk.bettingai.marketsimulator.betex.api._
import IBet.BetTypeEnum._
import scala.collection._

/**
 * This trait represents a function that calculates market expected profit and wealth from bets and probabilities.
 *
 * More on expected value: http://en.wikipedia.org/wiki/Expected_value
 * More on wealth: http://en.wikipedia.org/wiki/Kelly_criterion
 *
 * @author korzekwad
 *
 */
object ExpectedProfitCalculator extends IExpectedProfitCalculator {

  /**
   * Calculates market expected profit from bets and probabilities
   * @param bets
   * @param probabilities Key - runnerId, value - runner probability.
   * @param commision Commission on winnings in percentage.
   * @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)
   * @return Market expected profit and ifWin for all market runners @see MarketExpectedProfit
   */
   def calculate(bets: List[IBet], probabilities: Map[Long, Double], commission: Double, bank: Double): MarketExpectedProfit = {
    if (bets.isEmpty) 0

    /**Check input parameters.*/
    bets.foreach(bet => {
      require(bet.marketId == bets(0).marketId, "All bets have to be on the same market:  marketId %s != marketId %s".format(bet.marketId, bets(0).marketId))
      require(probabilities.contains(bet.runnerId), "Runner probability doesn't exist for runnerId=" + bet.runnerId)
    })

    /**Key - runnerId, value - sum of bet payouts for all runner bets. Bet payout = betsize*betprice*/
    val runnerPayoutMap = bets.groupBy(_.runnerId).map(entry => entry._1 -> entry._2.foldLeft(0d)((a, b) => a + betSize(b) * b.betPrice))

    /**Sum of all stakes for all bets.*/
    val totalStake = bets.foldLeft(0d)(_ + betSize(_))

    def ifWinCommission(ifWin: Double): Double = if (ifWin > 0) ifWin * (1 - commission) else ifWin
    /**[runnerId, ifWin]*/
    val runnersIfwin: Map[Long, Double] = probabilities.map(entry => entry._1 -> ifWinCommission(runnerPayoutMap.getOrElse(entry._1, 0d) - totalStake))

    val expectedProfitValue = runnersIfwin.map(entry => entry._2 * probabilities(entry._1)).sum

    def u(ifWin: Double) = Math.log(bank + ifWin)
    def r(currentExp: Double) = -(bank - Math.exp(currentExp))
    val wealth = r(runnersIfwin.map(entry => u(entry._2) * probabilities(entry._1)).sum)

    /**Calculate market expected profit.*/
    new MarketExpectedProfit(expectedProfitValue, wealth, runnersIfwin, probabilities)
  }

  /**Lay bet is a back bet with a negative stake.*/
  private def betSize(bet: IBet): Double = if (bet.betType == BACK) bet.betSize else -bet.betSize

}