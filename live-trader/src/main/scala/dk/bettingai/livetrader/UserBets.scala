package dk.bettingai.livetrader

import dk.bettingai.marketsimulator.betex._
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
case class UserBets(initialBets: List[IBet]) extends IUserBets {

  private val bets = ListBuffer(initialBets: _*)

  /**
   * Returns matched/unmatched/all user bets for a market id.
   *
   * @param marketId
   * @param betStatus if None (default) all bets are returned.
   * @return
   */
  def getUserBets(marketId: Long, betStatus: Option[BetStatusEnum] = None): List[IBet] = betStatus match {
    case None => bets.toList
    case Some(M) => bets.filter(b => b.betStatus == M).toList
    case Some(U) => bets.filter(b => b.betStatus == U).toList
  }

  /**Place unmatched bet.*/
  def betPlaced(bet: IBet) = bets += bet

  /**Match bet portion.*/
  def betMatched(bet: IBet) = {
    val unmatchedBet = bets.find(b => b.betId == bet.betId)
    /**Update unmatched bet.*/
    if (unmatchedBet.isDefined) {
      val remainingUnmatched = unmatchedBet.get.betSize - bet.betSize
      if (remainingUnmatched > 0) {
        val remainingUnmatchedBet = Bet(unmatchedBet.get.betId, unmatchedBet.get.userId, remainingUnmatched, unmatchedBet.get.betPrice, unmatchedBet.get.betType, unmatchedBet.get.marketId, unmatchedBet.get.runnerId)
        bets.update(bets.indexOf(unmatchedBet.get), remainingUnmatchedBet)
      } else {
        bets -= (unmatchedBet.get)
      }
    }
    /**Update matched bet.*/
    bets += bet
  }

  /**Cancel a bet.*/
  def betCancelled(betId: Long) = throw new UnsupportedOperationException("Not implemented yet.")

  /**Date of recently matched bet.*/
  def getLatestMatchedDate: Long = bets.toList match {
    case Nil => 0
    case xs => bets.filter(b => b.betStatus == M).map(b => b.matchedDate.get).foldLeft(0l)((a, b) => if (a > b) a else b)
  }
}