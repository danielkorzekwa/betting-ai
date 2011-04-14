package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator._
import ISimulator._
import HillClimbing._

/** Search for optimal trader using co-evolution based gradient ascent algorithm. Algorithm 6 from Essentials of metaheuristics book 
 *  (http://www.goodreads.com/book/show/9734814-essentials-of-metaheuristics) with a one difference that individuals in population compete 
 *  against each other instead of evaluating them in isolation (chapter 6, page 107,...The entire population participated at the same time in the game...)
 *  
 *  Fitness function for individual (trader) is defined by current expected profit. 
 *
 * @author korzekwad
 * 
 * @param marketData Contains market events that the market simulation/optimisation is executed for. Key - marketId, value - market events
 * @param mutate Takes trader as input and creates mutated trader.
 * @param populationSize Number of individuals in every generation.
 *
 */
case class CoevolutionHillClimbing[T <: ITrader](marketData: MarketData, mutate: (Solution[T]) => T, populationSize: Int) extends HillClimbing[T] {

  def breed(trader: Solution[T]): Solution[T] = {
    /** Born 10 traders.*/
    val population = for (i <- 1 to populationSize) yield mutate(trader)

    /**Create simulation environment.*/
    val simulator = Simulator(0.05)

    /**Run simulation and find the best solution.*/
    val simulationReport = simulator.runSimulation(marketData.data, population.toList, p => {})

    val bestSolution = simulationReport.marketReports match {
      case Nil => Solution(population.head, 0d, 0d)
      case x :: xs => {
        val traders = simulationReport.marketReports.head.traderReports.map(_.trader)
        val tradersFitness: List[Tuple2[RegisteredTrader, Double]] = traders.map(t => t -> simulationReport.totalExpectedProfit(t.userId))
        /** Find best trader [trader, fitness]*/
        val (bestTrader, expectedProfit) = tradersFitness.reduceLeft((a, b) => if (a._2 > b._2) a else b)
        val matchedBetsNum = simulationReport.totalMatchedBetsNum(bestTrader.userId)

        Solution(bestTrader.trader.asInstanceOf[T], expectedProfit, matchedBetsNum)
      }
    }

    /**Return best of children.*/
    bestSolution
  }

}