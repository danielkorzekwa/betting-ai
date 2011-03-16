package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._

/** Data model representing trader implementation and its fitness. 
 *   @param trader Trader implementation.
 *   @param expectedProfit Current expected profit achieved by this trader.
 *   @param matchedBetsNumm Total number of matched bets for this trader.
 **/
case class Solution[T <: ITrader](trader: T, expectedProfit: Double, matchedBetsNum: Double) {
  override def toString = "Solution [trader=%s, expectedProfit=%s, matchedBetsNum=%s]".format(trader, expectedProfit, matchedBetsNum)
}