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

    /**Search for best solution by creating a population of traders that are mutated versions of modelTrader and then by competing among them on market simulator.
     * @param modelTrader Model trader used for breeding population.
     * @param iter Current number of generation.
     * @returns The best of two, the best solution in population and modelTrader.
     * */
    def optimise(modelTrader: Solution[T], iter: Int): Solution[T] = {

      /** Born 10 traders.*/
      val population = for (i <- 1 to populationSize) yield restart(modelTrader)

      /**Get best of children.*/
      val bestOfPopulation = CoevolutionFitness(marketData, 0.05).fitness(population)

      /**Get best of children and parent.*/

      val optimisedSolution = if (bestOfPopulation.expectedProfit > 0) optimize(bestOfPopulation) else bestOfPopulation

      val bestOfTwo = if (optimisedSolution.expectedProfit > modelTrader.expectedProfit) optimisedSolution else modelTrader
      progress.getOrElse(defaultProgress _)(iter, bestOfTwo, optimisedSolution)
      bestOfTwo

    }

    /**We assume that initial trader is always the worst one and should never be returned itself.*/
    val initialSolution = Solution(trader, Double.MinValue, 0)
    val bestTrader = (1 to generationNum).foldLeft(initialSolution)((best, iter) => optimise(best, iter))
    bestTrader
    Solution(trader, -1d, -1d)
  }
}
