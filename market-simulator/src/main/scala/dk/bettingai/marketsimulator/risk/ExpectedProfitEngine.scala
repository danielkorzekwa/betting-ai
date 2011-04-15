package dk.bettingai.marketsimulator.risk

import dk.bettingai.marketsimulator.betex.api._
import IBet.BetTypeEnum._
import scala.collection._

/**
 * Calculates risk metrics (expected profit and wealth) based on market bets.
 * It's a stateful component, that keeps internally the total stake and payouts for all market runners,
 * which allows for high performance calculation of risk metrics while market probabilities are changing.
 *
 * More on expected value: http://en.wikipedia.org/wiki/Expected_value
 * More on wealth: http://en.wikipedia.org/wiki/Kelly_criterion
 *
 * I'm not thread-safe.
 *
 * @author korzekwad
 *
 */
case class ExpectedProfitEngine extends IExpectedProfitEngine {

  /**Sum of all bet sizes, lay bets have negative bet size.*/
  private var totalStake = 0d;

  /**Key - runnerId, value - sum of bet payouts for all runner bets. Bet payout = betsize*betprice*/
  private val runnerPayoutMap = mutable.Map[Long, Double]()

  /**Add bet to the model.*/
  override def addBet(betSize: Double, betPrice: Double, betType: BetTypeEnum, runnerId: Long) {
    val validatedBetSize = if (betType == BACK) betSize else -betSize
    totalStake += validatedBetSize
    runnerPayoutMap(runnerId) = runnerPayoutMap.getOrElse(runnerId, 0d) + (validatedBetSize * betPrice)
  }

  /**
   * Calculates market expected profit based on all bets in a model, given market probabilities and commission.
   * @param probabilities Key - runnerId, value - runner probability.
   * @param commision Commission on winnings in percentage.
   * @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)
   *
   * @return market expected profit
   */
  override def calculateExpectedProfit(probabilities: Map[Long, Double], commission: Double, bank: Double): MarketExpectedProfit = {
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

}