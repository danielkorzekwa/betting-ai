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
   *                (current number of generation, the best solution so far, best solution for the current generation)
   *                  
   * @return Best trader found.
   **/
  def optimise[T <: ITrader](marketData: TreeMap[Long, File], trader: T, mutate: (Solution[T]) => T, populationSize: Int, generationNum: Int, progress: (Int, Solution[T], Solution[T]) => Unit): Solution[T] = {

    /**Search for best solution by creating a population of traders that are mutated versions of modelTrader and then by competing among them on market simulator.
     * @param modelTrader Model trader used for breeding population.
     * @param iter Current number of generation.
     * @returns The best of two, the best solution in population and modelTrader.
     * */
    def optimise(modelTrader: Solution[T], iter: Int): Solution[T] = {

      /**Evaluate fitness for all individuals.
       * @returns Fitness for the best individual or none if no market data to analyse.
       */
      def fitness(population: Seq[T]): Option[Solution[T]] = {
        /**Create simulation environment.*/
        val commission = 0.05
        val simulator = Simulator(commission)

        /**Run simulation and find the best solution.*/
        val simulationReport = simulator.runSimulation(marketData, population.toList, p => {})

        val bestSolution = simulationReport.marketReports match {
          case Nil => None
          case x :: xs => {
            val traders = simulationReport.marketReports.head.traderReports.map(_.trader)
            val tradersFitness: List[Tuple2[RegisteredTrader, Double]] = traders.map(t => t -> simulationReport.totalExpectedProfit(t.userId))
            /** Find best trader [trader, fitness]*/
            val (bestTrader, expectedProfit) = tradersFitness.reduceLeft((a, b) => if (a._2 > b._2) a else b)
            val matchedBetsNum = simulationReport.totalMatchedBetsNum(bestTrader.userId)

            Option(Solution(bestTrader.trader.asInstanceOf[T], expectedProfit, matchedBetsNum))
          }
        }

        bestSolution
      }

      /** Born 10 traders.*/
      val population = for (i <- 1 to populationSize) yield mutate(modelTrader)

      /**Get best of children.*/
      val bestOfPopulation = fitness(population)

      /**Get best of children and parent.*/
      val best: Solution[T] = bestOfPopulation match {
        case None => modelTrader
        case Some(solution) => {
          val bestOfTwo = if (solution.expectedProfit > modelTrader.expectedProfit) solution else modelTrader
          progress(iter, bestOfTwo, solution)
          bestOfTwo
        }
      }
      best
    }

    /**We assume that initial trader is always the worst one and should never be returned itself.*/
    val initialSolution = Solution(trader, Double.MinValue, 0)
    val bestTrader = (1 to generationNum).foldLeft(initialSolution)((best, iter) => optimise(best, iter))
    bestTrader
  }

}