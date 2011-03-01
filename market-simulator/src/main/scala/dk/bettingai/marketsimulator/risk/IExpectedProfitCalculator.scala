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
  def calculate(bets: List[IBet], probabilities: Map[Long, Double], commission: Double): MarketExpectedProfit

  /** Calculates wealth from bets and probabilities based on the following utility and reverse functions:
   * def u(ifWin: Double) = log(bank - ifWin)
   * def r(currentExp: Double) = bank - exp(currentExp) 
   * @param bets
   * @param probabilities Key - runnerId, value - runner probability.
   * @param commision Commission on winnings in percentage.
   * @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)
   * @return Market expected profit and ifWin for all market runners @see MarketExpectedProfit
   */
  def wealth(bets: List[IBet], probabilities: Map[Long, Double], commission: Double,bank: Double): MarketExpectedProfit

  /** Calculates market expected profit from bets and probabilities
   * @param bets
   * @param probabilities Key - runnerId, value - runner probability.
   * @param commision Commission on winnings in percentage.
   * @param u - Utility function that transforms ifWins before calculating expected liability.
   * @param r - Utility function that reverses expected liability based on utility function.
   * @return Market expected profit and ifWin for all market runners @see MarketExpectedProfit
   */
  def calculate(bets: List[IBet], probabilities: Map[Long, Double], commission: Double, u: (Double) => Double, r: (Double) => Double): MarketExpectedProfit
}