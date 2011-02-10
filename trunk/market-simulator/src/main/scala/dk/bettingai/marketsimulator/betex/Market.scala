package dk.bettingai.marketsimulator.betex

import java.util.Date
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex.api.IBet._
import scala.collection.mutable.ListBuffer
import IBet.BetStatusEnum._
import IBet.BetTypeEnum._
import Market._
import IMarket._
import IRunnerTradedVolume._
import scala.collection._

/**This class represents a market on a betting exchange.
 * @author korzekwad
 *
 */
object Market {
  class Runner(val runnerId: Long, val runnerName: String) extends IMarket.IRunner {
    override def toString = "Runner [runnerId=%s, runnerName=%s]".format(runnerId, runnerName)
  }

  class RunnerPrice(val price: Double, val totalToBack: Double, val totalToLay: Double) extends IMarket.IRunnerPrice {
    override def toString = "RunnerPrice [price=%s, totalToBack=%s, totalToLay=%s]".format(price, totalToBack, totalToLay)
  }

}

class Market(val marketId: Long, val marketName: String, val eventName: String, val numOfWinners: Int, val marketTime: Date, val runners: List[IMarket.IRunner]) extends IMarket {

  /**key - runnerId, value - runnerBackBetsPerPrice*/
  private val backBets = new UnmatchedBackBets()
  /**key - runnerId, value - runnerLayBetsPerPrice*/
  private val layBets = new UnmatchedLayBets()

  private val matchedBets = ListBuffer[IBet]()
  private val betsIds = scala.collection.mutable.Set[Long]()

  require(numOfWinners > 0, "numOfWinners should be bigger than 0, numOfWinners=" + numOfWinners)
  require(runners.size > 1, "Number of market runners should be bigger than 1, numOfRunners=" + runners.size)
  /**Returns Map[price,bets] for runnerId*/
  private def getRunnerBets(runnerId: Long, bets: mutable.Map[Long, mutable.Map[Double, ListBuffer[IBet]]]): mutable.Map[Double, ListBuffer[IBet]] = {
    bets.getOrElseUpdate(runnerId, scala.collection.mutable.Map[Double, ListBuffer[IBet]]())
  }

  /** Places a bet on a betting exchange market.
   * 
   * @param betId
   * @param userId
   * @param betSize
   * @param betPrice
   * @param betType
   * @param runnerId
   * 
   * @return The bet that was placed.
   */
  def placeBet(betId: Long, userId: Long, betSize: Double, betPrice: Double, betType: BetTypeEnum, runnerId: Long): IBet = {

    require(betSize > 0, "Bet size must be >0, betSize=" + betSize)
    require(runners.exists(s => s.runnerId == runnerId), "Can't place bet on a market. Market runner not found for marketId/runnerId=" + marketId + "/" + runnerId)
    require(!betsIds.contains(betId), "Bet for betId=%s already exists".format(betId))

    betsIds += betId

    val newBet = new Bet(betId, userId, betSize, betPrice, betType, U, marketId, runnerId)

    betType match {
      case BACK => {
        val betMatchingResults = layBets.matchBet(newBet)
        betMatchingResults.unmatchedBet.foreach(b => backBets.addBet(b))
        matchedBets ++= betMatchingResults.matchedBets
      }
      case LAY => {
        val betMatchingResults = backBets.matchBet(newBet)
        betMatchingResults.unmatchedBet.foreach(b => layBets.addBet(b))
        matchedBets ++= betMatchingResults.matchedBets
      }
    }

    newBet
  }

  /** Cancels a bet on a betting exchange market.
   *
   * @param betId Unique id of a bet to be cancelled.
   * 
   * @return amount cancelled
   * @throws NoSuchElementException is thrown if no unmatched bet for betId/userId found.
   */
  def cancelBet(betId: Long): Double = {

    val backCancelledAmount = backBets.cancelBet(betId)
    if (backCancelledAmount > 0) backCancelledAmount else {
      val layCancelledAmount = layBets.cancelBet(betId)
      if (layCancelledAmount > 0) layCancelledAmount else throw new NoSuchElementException("Bet not found for bet id=" + betId)
    }
  }

  /** Cancels bets on a betting exchange market.
   * 
   * @param userId 
   * @param betsSize Total size of bets to be cancelled.
   * @param betPrice The price that bets are cancelled on.
   * @param betType
   * @param runnerId 
   * 
   * @return Amount cancelled. Zero is returned if nothing is available to cancel.
   */
  def cancelBets(userId: Long, betsSize: Double, betPrice: Double, betType: BetTypeEnum, runnerId: Long): Double = {

    val amountCancelled = betType match {
      case BACK => backBets.cancelBets(userId, betsSize, betPrice, runnerId)
      case LAY => layBets.cancelBets(userId, betsSize, betPrice, runnerId)
    }

    amountCancelled
  }

