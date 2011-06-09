package dk.bettingai.marketsimulator.risk

import dk.betex.api._
import IBet.BetTypeEnum._
import scala.collection._

/** Calculates risk metrics (expected profit and wealth) based on market bets.  
 * It's a stateful component, that keeps internally the total stake and payouts for all market runners, 
 * which allows for high performance calculation of risk metrics while market probabilities are changing.
 * 
 * More on expected value: http://en.wikipedia.org/wiki/Expected_value
 * More on wealth: http://en.wikipedia.org/wiki/Kelly_criterion
 * 
 * @author korzekwad
 *
 */
trait IExpectedProfitEngine {

  /**Add bet to the model.*/
  def addBet(betSize: Double, betPrice: Double, betType: BetTypeEnum, runnerId:Long)

  /**Calculates market expected profit based on all bets in a model, given market probabilities and commission.
   * @param probabilities Key - runnerId, value - runner probability.
   * @param commision Commission on winnings in percentage.
   * @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)
   * 
   * @return market expected profit
   * */
  def calculateExpectedProfit(probabilities: Map[Long, Double], commission: Double,bank:Double): MarketExpectedProfit
  

}