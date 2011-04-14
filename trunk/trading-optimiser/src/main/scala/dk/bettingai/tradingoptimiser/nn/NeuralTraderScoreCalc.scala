package dk.bettingai.tradingoptimiser.nn

import org.encog.neural.networks.training._
import org.encog.neural.networks.BasicNetwork
import dk.bettingai.marketsimulator._
import dk.bettingai.tradingoptimiser._
import dk.bettingai.marketsimulator.trader._

/**
 * This class represents a Calculate Score function used by Encog Neural Networks Framework to train neural network with genetic algorithm.
 * This function calculates a score for trader running it on a market simulator for a given market data.
 * Trader score is represented by total market expected profit achieved by this trader.
 *
 * @author korzekwad
 *
 * @param marketData Market data that market simulation is executed for.
 * @param createTrader Function that creates a new trader that score is calculated for.
 */
class NeuralTraderScoreCalc(marketData: MarketData, createTrader: (BasicNetwork) => ITrader) extends CalculateScore {

  def calculateScore(network: BasicNetwork): Double = {
    /**Create simulation environment.*/
    val simulator = Simulator(0.05)

    /**Run simulation and find the best solution.*/
    val simulationReport = simulator.runSimulation(marketData.data, createTrader(network) :: Nil, p => {})

    val traderId = simulationReport.marketReports.head.traderReports.head.trader.userId
    val score = simulationReport.totalExpectedProfit(traderId)
    /**Expected profit 0 usually indicates that no bets were placed by trader. This scenario should be always avoided, therefore it has the lowest possible score.*/
    if (score != 0) score else Double.MinValue
  }

  def shouldMinimize(): Boolean = false
}