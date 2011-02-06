package dk.bettingai.marketsimulator.betex.api

import IUnmatchedBets._
import scala.collection._
import mutable.ListBuffer
import IMarket._
import IBet.BetStatusEnum._
import IBet.BetTypeEnum._
import dk.bettingai.marketsimulator.betex._

/**This trait represents unmatched back or lay bets. 
 * It also acts as a bet matching engine taking a bet and matching it against unmatched bets in a model.
 * 
 * This is a stateful component.
 * 
 * @author korzekwad
 *
 */
object IUnmatchedBets {

  /**This class represents a result of matching a bet against unmatched bets, e.g. matching back bet against lay bets.
   * 
   * @param unmatchedBet Unmatched portion of a bet that was matched against unmatched bets. If a bet is not matched at all then unmatchedBet is none.
   * @param matchedBets List of all back and lay matched bets. Examples: 
   * If a bet is fully matched against another bet then matchedBets list contains two matched bets. 
   * If a bet is matched against 2 unmatched bets then matchedBets contains 3 matched bets.
   * If a bet is not matched then matchedBets list is empty.
   * 	  
   */
  class BetMatchingResult(val unmatchedBet: Option[IBet], val matchedBets: List[IBet]) {
    override def toString = "BetMatchingResult [unmatchedBet=%s, matchedBets=%s".format(unmatchedBet, matchedBets)
  }
}

trait IUnmatchedBets {
  /**key - runnerId, value - runnerBackBetsPerPrice*/
  val unmatchedBets = scala.collection.mutable.Map[Long, scala.collection.mutable.Map[Double, ListBuffer[IBet]]]()

  /**Returns Map[price,bets] for runnerId*/
  protected def getRunnerBets(runnerId: Long, bets: mutable.Map[Long, mutable.Map[Double, ListBuffer[IBet]]]): mutable.Map[Double, ListBuffer[IBet]] = {
    bets.getOrElseUpdate(runnerId, scala.collection.mutable.Map[Double, ListBuffer[IBet]]())
  }
  
  protected def getPricesToBeMatched(runnerId:Long, price:Double):Iterator[Double]

  /**Add unmatched bet to a model. Only bet of a type, of which this instance of UnmatchedBets is parameterised by, can be added to a model, otherwise exception should be thrown.*/
  def addBet(bet: IBet) = getRunnerBets(bet.runnerId, unmatchedBets).getOrElseUpdate(bet.betPrice, ListBuffer()) += bet

  /**Match a bet against unmatched bets in a model.
   * 
   * @param bet The bet to be matched against unmatched bets.
   * @return Result of bet matching @see BetMatchingResult
   */
  def matchBet(bet: IBet): BetMatchingResult = {
	    val pricesToBeMatched = getPricesToBeMatched(bet.runnerId, bet.betPrice)

    val betsToBeMatched = getRunnerBets(bet.runnerId, unmatchedBets)

    val matchedBets = ListBuffer[IBet]()

    var unmatchedBet: Option[IBet] = None

    def matchBet(newBet: IBet, priceToMatch: Double): Unit = {

      val priceBets = betsToBeMatched.getOrElse(priceToMatch, ListBuffer())
      if (!priceBets.isEmpty) {

        /**Get bet to be matched and remove it from the main list of bets - it will be added later as a result of matching.*/
        val betToBeMatched = priceBets.head
        priceBets.remove(0)

        /**Do the bets matching.*/
        val matchingResult = newBet.matchBet(betToBeMatched)
        matchingResult.filter(b => b.betStatus == U && b.betId != bet.betId).foreach(b => priceBets.insert(0, b))
        matchingResult.filter(b => b.betStatus == M).foreach(b => matchedBets += b)

        /**Make sure that priceBetsMap doesn't contain entries with empty bets list.*/
        if (priceBets.isEmpty) betsToBeMatched.remove(priceToMatch)

        /**Find unmatched portion for a bet being placed.*/
        val unmatchedPortion = matchingResult.find(b => b.betId == bet.betId && b.betStatus == U)
        if (!unmatchedPortion.isEmpty) {
          matchBet(new Bet(bet.betId, bet.userId, unmatchedPortion.get.betSize, bet.betPrice, bet.betType, U, bet.marketId, bet.runnerId), priceToMatch)
        }
      } else {
        if (pricesToBeMatched.hasNext) {
          matchBet(newBet, pricesToBeMatched.next)
        } else {
          unmatchedBet = Some(newBet)
        }
      }
    }

    if (pricesToBeMatched.hasNext) matchBet(bet, pricesToBeMatched.next)
    else unmatchedBet = Some(bet)

    new BetMatchingResult(unmatchedBet, matchedBets.toList)
  }

  /**Returns all unmatched bets placed by user on that market.
   *
   *@param userId
   */
  def getBets(userId: Int): List[IBet] = {
    val unmatchedBetsList = for {
      runnerBackBetsMap <- unmatchedBets.values
      val runnerBets = runnerBackBetsMap.values.foldLeft(List[IBet]())((a, b) => a.toList ::: b.toList).filter(b => b.userId == userId)
    } yield runnerBets

    List.flatten(unmatchedBetsList.toList)
  }

   /** Cancels bets on a betting exchange market.
   * 
   * @param userId 
   * @param betsSize Total size of bets to be cancelled.
   * @param betPrice The price that bets are cancelled on.
   * @param runnerId 
   * 
   * @return Amount cancelled. Zero is returned if nothing is available to cancel.
   */
  def cancelBets(userId: Long, betsSize: Double, betPrice: Double, runnerId: Long): Double = {

    val runnerBets = unmatchedBets.getOrElseUpdate(runnerId, scala.collection.mutable.Map[Double, ListBuffer[IBet]]())
    val priceBets = runnerBets.getOrElse(betPrice, new ListBuffer[IBet]())

    val betsToBeCancelled = priceBets.filter(b => b.userId == userId).reverseIterator

    def cancelRecursively(amountToCancel: Double, amountCancelled: Double): Double = {
      val betToCancel = betsToBeCancelled.next
      val betCanceledAmount = if (amountToCancel >= betToCancel.betSize) {
        priceBets -= betToCancel
        betToCancel.betSize
      } else {
        val updatedBet = Bet(betToCancel.betId, betToCancel.userId, betToCancel.betSize - amountToCancel, betToCancel.betPrice, betToCancel.betType, betToCancel.marketId, betToCancel.runnerId)
        priceBets.update(priceBets.indexOf(betToCancel, 0), updatedBet)
        amountToCancel
      }
      val newAmountToCancel = amountToCancel - betCanceledAmount
      val newAmountCancelled = amountCancelled + betCanceledAmount
      if (betsToBeCancelled.hasNext && newAmountToCancel > 0) cancelRecursively(newAmountToCancel, newAmountCancelled)
      else newAmountCancelled
    }

    val totalCancelled = if (betsToBeCancelled.hasNext) cancelRecursively(betsSize, 0) else 0

    /**Make sure that there are no empty entries in priceBetsMap*/
    if (totalCancelled > 0) {
      val pricesWithEmptyBets = runnerBets.filter { case (price, bets) => bets.isEmpty }.keys
      pricesWithEmptyBets.foreach(price => runnerBets.remove(price))
    }

    totalCancelled
  }
  
  /**Returns best unmatched price
   * 
   * @return Double.NaN is returned if price is not available.
   * */
  def getBestPrice(runnerId: Long): IRunnerPrice

}