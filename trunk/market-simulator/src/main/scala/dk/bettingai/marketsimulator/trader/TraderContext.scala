package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import IMarket._
import dk.bettingai.marketsimulator.risk._
import dk.bettingai.marketsimulator.risk._
import dk.bettingai.marketsimulator.betex.BetUtil._
import dk.bettingai.marketsimulator._
import scala.collection._
import com.espertech.esper.client._
/**Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
 * 
 * @author korzekwad
 *
 * @param commision Commission on winnings in percentage.
 * @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)
 */
class TraderContext(nextBetId: => Long, val userId: Int, market: IMarket, val commission: Double, val bank: Double,simulator: Simulator, marketSimActor:MarketSimActor) extends ITraderContext {
  val marketId = market.marketId
  val marketName = market.marketName
  val eventName = market.eventName
  val numOfWinners = market.numOfWinners
  val marketTime = market.marketTime
  val runners = market.runners
  private var _eventTimestamp = -1l

  /**Creates expected profit engine and register listener on matched bets.*/
  private val expectedProfitEngine = {
    val engine = ExpectedProfitEngine()
    market.addMatchedBetsListener(bet => bet.userId == userId, bet => engine.addBet(bet.betSize,bet.betPrice,bet.betType,bet.runnerId))
    engine
  }

  def setEventTimestamp(eventTimestamp: Long) { _eventTimestamp = eventTimestamp }
  def getEventTimestamp = _eventTimestamp

  /** key - timestamp, value Map[chartLabel,value]*/
  val chartData = collection.mutable.LinkedHashMap[Long, collection.mutable.Map[String, Double]]()
  val chartLabels = collection.mutable.LinkedHashSet[String]()
  /**Returns labels for all chart series.*/
  def getChartLabels: List[String] = chartLabels.toList

  /**Returns chart values for all time series in the same order as chart labels. 
   * Key - time stamp, value - list of values for all series in the same order as labels.*/
  def getChartValues: List[Tuple2[Long, List[Double]]] = {
    val chartValues = for {
      (timestamp, values) <- chartData
      val timestampValues = chartLabels.toList.map(l => values.getOrElse(l, Double.NaN))

    } yield Tuple2(timestamp, timestampValues)
    chartValues.toList
  }

  /**Add chart value to time line chart
   * 
   * @param label Label of chart series
   * @param value Value to be added to chart series
   */
  def addChartValue(label: String, value: Double) = {
    chartLabels += label
    val timestampChartValues = chartData.getOrElseUpdate(getEventTimestamp, collection.mutable.Map[String, Double]())
    timestampChartValues += label -> value
  }

   /**Saves html chart to file.*/
   def saveChart(chartFilePath:String) {throw new UnsupportedOperationException("Not implemented yet")}
  
