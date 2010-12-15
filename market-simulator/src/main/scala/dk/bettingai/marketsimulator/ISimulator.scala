package dk.bettingai.marketsimulator

import ISimulator._
import scala.io._
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.risk._
import java.io.File
import dk.bettingai.marketsimulator.trader.ITrader._
import dk.bettingai.marketsimulator.betex.api._
/**This trait represents a simulator that processes market events, analyses trader implementation and returns analysis report for trader implementation.
 * 
 * @author korzekwad
 *
 */
object ISimulator {
	trait IMarketRiskReport {
		val marketId:Long
		val marketName:String
		val eventName:String
		/**Market expected profit based on bets and market probabilities.*/
		val marketExpectedProfit:MarketExpectedProfit
		val matchedBetsNumber:Long
		val unmatchedBetsNumber:Long
		
		/**Labels for all chart series.*/
		val chartLabels:List[String]
		
		/**Key - time stamp, value - list of values for all series in the same order as labels.*/
		val chartValues:List[Tuple2[Long,List[Double]]]
	}
}
trait ISimulator {

	/** Processes market events, analyses trader implementation and returns analysis report for trader implementation.
 * 
 * @param marketDataContains market events that the market simulation is executed for. Key - marketId, value - market events
 * @param trader
 * @param p Progress listener. Value between 0% and 100% is passed as an function argument.
 */
	def runSimulation(marketData:Map[Long,File], trader:ITrader,p: (Int) => Unit):List[IMarketRiskReport] 
	
	 /**Registers new trader and return trader context. 
     * This context can be used to trigger some custom traders that are registered manually by a master trader, 
     * e.g. when testing some evolution algorithms for which more than one trader is required.
     * @return trader context
     */
    def registerTrader(market:IMarket):ITraderContext
}