package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._

/** Search for optimal trader using co-evolution based hill climbing gradient with random restarts algorithm. Algorithm 10 from Essentials of metaheuristics book 
 *  (http://www.goodreads.com/book/show/9734814-essentials-of-metaheuristics) with a one difference that individuals in population compete 
 *  against each other instead of evaluating them in isolation (chapter 6, page 107,...The entire population participated at the same time in the game...)
 *  
 *  Fitness function for individual (trader) is defined by current expected profit. 
 *
 * @author korzekwad
 *
 */
trait ICoevolutionHillClimbingWithRestarts {

  type TRADER <: ITrader
  /** Search for optimal trader using co-evolution based gradient hill climbing algorithm with random restarts, @see ICoevolutionHillClimbingWithRestarts	 
   *   
   *   @param marketData Contains market events that the market simulation/optimisation is executed for. Key - marketId, value - market events
   * @param trader Trader to be optimised.
   * @param mutate Takes trader as input and creates mutated trader.
   * @param populationSize Number of individuals in every generation.
   * @param generationNum Number of generations that optimisation is executed for.
   *                (current number of generation, the best solution so far, best solution for the current generation)
   * @param optimise Optimise best candidate of population.
   * @param progress The current progress of optimisation, it is called after every generation and returns                 
   * @return Best trader found.
   **/
  def optimise(marketData: MarketData,trader: TRADER, mutate: (Solution[TRADER]) => TRADER, populationSize: Int, generationNum: Int, optimize: (Solution[TRADER]) => Solution[TRADER], progress: Option[(Int, Solution[TRADER], Solution[TRADER]) => Unit]): Solution[TRADER]
}