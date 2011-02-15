package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._
import ICoevolutionHillClimbing._
import java.io.File
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator._

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

  /** Search for optimal trader using co-evolution based gradient hill climbing algorithm, @see ICoevolutionHillClimbing	 
   * 
   * @param Contains market events that the market simulation/optimisation is executed for. Key - marketId, value - market events
   * @param trader Trader to be optimised.
   * @param mutate Takes trader as input and creates mutated trader.
   * @param populationSize Number of individuals in every generation.
   * @param generationNum Maximum number of generations that optimisation is executed for.
   * @param progress The current progress of optimisation, it is called after every generation and returns 
   *                (current number of generation, the best solution so far, best solution for the current generation)
   *                  
   * @return Best trader found.
   **/
  def optimise[T <: ITrader](marketData: Map[Long, File], trader: T, mutate: (T) => T, populationSize: Int, generationNum: Int, progress: (Int, Solution[T], Solution[T]) => Unit): Solution[T] = {

    /**Search for best solution by creating a population of traders that are mutated versions of modelTrader and then by competing among them on market simulator.
     * @param modelTrader Model trader used for breeding population.
     * @param iter Current number of generation.
     * @returns The best of two, the best solution in population and modelTrader.
     * */
    def optimise(modelTrader: Solution[T], iter: Int): Solution[T] = {

      /**Evaluate fitness for all individuals.
       * @returns Fitness for the best individual.
       */
      def fitness(population: Seq[T]): Solution[T] = {
        /**Create simulation environment.*/
        val betex = new Betex()
        val marketEventProcessor = new MarketEventProcessorImpl(betex)
        val commission = 0.05
        val simulator = new Simulator(marketEventProcessor, betex, commission)

        /**Run simulation and find the best solution.*/
        val simulationReport = simulator.runSimulation(marketData, population.toList, p => {})
        val traders = simulationReport.marketReports.head.traderReports.map(_.trader)
        val tradersFitness: List[Tuple2[ITrader, Double]] = traders.map(t => t.trader -> simulationReport.totalExpectedProfit(t.userId))
        /** Find best trader [trader, fitness]*/
        val best = tradersFitness.reduceLeft((a, b) => if (a._2 > b._2) a else b)
        Solution(best._1.asInstanceOf[T], best._2)
      }

      /** Born 10 traders.*/
      val population = for (i <- 1 to populationSize) yield mutate(modelTrader.trader)

      val bestOfPopulation = fitness(population)
      val best = if (bestOfPopulation.fitness > modelTrader.fitness) bestOfPopulation else modelTrader
      progress(iter, best, bestOfPopulation)
      best
    }

    /**We assume that initial trader is always the worst one and should never be returned itself.*/
    val initialSolution = Solution(trader, Double.MinValue)
    val bestTrader = (1 to generationNum).foldLeft(initialSolution)((best, iter) => optimise(best, iter))
    bestTrader
  }

}