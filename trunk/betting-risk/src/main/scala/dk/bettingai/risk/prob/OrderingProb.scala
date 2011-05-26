package dk.bettingai.risk.prob

/**
 * This trait calculates ordering probabilities for place (2 winners) and show( 3 winners) markets.
 * Those probabilities are derived from win probabilities.
 * For more information on used algorithm go to Handbook of Sports and Lottery Markets,
 * Donald B.Hausch,Wiliam T.Ziemba, Chapter 10 Efficiency of Betting Markets, page 196.
 *
 * @author korzekwad
 */
trait OrderingProb {

  /**
   * Calculates probability that runner finishes first or second.
   * Place is a two winner market.
   *
   * @param runnerId The unique runner id that place probability is calculated for.
   * @param winProbabilities key - runner Id, value - probability
   */
  def calcPlaceProb(runnerId: Long, winProbabilities: Map[Long, Double]): Double

  /**
   * Calculates probability that runner finishes first, second or third.
   * Show is a 3 winner market.
   *
   * @param runnerId The unique runner id that show probability is calculated for.
   * @param winProbabilities key - runner Id, value - probability
   */
  def calcShowProb(runnerId: Long, winProbabilities: Map[Long, Double]): Double

  /**
   * Calculates probability of runners 1 and 2 taking first and second place respectively.
   *
   * @param firstRunnerProb
   * @param secondRunnerProb
   *
   */
  def orderingProb(firstRunnerProb: Double, secondRunnerProb: Double): Double

  /**
   * Calculates probability of runners 1 and 2 and 3 taking first, second and third place respectively.
   *
   * @param firstRunnerProb
   * @param secondRunnerProb
   * @param thirdRunnerProb
   *
   */
  def orderingProb(firstRunnerProb: Double, secondRunnerProb: Double, thirdRunnerProb: Double): Double

  /**Calculates probability that runner finishes second in a market.*/
  def secondPlaceProb(runnerId: Long, winProbabilities: Map[Long, Double]): Double

  /**Calculates probability that runner finishes third in a market.*/
  def thirdPlaceProb(runnerId: Long, winProbabilities: Map[Long, Double]): Double
}

object OrderingProb extends OrderingProb {

  def calcPlaceProb(runnerId: Long, winProbabilities: Map[Long, Double]): Double = {
    require(winProbabilities.size > 1, "Number of runners should be >1")
    winProbabilities(runnerId) + secondPlaceProb(runnerId, winProbabilities)
  }

  def calcShowProb(runnerId: Long, winProbabilities: Map[Long, Double]): Double = {
    require(winProbabilities.size > 2, "Number of runners should be >2")
    val showProb = winProbabilities(runnerId) + secondPlaceProb(runnerId, winProbabilities) + thirdPlaceProb(runnerId, winProbabilities)
    showProb
  }

  def orderingProb(firstRunnerProb: Double, secondRunnerProb: Double): Double =
    firstRunnerProb * secondRunnerProb / (1 - firstRunnerProb)

  def orderingProb(firstRunnerProb: Double, secondRunnerProb: Double, thirdRunnerProb: Double): Double =
    firstRunnerProb * secondRunnerProb * thirdRunnerProb / (1 - firstRunnerProb) / (1 - firstRunnerProb - secondRunnerProb)

  def secondPlaceProb(runnerId: Long, winProbabilities: Map[Long, Double]): Double = {
    val secondPlaceProb = winProbabilities.filterKeys { rId => rId != runnerId }.map { case (rId, prob) => orderingProb(winProbabilities(rId), winProbabilities(runnerId)) }.sum
    secondPlaceProb
  }

  def thirdPlaceProb(runnerId: Long, winProbabilities: Map[Long, Double]): Double = {

    val firstAndSecondPairs = for {
      rId1 <- winProbabilities.keys.filter { rId => rId != runnerId }
      rId2 <- winProbabilities.keys.filter { rId => rId != runnerId && rId != rId1 }
    } yield rId1 -> rId2
    val thirdPlaceProb = firstAndSecondPairs.toList.map { case (first, second) => orderingProb(winProbabilities(first), winProbabilities(second), winProbabilities(runnerId)) }.sum
    thirdPlaceProb
  }
}