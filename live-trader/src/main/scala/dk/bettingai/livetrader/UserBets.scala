package dk.bettingai.livetrader

import dk.bettingai.marketsimulator.betex.api._
import IBet._
import BetTypeEnum._
import BetStatusEnum._
import scala.collection.mutable.ListBuffer
/**
 * This class represents an event driven component that provides a quick access to a recent state of user bets. State of user bets is build up from bet events, e.g. betPlaced, betMatched, betCancelled.
 *
 * @author korzekwad
 */
case class UserBets(initialBets: List[IBet]) extends IUserBets{

	private val bets = ListBuffer(initialBets: _*)
	
	 /**
   * Returns matched/unmatched/all user bets for a market id.
   *
   * @param marketId
   * @param betStatus if None (default) all bets are returned.
   * @return
   */
  def getUserBets(marketId: Long, betStatus: Option[BetStatusEnum] = None): List[IBet] = bets.toList

  /**Place unmatched bet.*/
  def betPlaced(bet: IBet) = bets += bet
  
  /**Match bet portion.*/
  def betMatched(betId:Long,matchedSize: Double) = throw new UnsupportedOperationException("Not implemented yet.")

  /**Cancel a bet.*/
  def betCancelled(betId:Long) = throw new UnsupportedOperationException("Not implemented yet.")
}