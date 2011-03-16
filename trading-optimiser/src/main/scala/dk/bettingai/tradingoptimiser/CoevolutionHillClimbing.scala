package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._
import java.io.File
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator._
import ISimulator._
import org.slf4j.LoggerFactory
import scala.collection.immutable.TreeMap

/** Search for optimal trader using co-evolution based hill climbing gradient algorithm. Algorithm 6 from Essentials of metaheuristics book 
 *  (http://www.goodreads.com/book/show/9734814-essentials-of-metaheuristics) with a one difference that individuals in population compete 
 *  against each other instead of evaluating them in isolation (chapter 6, page 107,...The entire population participated at the same time in the game...)
 *  
 *  Fitness function for individual (trader) is defined by current expected profit. 
 *
 * @author korzekwad
 *
 */
object CoevolutionHillClimbing extends ICoevolutionHillClimbing {

  /** Search for optimal trader using co-evolution based gradient hill climbing algorithm, @see ICoevolutionHillClimbing.
   *  It's logging simulation progress using log4j info level.
   * 
   * @param marketDataDir Contains market data for simulation. 
   * @param trader Trader to be optimised.
   * @param mutate Takes trader as input and creates mutated trader.
   * @param populationSize Number of individuals in every generation.
   * @param generationNum Maximum number of generations that optimisation is executed for.
   *                  
   * @return Best trader found.
   **/
  def optimise[T <: ITrader](marketDataDir: String, trader: T, mutate: (Solution[T]) => T, populationSize: Int, generationNum: Int): Solution[T] = {
    val logger = LoggerFactory.getLogger(getClass)

    val progress = (iter: Int, best: Solution[T], current: Solution[T]) => logger.info("Iter number=" + iter + ", bestSoFar=" + best + ", currentBest=" + current)
    optimise(marketDataDir, trader, mutate, populationSize, generationNum, progress)
  }

  /** Search for optimal trader using co-evolution based gradient hill climbing algorithm, @see ICoevolutionHillClimbing	 
   * 
   * @param marketDataDir Directory, which contains market data for simulation. 
   * @param trader Trader to be optimised.
   * @param mutate Takes trader as input and creates mutated trader.
   * @param populationSize Number of individuals in every generation.
   * @param generationNum Maximum number of generations that optimisation is executed for.
   * @param progress The current progress of optimisation, it is called after every generation and returns 
   *                (current number of generation, the best solution so far, best solution for the current generation)
   *                  
   * @return Best trader found.
   **/
  def optimise[T <: ITrader](marketDataDir: String, trader: T, mutate: (Solution[T]) => T, populationSize: Int, generationNum: Int, progress: (Int, Solution[T], Solution[T]) => Unit): Solution[T] = {

    val marketDataSources = TreeMap(new File(marketDataDir).listFiles.filter(_.getName.endsWith(".csv")).map(f => f.getName.split("\\.")(0).toLong -> f): _*)
    optimise(marketDataSources, trader, mutate, populationSize, generationNum, progress)
  }

  /** Search for optimal trader using co-evolution based gradient hill climbing algorithm, @see ICoevolutionHillClimbing	 
   * 
   * @param marketData Contains market events that the market simulation/optimisation is executed for. Key - marketId, value - market events
   * @param trader Trader to be optimised.
   * @param mutate Takes trader as input and creates mutated trader.
   * @param populationSize Number of individuals in every generation.
   * @param generationNum Maximum number of generations that optimisation is executed for.
   * @param progress The current progress of optimisation, it is called after every generation and returns 
   *                (current number of generation, the best solution so far, best solution for the current generation). Default progress is provided.
   *                  
   * @return Best trader found.
   **/
  def optimise[T <: ITrader](marketData: TreeMap[Long, File], trader: T, mutate: (Solution[T]) => T, populationSize: Int, generationNum: Int, progress: (Int, Solution[T], Solution[T]) => Unit): Solution[T] = {

    /**We assume that initial trader is always the worst one and should never be returned itself.*/
    val initialSolution = Solution(trader, Double.MinValue, 0)
    val bestTrader = (1 to generationNum).foldLeft(initialSolution)((best, iter) => {
      val child = CoevolutionBreeding(mutate,populationSize,CoevolutionFitness(MarketData(marketData), 0.05)).breed(best)
      val bestOfTwo = if (child.expectedProfit > best.expectedProfit) child else best
      progress(iter, bestOfTwo, child)
      bestOfTwo
    })
    bestTrader
  }

}