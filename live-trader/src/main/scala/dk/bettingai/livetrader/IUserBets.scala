package dk.bettingai.livetrader

import dk.betex.api._
import IBet._
import BetTypeEnum._
import BetStatusEnum._

/**
 * This trait represents an event driven component that provides a quick access to a recent state of user bets. State of user bets is build up from bet events, e.g. betPlaced, betMatched, betCancelled.
 *
 * @author korzekwad
 */
trait IUserBets {

  /**
   * Returns matched/unmatched/all user bets for a market id.
   *
   * @param marketId
   * @param betStatus if None (default) all bets are returned.
   * @return
   */
  def getUserBets(marketId: Long, betStatus: Option[BetStatusEnum] = None): List[IBet]

  /**Place unmatched bet.*/
  def betPlaced(bet: IBet)

  /**Match bet portion.*/
  def betMatched(bet: IBet)

  /**Cancel a bet.*/
  def betCancelled(betId: Long)

  /**Date of recently matched bet.*/
  def getLatestMatchedDate: Long
}