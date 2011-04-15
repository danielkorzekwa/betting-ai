package dk.bettingai.marketsimulator.risk

import dk.bettingai.marketsimulator.betex.api._
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
trait IExpectedProfitCalculator {

  /**
   * Calculates market expected profit from bets and probabilities
   * @param bets
   * @param probabilities Key - runnerId, value - runner probability.
   * @param commision Commission on winnings in percentage.
   * @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)
   * @return Market expected profit and ifWin for all market runners @see MarketExpectedProfit
   */
  def calculate(bets: List[IBet], probabilities: Map[Long, Double], commission: Double, bank: Double): MarketExpectedProfit
}