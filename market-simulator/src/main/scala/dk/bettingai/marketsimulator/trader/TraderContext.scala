package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import ITrader._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import IMarket._
import dk.bettingai.marketsimulator.risk._
import dk.bettingai.marketsimulator.risk._
import dk.bettingai.marketsimulator.betex.BetUtil._
import dk.bettingai.marketsimulator._

/**Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
 * 
 * @author korzekwad
 *
 * @param commision Commission on winnings in percentage.
 */
class TraderContext(nextBetId: => Long,val userId:Int, market:IMarket, val commission:Double, simulator:Simulator) extends ITraderContext {
	val marketId = market.marketId
	val marketName = market.marketName
	val eventName = market.eventName
	val numOfWinners = market.numOfWinners
	val marketTime = market.marketTime
	val runners = market.runners
  private var _eventTimestamp = -1l
  
  def setEventTimestamp(eventTimestamp:Long) {_eventTimestamp = eventTimestamp}
  def getEventTimestamp = _eventTimestamp

	/** key - timestamp, value Map[chartLabel,value]*/
	val chartData = collection.mutable.LinkedHashMap[Long,collection.mutable.Map[String,Double]]()
	val chartLabels = collection.mutable.LinkedHashSet[String]()
	/**Returns labels for all chart series.*/
	def getChartLabels:List[String] = chartLabels.toList

	/**Returns chart values for all time series in the same order as chart labels. 
	 * Key - time stamp, value - list of values for all series in the same order as labels.*/
	def getChartValues:List[Tuple2[Long,List[Double]]] = {
		val chartValues = for {
			(timestamp,values) <- chartData
			val timestampValues = chartLabels.toList.map(l => values.getOrElse(l,Double.NaN))
			
		} yield Tuple2(timestamp,timestampValues)
		chartValues.toList
	}

	/**Add chart value to time line chart
	 * 
	 * @param label Label of chart series
	 * @param value Value to be added to chart series
	 */
	def addChartValue(label:String, value:Double) = {
		chartLabels += label
		val timestampChartValues = chartData.getOrElseUpdate(getEventTimestamp,collection.mutable.Map[String,Double]())
		timestampChartValues += label -> value
	}
	
	/** Places a bet on a betting exchange market.
	 * 
	 * @param betSize
	 * @param betPrice
	 * @param betType
	 * @param runnerId
	 * 
	 * @return The bet that was placed.
	 */
	def placeBet(betSize:Double, betPrice:Double, betType:BetTypeEnum, runnerId:Long):IBet = {
		val betId = nextBetId
		market.placeBet(betId,userId,betSize,betPrice,betType,runnerId)
	}
	
	/** Places a bet on a betting exchange market.
	 * 
	 * @param betSizeLimit Total user unmatched volume that should be achieved after calling this method. 
	 * For example is unmatched volume is 2 and betSizeLimit is 5 then bet with bet size 3 is placed. Minimum bet size is 2 
	 * @param betPrice
	 * @param betType
	 * @param runnerId
	 * 
	 * @return The bet that was placed or None if nothing has been placed.
	 */
	def fillBet(betSizeLimit:Double, betPrice:Double, betType:BetTypeEnum, runnerId:Long):Option[IBet] = {
		val betId = nextBetId
		
		val bets = getBets(false).filter(b=> b.betStatus==U && b.betPrice==betPrice && b.betType==betType && b.runnerId==runnerId)
		val fillBetSize = betSizeLimit-totalStake(bets)
		if(fillBetSize>=2) Option(placeBet(fillBetSize,betPrice,betType,runnerId)) else None
	}
	
	/** Cancels a bet on a betting exchange market.
	 * 
	 * @param betId Unique id of a bet to be cancelled.
	 * 
	 * @return amount cancelled
	*/
	def cancelBet(betId:Long):Double = {
		market.cancelBet(betId)
	}

	/**Place hedge bet on a market runner to make ifwin/iflose profits even. Either back or lay bet is placed on best available price.
	 * 
	 * @param runnerId
	 * 
	 * @return Hedge bet that was placed or none if no hedge bet was placed.
	 */
	def placeHedgeBet(runnerId:Long):Option[IBet] = {
		val riskReport = risk()
		val ifWin = riskReport.ifWin(runnerId)
		val ifLose = riskReport.ifLose(runnerId)
		val bestPrices = getBestPrices(runnerId)
		
		if(ifWin>ifLose && !bestPrices._2.price.isNaN) {
			val betPrice = bestPrices._2.price
			val betSize = (ifWin-ifLose)/betPrice
			Option(placeBet(betSize,betPrice,LAY,runnerId))
		}
		else if(ifLose>ifWin && !bestPrices._1.price.isNaN) {
			val betPrice = bestPrices._1.price
			val betSize = (ifLose-ifWin)/betPrice
			Option(placeBet(betSize,betPrice,BACK,runnerId))
		}
		else None
	}
	
	/**Returns best toBack/toLay prices for market runner.
	 * Element 1 - best price to back, element 2 - best price to lay
	 * Double.NaN is returned if price is not available.
	 * @return 
	 * */
	def getBestPrices(runnerId: Long): Tuple2[IRunnerPrice,IRunnerPrice] = market.getBestPrices(runnerId)

	/**Returns best toBack/toLay prices for market.
	 * 
	 * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
	 */
	def getBestPrices():Map[Long,Tuple2[IRunnerPrice,IRunnerPrice]] = {
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
			val probs = ProbabilityCalculator.calculate(getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), 1)
			ExpectedProfitCalculator.calculate(matchedBets, probs,commission)
	}
	
	 /**Registers new trader and return trader context. 
     * This context can be used to trigger some custom traders that are registered manually by a master trader, 
     * e.g. when testing some evolution algorithms for which more than one trader is required.
     * @return trader context
     */
    def registerTrader():ITraderContext = {
    	val ctx = simulator.registerTrader(market)
    	ctx.setEventTimestamp(_eventTimestamp)
    	ctx
    }

}