  /** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
   *  Prices with zero volume are not returned by this method.
   * 
   * @param runnerId Unique runner id that runner prices are returned for.
   * @return
   */
  def getRunnerPrices(runnerId: Long): List[IMarket.IRunnerPrice] = {
    require(runners.exists(s => s.runnerId == runnerId), "Market runner not found for marketId/runnerId=" + marketId + "/" + runnerId)

    val allBackBetsList = backBets.getBets(runnerId)
    val allLayBetsList = layBets.getBets(runnerId)

    val betsByPriceMap = (allBackBetsList.toList ::: allLayBetsList.toList).toList.groupBy(b => b.betPrice)

    def totalStake(bets: List[IBet], betType: BetTypeEnum) = bets.filter(b => b.betType == betType).foldLeft(0d)(_ + _.betSize)
    betsByPriceMap.map(entry => new RunnerPrice(entry._1, totalStake(entry._2, LAY), totalStake(entry._2, BACK))).toList.sortWith(_.price < _.price)
  }

  /**Returns best toBack/toLay prices for market runner.
   * Element 1 - best price to back, element 2 - best price to lay
   * Double.NaN is returned if price is not available.
   * @return 
   * */
  def getBestPrices(runnerId: Long): Tuple2[IRunnerPrice, IRunnerPrice] = {

    require(runners.exists(s => s.runnerId == runnerId), "Market runner not found for marketId/runnerId=" + marketId + "/" + runnerId)

    val bestPriceToBack = layBets.getBestPrice(runnerId)
    val bestPriceToLay = backBets.getBestPrice(runnerId)

    new Tuple2(bestPriceToBack, bestPriceToLay)

  }

  /**Returns best toBack/toLay prices for market.
   * 
   * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
   */
  def getBestPrices(): Map[Long, Tuple2[IRunnerPrice, IRunnerPrice]] = {
    Map(runners.map(r => r.runnerId -> getBestPrices(r.runnerId)): _*)
  }

  /**Returns total traded volume for all prices on all runners in a market.*/
  def getRunnerTradedVolume(runnerId: Long): IRunnerTradedVolume = {
    require(runners.exists(s => s.runnerId == runnerId), "Market runner not found for marketId/runnerId=" + marketId + "/" + runnerId)

    /**Take only BACK bets to not double count traded volume (each matched back bet has corresponding matched lay bet.*/
    val betsByPrice = matchedBets.toList.filter(b => b.betType == BACK && b.runnerId == runnerId).groupBy(b => b.betPrice)

    /**Map betsByPrice to list of PriceTradedVolume.*/
    val pricesTradedVolume = betsByPrice.map(entry => new RunnerTradedVolume.PriceTradedVolume(entry._1, entry._2.foldLeft(0d)(_ + _.betSize))).toList.sortWith(_.price < _.price)
    new RunnerTradedVolume(pricesTradedVolume)
  }

  /**Returns all bets placed by user on that market.
   *
   *@param userId
   */
  def getBets(userId: Int): List[IBet] = backBets.getBets(userId) ::: layBets.getBets(userId) ::: matchedBets.filter(b => b.userId == userId).toList

  /**Returns all bets placed by user on that market.
   *
   *@param userId
   *@param matchedBetsOnly If true then matched bets are returned only, 
   * otherwise all unmatched and matched bets for user are returned.
   */
  def getBets(userId: Int, matchedBetsOnly: Boolean): List[IBet] = {
    val bets = matchedBetsOnly match {
      case true => matchedBets.filter(b => b.userId == userId).toList
      case false => getBets(userId)
    }
    bets
  }

 	/**Returns bet for a number o criteria.
	 * 
	 * @param userId
	 * @param betStatus
	 * @param betType
	 * @param betPrice
	 * @param runnerId
	 */
	def getBets(userId: Int,betStatus: BetStatusEnum, betType: BetTypeEnum, betPrice: Double, runnerId:Long):List[IBet] = {

    val bets = betStatus match {
      case U => betType match {
          case BACK => backBets.getBets(betPrice, runnerId)
          case LAY => layBets.getBets(betPrice, runnerId)
        }
      case M => matchedBets.filter(b => b.betType == betType && b.betPrice == betPrice && b.runnerId == runnerId).toList
    }

    bets.filter(b => b.userId==userId)
  }

  override def toString = "Market [marketId=%s, marketName=%s, eventName=%s, numOfWinners=%s, marketTime=%s, runners=%s]".format(marketId, marketName, eventName, numOfWinners, marketTime, runners)
}