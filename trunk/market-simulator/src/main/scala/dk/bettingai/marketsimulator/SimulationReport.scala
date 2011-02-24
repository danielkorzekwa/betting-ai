package dk.bettingai.marketsimulator

import ISimulator._
import dk.bettingai.marketsimulator.risk._
import java.util.Date

/**This class represents the result of market simulation.
 * 
 * @author korzekwad
 *
 */
case class SimulationReport(marketReports: List[MarketReport]) {
    /**Returns total expected profit for betting exchange user id.*/
    def totalExpectedProfit(userId: Int): Double = marketReports.foldLeft(0d)((total, marketReport) => total + marketReport.expectedProfit(userId))
    
    /**Returns total number of matched bets for userId, which were placed on all betting exchange markets.*/
    def totalMatchedBetsNum(userId: Int) = marketReports.foldLeft(0l)((total,marketReport) => total + marketReport.matchedBetsNum(userId))
    
     /**Returns total number of unmatched bets for userId, which were placed on all betting exchange markets.*/
    def totalUnmatchedBetsNum(userId: Int) = marketReports.foldLeft(0l)((total,marketReport) => total + marketReport.unmatchedBetsNum(userId))
  }

case class MarketReport(
    val marketId: Long,
    val marketName: String,
    val eventName: String,
    val marketTime: Date,
    traderReports: List[TraderReport]) {

    /**Returns expected profit on a market for a trader user id.*/
    def expectedProfit(userId: Int): Double = traderReports.find(tr => tr.trader.userId == userId).get.marketExpectedProfit.marketExpectedProfit
    
     /**Returns total number of matched bets for userId, which were placed on a given betting exchange market.*/
    def matchedBetsNum(userId: Int) = traderReports.find(tr => tr.trader.userId == userId).get.matchedBetsNumber
    
     /**Returns total number of unmatched bets for userId, which were placed on a given betting exchange market.*/
    def unmatchedBetsNum(userId: Int) = traderReports.find(tr => tr.trader.userId == userId).get.unmatchedBetsNumber
  }

 case class TraderReport(
    val trader: RegisteredTrader,
    /**Market expected profit based on bets and market probabilities.*/
    val marketExpectedProfit: MarketExpectedProfit,
    val matchedBetsNumber: Long,
    val unmatchedBetsNumber: Long,
    /**Labels for all chart series.*/
    val chartLabels: List[String],
    /**Key - time stamp, value - list of values for all series in the same order as labels.*/
    val chartValues: List[Tuple2[Long, List[Double]]])