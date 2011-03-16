package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator._
import ISimulator._

/**Evaluates fitness for population of traders using coevolution algorithm and returns the individual with the highest fitness.
 * 
 * Fitness is a function of market expected profit.
 *
 * @param marketData Historical markets used for simulation and fitness evaluation.
 * @param commission Betting exchange commission.
 * @author korzekwad
 *
 */
case class CoevolutionFitness(marketData: MarketData, commission: Double) {

  /**@see CoevolutionFitness
   * @returns Fitness for the best individual or none if no market data to analyse.
   */
  def fitness[T <: ITrader](population: Seq[T]): Option[Solution[T]] = {
    /**Create simulation environment.*/
    val simulator = Simulator(commission)

    /**Run simulation and find the best solution.*/
    val simulationReport = simulator.runSimulation(marketData.data, population.toList, p => {})

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
}