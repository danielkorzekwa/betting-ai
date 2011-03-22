package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._
import org.slf4j._
import HillClimbing._

/**@see trait level comments.
 * 
 * @author korzekwad
 *
 */
object HillClimbing {

  /** Data model representing trader implementation and its fitness. 
   *   @param trader Trader implementation.
   *   @param expectedProfit Current expected profit achieved by this trader.
   *   @param matchedBetsNumm Total number of matched bets for this trader.
   **/
  case class Solution[T <: ITrader](trader: T, expectedProfit: Double, matchedBetsNum: Double) {
    override def toString = "Solution [trader=%s, expectedProfit=%s, matchedBetsNum=%s]".format(trader, expectedProfit, matchedBetsNum)
  }

  /**The current progress of optimisation.
   * @param iterationNum The current number of iteration.
   * @param bestSoFar The best solution found so far.
   * @param currentBest The best solution found for the current generation
   */
  case class Progress[T <: ITrader](iterationNum: Int, bestSoFar: Solution[T], currentBest: Solution[T])
}

/** Search for optimal trader using hill climbing algorithm from Essentials of metaheuristics book 
 *  (http://www.goodreads.com/book/show/9734814-essentials-of-metaheuristics)
 */
trait HillClimbing[T <: ITrader] {

  private val logger = LoggerFactory.getLogger(getClass)
  private def defaultProgress(progress: Progress[T]) { logger.info("Iter number=" + progress.iterationNum + ", bestSoFar=" + progress.bestSoFar + ", currentBest=" + progress.currentBest) }

  /** Search for optimal trader using hill climbing algorithm. 
   * 
   * @param trader Trader to be optimised.
   * @param generationNum Number of generations that optimisation is executed for.
   * @param progress The current progress of optimisation, it is called after every generation.
   *                If progress is not specified then default progress based on console logging is provided.
   *
   * @return Best trader found.
   */
  def optimise(trader: T, generationNum: Int, progress: Option[Progress[T] => Unit] = None): Solution[T] = {

    /**We assume that initial trader is always the worst one and should never be returned itself.*/
    val initialSolution = Solution(trader, Double.MinValue, 0)

    val bestTrader = (1 to generationNum).foldLeft(initialSolution)((best, iter) => {
      val child = breed(best)
      val bestOfTwo = if (child.expectedProfit > best.expectedProfit) child else best
      progress.getOrElse(defaultProgress _)(Progress(iter, bestOfTwo, child))
      bestOfTwo
    })

    bestTrader
  }

  /**Returns a new trader solution, that is compared against the current best. It must be provided by a concrete implementation of HillClimbing algorithm.
   * 
   * @param trader The current best solution (a parent used to born a new child)
   * @return New trader solution (a child).
   */
  def breed(trader: Solution[T]): Solution[T]
}