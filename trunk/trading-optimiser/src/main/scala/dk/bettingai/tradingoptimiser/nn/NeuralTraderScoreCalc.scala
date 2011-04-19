package dk.bettingai.tradingoptimiser.nn

import org.encog.neural.networks.training._
import org.encog.neural.networks.BasicNetwork
import dk.bettingai.marketsimulator._
import dk.bettingai.tradingoptimiser._
import dk.bettingai.marketsimulator.trader._

/**
 * This class represents a Calculate Score function used by Encog Neural Networks Framework to train neural network with genetic algorithm.
 * This function calculates a score for trader running it on a market simulator for a given market data.
 * Trader score is represented by wealth function of total market expected profit achieved by this trader.
 *
 * @author korzekwad
 *
 * @param marketData Market data that market simulation is executed for.
 * @param createTrader Function that creates a new trader that score is calculated for.
 * @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)
 */
class NeuralTraderScoreCalc(marketData: MarketData, createTrader: (BasicNetwork) => ITrader, bank:Double) extends CalculateScore {

  def calculateScore(network: BasicNetwork): Double = {
    /**Create simulation environment.*/
    val simulator = Simulator(0.05,bank)

    /**Run simulation and find the best solution.*/
    val simulationReport = simulator.runSimulation(marketData.data, createTrader(network) :: Nil, p => {})

    val traderId = simulationReport.marketReports.head.traderReports.head.trader.userId
    val score = simulationReport.totalWealth(traderId)
    /**Expected profit 0 usually indicates that no bets were placed by trader. This scenario should be always avoided, therefore it has the lowest possible score.*/
    if (!score.isNaN && Math.abs(score)>0.001) score else Double.MinValue
  }

  def shouldMinimize(): Boolean = false
}