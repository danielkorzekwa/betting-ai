package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import ITrader._
import IBet.BetTypeEnum._
import IMarket._
import dk.bettingai.marketsimulator.risk._
import dk.bettingai.marketsimulator.risk._

/**Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
 * 
 * @author korzekwad
 *
 * @param commision Commission on winnings in percentage.
 */
class TraderContext(nextBetId: => Long,userId:Int, market:IMarket, commission:Double) extends ITraderContext {
	val marketId = market.marketId
	val marketName = market.marketName
	val eventName = market.eventName
	val numOfWinners = market.numOfWinners
	val marketTime = market.marketTime
	val runners = market.runners

	var chartLabels = List[String]()
	/**Key - time stamp, value - list of values for all series in the same order as labels.*/
	val chartData = new scala.collection.mutable.ListBuffer[Tuple2[Long,List[Double]]]()

	/**Returns labels for all chart series.*/
	def getChartLabels:List[String] = chartLabels

	/**Set labels for all chart series.*/
	def setChartLabels(chartLabels:List[String]) {this.chartLabels = chartLabels}

	/**Returns chart values for all time series in the same order as chart labels. 
	 * Key - time stamp, value - list of values for all series in the same order as labels.*/
	def getChartValues:List[Tuple2[Long,List[Double]]] = chartData.toList

	/**Add chart values to time line chart. Key - time stamp, value - list of values for all series in the same order as labels.*/
	def addChartValues(chartValues:Tuple2[Long,List[Double]]) {chartData += chartValues}


	/** Places a bet on a betting exchange market.
	 * 
	 * @param betSize
	 * @param betPrice
	 * @param betType
	 * @param runnerId
	 */
	def placeBet(betSize:Double, betPrice:Double, betType:BetTypeEnum, runnerId:Long) {
		market.placeBet(nextBetId,userId,betSize,betPrice,betType,runnerId)
	}

	/**Returns best toBack/toLay prices for market runner.
	 * Element 1 - best price to back, element 2 - best price to lay
	 * Double.NaN is returned if price is not available.
	 * @return 
	 * */
	def getBestPrices(runnerId: Long): Tuple2[Double,Double] = market.getBestPrices(runnerId)

	/**Returns best toBack/toLay prices for market.
	 * 
	 * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
	 */
	def getBestPrices():Map[Long,Tuple2[Double,Double]] = {
			market.getBestPrices()
	}

	/**Returns all bets placed by user on that market.
	 *
	 *@param matchedBetsOnly If true then matched bets are returned only, 
	 * otherwise all unmatched and matched bets for user are returned.
	 */
	def getBets(matchedBetsOnly:Boolean):List[IBet] = market.getBets(userId,matchedBetsOnly)

	/** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
	 *  Prices with zero volume are not returned by this method.
	 * 
	 * @param runnerId Unique runner id that runner prices are returned for.
	 * @return
	 */
	def getRunnerPrices(runnerId:Long):List[IRunnerPrice] = market.getRunnerPrices(runnerId)

	/**Returns total traded volume for all prices on all runners in a market.*/
	def getRunnerTradedVolume(runnerId:Long): IRunnerTradedVolume = market.getRunnerTradedVolume(runnerId)

	def risk():MarketExpectedProfit = {
			val matchedBets = getBets(true)
			val probs = ProbabilityCalculator.calculate(getBestPrices, 1)
			ExpectedProfitCalculator.calculate(matchedBets, probs,commission)
	}

}
