package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._
import ICoevolutionHillClimbing._
import java.io.File

/** Search for optimal trader using co-evolution based hill climbing gradient algorithm. Algorithm 6 from Essentials of metaheuristics book 
 *  (http://www.goodreads.com/book/show/9734814-essentials-of-metaheuristics) with a one difference that individuals in population compete 
 *  against each other instead of evaluating them in isolation (chapter 6, page 107,...The entire population participated at the same time in the game...)
 *  
 *  Fitness function for individual (trader) is defined by current expected profit. 
 *
 * @author korzekwad
 *
 */
object ICoevolutionHillClimbing {

  /** Data model representing trader implementation and its fitness. 
   *   trader - Trader implementation.
   *   fitness - Current expected profit achieved by this trader.
   **/
	case class Solution[T <: ITrader](trader: T, fitness: Double) {
	  override def toString = "Solution [trader=%s, fitness=%s]".format(trader,fitness)
  }
}
trait ICoevolutionHillClimbing {

  /** Search for optimal trader using co-evolution based gradient hill climbing algorithm, @see ICoevolutionHillClimbing	 
   * 
   * @param Contains market events that the market simulation/optimisation is executed for. Key - marketId, value - market events
   * @param trader Trader to be optimised.
   * @param mutate Takes trader as input and creates mutated trader.
   * @param populationSize Number of individuals in every generation.
   * @param generationNum Number of generations that optimisation is executed for.
   * @param progress The current progress of optimisation, it is called after every generation and returns 
   *                (current number of generation, the best solution so far, best solution for the current generation)
   *                  
   * @return Best trader found.
   **/
  def optimise[T <: ITrader](marketData: Map[Long, File], trader: T, mutate: (T) => T, populationSize: Int,generationNum: Int, progress: (Int, Solution[T], Solution[T]) => Unit): Solution[T]
}