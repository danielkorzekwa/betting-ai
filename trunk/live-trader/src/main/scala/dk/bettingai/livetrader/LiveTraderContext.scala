package dk.bettingai.livetrader

import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketcollector.marketservice._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import IMarket._
import Market._
import dk.bettingai.marketsimulator.risk._
import dk.bettingai.marketsimulator.risk._
import dk.bettingai.marketsimulator.betex.BetUtil._
import dk.bettingai.marketsimulator._
import scala.collection._
import com.espertech.esper.client._
import java.util.Date
import dk.bettingai.marketcollector.marketservice._
import IMarketService._
import dk.bettingai.marketsimulator.risk._
import org.apache.commons.io.FileUtils
import java.io.File
import dk.bettingai.marketsimulator.reporting._

case class LiveTraderContext(marketDetails: MarketDetails, marketService: IMarketService, commission: Double, liveTrader: LiveTrader) extends ITraderContext {

  lazy val userId: Int = throw new UnsupportedOperationException("Not implemented yet")
  val marketId: Long = marketDetails.marketId
  val marketName: String = marketDetails.marketName
  val eventName: String = marketDetails.menuPath
  val numOfWinners: Int = marketDetails.numOfWinners
  val marketTime: Date = marketDetails.marketTime
  val runners: List[IRunner] = marketDetails.runners.map(r => new Runner(r.runnerId, r.runnerName))

  private var _eventTimestamp = -1l

  /**Cache.*/
  private var cachedBestPrices: Option[Map[Long, List[IRunnerPrice]]] = None
  private var marketTradedVolume: Option[Map[Long, IRunnerTradedVolume]] = None
  private var marketExpectedProfit: Option[MarketExpectedProfit] = None

  /** key - timestamp, value Map[chartLabel,value]*/
  private val chartData = collection.mutable.LinkedHashMap[Long, collection.mutable.Map[String, Double]]()
  private val chartLabels = collection.mutable.LinkedHashSet[String]()

  private val userBetsState = UserBets(marketService.getUserBets(marketId, None))

  /**Time stamp of market event */
  def getEventTimestamp: Long = _eventTimestamp
  def setEventTimestamp(eventTimestamp: Long) = {
    /**clear cache.*/
    cachedBestPrices = None
    marketTradedVolume = None
    marketExpectedProfit = None

    /**update UserBets with matched bets.*/
    val matchedSince = new Date(userBetsState.getLatestMatchedDate + 1)
    val recentlyMatchedBets = marketService.getUserMatchedBets(marketId, matchedSince)
    recentlyMatchedBets.foreach(b => userBetsState.betMatched(b))

    _eventTimestamp = eventTimestamp
  }
  /**
   * Add chart value to time line chart
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
  def saveChart(chartFilePath: String) {

    val chartValues = for {
      (timestamp, values) <- chartData
      val timestampValues = chartLabels.toList.map(l => values.getOrElse(l, Double.NaN))

    } yield Tuple2(timestamp, timestampValues)
    val marketReport = MarketReport(marketId, marketName, eventName, marketTime, TraderReport(null, null, -1, -1, chartLabels.toList, chartValues.toList) :: Nil)

    val formmatedReport = ReportGenerator.generateReport(marketReport :: Nil)
    val reportFile = new File(chartFilePath)
    FileUtils.writeStringToFile(reportFile, formmatedReport.toString)
  }

  /**
   * Returns best toBack/toLay prices for market runner.
   * Element 1 - best price to back, element 2 - best price to lay
   * Double.NaN is returned if price is not available.
   * @return
   */
  def getBestPrices(runnerId: Long): Tuple2[IRunnerPrice, IRunnerPrice] = getBestPrices()(runnerId)

  /**
   * Returns best toBack/toLay prices for market.
   *
   * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
   */
  def getBestPrices(): Map[Long, Tuple2[IRunnerPrice, IRunnerPrice]] = {
    if (cachedBestPrices.isEmpty) {
      val marketPrices = marketService.getMarketPrices(marketId)
      if (marketPrices.inPlayDelay > 0) throw new IllegalStateException("Market is in play.")
      else cachedBestPrices = Option(marketPrices.runnerPrices)
    }

    def toBestPrices(runnerPrices: List[IRunnerPrice]): Tuple2[IRunnerPrice, IRunnerPrice] = {
      val bestToBack = runnerPrices.filter(p => p.totalToBack > 0).reduceLeft((a, b) => if (a.price > b.price) a else b)
      val bestToLay = runnerPrices.filter(p => p.totalToLay > 0).reduceLeft((a, b) => if (a.price < b.price) a else b)

      Tuple2(bestToBack, bestToLay)
    }

    val bestPrices = cachedBestPrices.get.mapValues(prices => toBestPrices(prices))
    bestPrices
  }

  /**
   * Places a bet on a betting exchange market.
   *
   * @param betSize
   * @param betPrice
   * @param betType
   * @param runnerId
   *
   * @return The bet that was placed.
   */
  def placeBet(betSize: Double, betPrice: Double, betType: BetTypeEnum, runnerId: Long): IBet = {
    val bet = marketService.placeBet(betSize, betPrice, betType, marketId, runnerId)
    userBetsState.betPlaced(bet)
    bet
  }

  /**
   * Places a bet on a betting exchange market.
   *
   * @param betSizeLimit Total user unmatched volume that should be achieved after calling this method.
   * For example is unmatched volume is 2 and betSizeLimit is 5 then bet with bet size 3 is placed.
   * @param betPrice
   * @param betType
   * @param runnerId
   *
   * @return The bet that was placed or None if nothing has been placed.
   */
  def fillBet(betSizeLimit: Double, betPrice: Double, betType: BetTypeEnum, runnerId: Long): Option[IBet] = {

    val userBets = userBetsState.getUserBets(marketId, Option(U))
    val filteredBets = userBets.filter(b => b.betType == betType && b.betPrice == betPrice && b.runnerId == runnerId)

    val fillBetSize = betSizeLimit - totalStake(filteredBets)
    if (fillBetSize >= 2) Option(placeBet(fillBetSize, betPrice, betType, runnerId)) else None
  }

  /**
   * Cancels a bet on a betting exchange market.
   *
   * @param betId Unique id of a bet to be cancelled.
   *
   * @return amount cancelled
   */
  def cancelBet(betId: Long): Double = throw new UnsupportedOperationException("Not implemented yet")

  /**
   * Place hedge bet on a market runner to make ifwin/iflose profits even. Either back or lay bet is placed on best available price.
   *
   * @param runnerId
   *
   * @return Hedge bet that was placed or none if no hedge bet was placed.
   */
  def placeHedgeBet(runnerId: Long): Option[IBet] = throw new UnsupportedOperationException("Not implemented yet")

  /**
   * Returns all bets placed by user on that market.
   *
   * @param matchedBetsOnly If true then matched bets are returned only,
   * otherwise all unmatched and matched bets for user are returned.
   */
  def getBets(matchedBetsOnly: Boolean): List[IBet] = {
	  matchedBetsOnly match {
	 	  case true => userBetsState.getUserBets(marketId,Option(M))
	 	  case false => userBetsState.getUserBets(marketId)
	  }
  }
  
   /**Returns all matched and unmatched portions of a bet.*/
  def getBet(betId:Long): List[IBet] = throw new UnsupportedOperationException("Not implemented yet.")

  /**
   * Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange.
   *  Prices with zero volume are not returned by this method.
   *
   * @param runnerId Unique runner id that runner prices are returned for.
   * @return
   */
  def getRunnerPrices(runnerId: Long): List[IRunnerPrice] = throw new UnsupportedOperationException("Not implemented yet")

  /**Returns total traded volume for all prices on all runners in a market.*/
  def getRunnerTradedVolume(runnerId: Long): IRunnerTradedVolume = throw new UnsupportedOperationException("Not implemented yet")

  /**Returns total traded volume for a given runner.*/
  def getTotalTradedVolume(runnerId: Long): Double = {
    if (marketTradedVolume.isEmpty) {
      marketTradedVolume = Option(marketService.getMarketTradedVolume(marketId))
    }
    marketTradedVolume.get(runnerId).totalTradedVolume
  }

   /** @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)*/
  def risk(bank:Double): MarketExpectedProfit = {
    if (marketExpectedProfit.isEmpty) {
      val matchedUserBets = userBetsState.getUserBets(marketId, Option(M))
      val probs = ProbabilityCalculator.calculate(getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), 1)
      marketExpectedProfit = Option(ExpectedProfitCalculator.calculate(matchedUserBets, probs, commission,bank))
    }

    marketExpectedProfit.get
  }

  /**see Kelly Criterion - http://en.wikipedia.org/wiki/Kelly_criterion.*/
  def wealth(bank: Double): MarketExpectedProfit = throw new UnsupportedOperationException("Not implemented yet")

  /**
   * Registers new trader and return trader context.
   * This context can be used to trigger some custom traders that are registered manually by a master trader,
   * e.g. when testing some evolution algorithms for which more than one trader is required.
   * @return trader context
   */
  def registerTrader(): ITraderContext = throw new UnsupportedOperationException("Not implemented yet")

  /**
   * Registers Esper(http://esper.codehaus.org/) Event Processing Network.
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
  def registerEPN(getEventTypes: => (Map[String, Map[String, Object]]), getEPLStatements: => Map[String, String], publish: (EPServiceProvider) => Unit): Boolean = {
    liveTrader.registerEPN(getEventTypes, getEPLStatements, publish)
  }

  /**
   * Returns registered EPL statement for a given eplID.
   * It could be used to iterate through the current state of EPL statement, e.g. get some delta or avg value from EPN.
   *
   * @param eplID
   * @return EPStatement
   */
  def getEPNStatement(eplID: String): EPStatement = liveTrader.getEPNStatement(eplID)

}