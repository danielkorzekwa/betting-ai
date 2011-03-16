package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._
import org.slf4j._
import dk.bettingai.marketsimulator._
import ISimulator._

/** Search for optimal trader using co-evolution based hill climbing gradient with random restarts algorithm. Algorithm 10 from Essentials of metaheuristics book 
 *  (http://www.goodreads.com/book/show/9734814-essentials-of-metaheuristics) with a one difference that individuals in population compete 
 *  against each other instead of evaluating them in isolation (chapter 6, page 107,...The entire population participated at the same time in the game...)
 *  
 *  Fitness function for individual (trader) is defined by current expected profit. 
 *
 * @author korzekwad
 *
 */
class CoevolutionHillClimbingWithRestarts[T <: ITrader] extends ICoevolutionHillClimbingWithRestarts {

  type TRADER = T

  private val logger = LoggerFactory.getLogger(getClass)
  private def defaultProgress(iter: Int, best: Solution[T], current: Solution[T]) { logger.info("Iter number=" + iter + ", bestSoFar=" + best + ", currentBest=" + current) }

  /** Search for optimal trader using co-evolution based gradient hill climbing algorithm with random restarts, @see ICoevolutionHillClimbingWithRestarts	 
   *   
   * @param trader Trader to be optimised.
   * @param mutate Takes trader as input and creates mutated trader.
   * @param populationSize Number of individuals in every generation.
   * @param generationNum Number of generations that optimisation is executed for.
   *                (current number of generation, the best solution so far, best solution for the current generation)
   * @param optimise Optimise best candidate of population.    
   * @param progress The current progress of optimisation, it is called after every generation and returns. Default progress is provided.           
   * @return Best trader found.
   **/
  def optimise(marketData: MarketData, trader: T, restart: (Solution[T]) => T, populationSize: Int, generationNum: Int, optimize: (Solution[T]) => Solution[T], progress: Option[(Int, Solution[T], Solution[T]) => Unit] = None): Solution[T] = {

    /**We assume that initial trader is always the worst one and should never be returned itself.*/
    val initialSolution = Solution(trader, Double.MinValue, 0)
    val bestTrader = (1 to generationNum).foldLeft(initialSolution)((best, iter) => {
      val child = CoevolutionBreeding(restart, populationSize, CoevolutionFitness(marketData, 0.05)).breed(best)
      val optimisedChild = if (child.expectedProfit > 0) optimize(child) else child
      val bestOfTwo = if (optimisedChild.expectedProfit > best.expectedProfit) optimisedChild else best
      progress.getOrElse(defaultProgress _)(iter, bestOfTwo, optimisedChild)
      bestOfTwo
    })
    bestTrader
  }
}