  /** Places a bet on a betting exchange market.
   * 
   * @param betSize
   * @param betPrice
   * @param betType
   * @param runnerId
   * 
   * @return The bet that was placed.
   */
  def placeBet(betSize: Double, betPrice: Double, betType: BetTypeEnum, runnerId: Long): IBet = {
    val betId = nextBetId
    market.placeBet(betId, userId, betSize, betPrice, betType, runnerId,getEventTimestamp)
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
  def fillBet(betSizeLimit: Double, betPrice: Double, betType: BetTypeEnum, runnerId: Long): Option[IBet] = {
    val betId = nextBetId

    val bets = market.getBets(userId, U, betType, betPrice, runnerId)
    val fillBetSize = betSizeLimit - totalStake(bets)
    if (fillBetSize >= 2) Option(placeBet(fillBetSize, betPrice, betType, runnerId)) else None
  }

  /** Cancels a bet on a betting exchange market.
   * 
   * @param betId Unique id of a bet to be cancelled.
   * 
   * @return amount cancelled
   */
  def cancelBet(betId: Long): Double = {
    market.cancelBet(betId)
  }

  /**Place hedge bet on a market runner to make ifwin/iflose profits even. Either back or lay bet is placed on best available price.
   * 
   * @param runnerId
   * 
   * @return Hedge bet that was placed or none if no hedge bet was placed.
   */
  def placeHedgeBet(runnerId: Long): Option[IBet] = {
    val riskReport = risk(bank)
    val ifWin = riskReport.ifWin(runnerId)
    val ifLose = riskReport.ifLose(runnerId)
    val bestPrices = getBestPrices(runnerId)

    if (ifWin > ifLose && !bestPrices._2.price.isNaN) {
      val betPrice = bestPrices._2.price
      val betSize = (ifWin - ifLose) / betPrice
      Option(placeBet(betSize, betPrice, LAY, runnerId))
    } else if (ifLose > ifWin && !bestPrices._1.price.isNaN) {
      val betPrice = bestPrices._1.price
      val betSize = (ifLose - ifWin) / betPrice
      Option(placeBet(betSize, betPrice, BACK, runnerId))
    } else None
  }

  /**Returns best toBack/toLay prices for market runner.
   * Element 1 - best price to back, element 2 - best price to lay
   * Double.NaN is returned if price is not available.
   * @return 
   * */
  def getBestPrices(runnerId: Long): Tuple2[IRunnerPrice, IRunnerPrice] = market.getBestPrices(runnerId)

  /**Returns best toBack/toLay prices for market.
   * 
   * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
   */
  def getBestPrices(): Map[Long, Tuple2[IRunnerPrice, IRunnerPrice]] = {
    market.getBestPrices()
  }

  /**Returns all bets placed by user on that market.
   *
   *@param matchedBetsOnly If true then matched bets are returned only, 
   * otherwise all unmatched and matched bets for user are returned.
   */
  def getBets(matchedBetsOnly: Boolean): List[IBet] = market.getBets(userId, matchedBetsOnly)

  /**Returns all matched and unmatched portions of a bet.*/
  def getBet(betId:Long): List[IBet] = market.getBets(userId).filter(b => b.betId == betId)
  
  /** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
   *  Prices with zero volume are not returned by this method.
   * 
   * @param runnerId Unique runner id that runner prices are returned for.
   * @return
   */
  def getRunnerPrices(runnerId: Long): List[IRunnerPrice] = market.getRunnerPrices(runnerId)

  /**Returns total traded volume for all prices on all runners in a market.*/
  def getRunnerTradedVolume(runnerId: Long): IRunnerTradedVolume = market.getRunnerTradedVolume(runnerId)

  /**Returns total traded volume for a given runner.*/
  def getTotalTradedVolume(runnerId: Long): Double = market.getTotalTradedVolume(runnerId)

  /** @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)*/
  def risk(bank: Double): MarketExpectedProfit = {
    val probs = ProbabilityCalculator.calculate(getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), 1)
    expectedProfitEngine.calculateExpectedProfit(probs, commission,bank)
  }

  /**Registers new trader and return trader context. 
   * This context can be used to trigger some custom traders that are registered manually by a master trader, 
   * e.g. when testing some evolution algorithms for which more than one trader is required.
   * @return trader context
   */
  def registerTrader(): ITraderContext = {
    val ctx = simulator.registerTrader(market,marketSimActor)
    ctx.setEventTimestamp(_eventTimestamp)
    ctx
  }

      /**Registers Esper(http://esper.codehaus.org/) Event Processing Network.
     * 
     * If two EPNs are registered for the same market, e.g. by two traders, the second one is ignored. It means that all traders must reuse the same EPN.
     *  
     * @param getEventTypes This function returns the list of event types that form Event Processing Network. Map[eventTypeName, [eventAttributeName, eventAttributeType]].
     * 
     * @param getEPLStatements This function returns the list of all Event Processing Language statements that form Event Processing Network. Map[eplID,eplQuery]
     * 
     * @param publish This function is called every time when market event time stamp progresses. It should publish all required events on Event Processing Network.
     * 
     * @return true if Event Processing Network registration finishes successfully, false is EPN is already registered.
     */
    def registerEPN(getEventTypes: => (Map[String,Map[String,Object]]), getEPLStatements: => Map[String,String],publish: (EPServiceProvider) => Unit):Boolean = 
    	marketSimActor.registerEPN(getEventTypes, getEPLStatements, publish)
    
     /**Returns registered EPL statement for a given eplID. 
     * It could be used to iterate through the current state of EPL statement, e.g. get some delta or avg value from EPN.
     * 
     * @param eplID
     * @return EPStatement
     * */
    def getEPNStatement(eplID:String):EPStatement = marketSimActor.getEPNStatement(eplID)
    
     /**Register listener on those matched bets, which match filter criteria
   * 
   * @param filter If true then listener is triggered for this bet.
   * @param listener
   */
  def addMatchedBetsListener(filter: (IBet) => Boolean, listener: (IBet) => Unit) = market.addMatchedBetsListener(filter,listener)
  
}
