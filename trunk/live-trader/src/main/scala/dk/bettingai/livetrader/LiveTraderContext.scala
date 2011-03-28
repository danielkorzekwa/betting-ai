package dk.bettingai.livetrader

import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketcollector.marketservice._
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
import java.util.Date
import dk.bettingai.marketcollector.marketservice._
import IMarketService._

case class LiveTraderContext(marketDetails:MarketDetails, marketService: IMarketService) extends ITraderContext {

    lazy val userId: Int = throw new UnsupportedOperationException("Not implemented yet")
    val marketId: Long = marketDetails.marketId
    val marketName: String = marketDetails.marketName
    val eventName: String = marketDetails.menuPath
    val numOfWinners: Int = marketDetails.numOfWinners 
    val marketTime: Date = marketDetails.marketTime 
    lazy val runners: List[IRunner] = throw new UnsupportedOperationException("Not implemented yet")

    lazy val commission: Double = throw new UnsupportedOperationException("Not implemented yet")

    /**Time stamp of market event */
    def getEventTimestamp: Long = throw new UnsupportedOperationException("Not implemented yet")
    def setEventTimestamp(eventTimestamp: Long) = throw new UnsupportedOperationException("Not implemented yet")
    /**Add chart value to time line chart
     * 
     * @param label Label of chart series
     * @param value Value to be added to chart series
     */
    def addChartValue(label: String, value: Double) = throw new UnsupportedOperationException("Not implemented yet")

    /**Returns best toBack/toLay prices for market runner.
     * Element 1 - best price to back, element 2 - best price to lay
     * Double.NaN is returned if price is not available.
     * @return 
     * */
    def getBestPrices(runnerId: Long): Tuple2[IRunnerPrice, IRunnerPrice] = throw new UnsupportedOperationException("Not implemented yet")

    /**Returns best toBack/toLay prices for market.
     * 
     * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
     */
    def getBestPrices(): Map[Long, Tuple2[IRunnerPrice, IRunnerPrice]] = throw new UnsupportedOperationException("Not implemented yet")

    /** Places a bet on a betting exchange market.
     * 
     * @param betSize
     * @param betPrice
     * @param betType
     * @param runnerId
     * 
     * @return The bet that was placed.
     */
    def placeBet(betSize: Double, betPrice: Double, betType: BetTypeEnum, runnerId: Long): IBet = marketService.placeBet(betSize, betPrice, betType, marketId, runnerId)

    /** Places a bet on a betting exchange market.
     * 
     * @param betSizeLimit Total user unmatched volume that should be achieved after calling this method. 
     * For example is unmatched volume is 2 and betSizeLimit is 5 then bet with bet size 3 is placed. 
     * @param betPrice
     * @param betType
     * @param runnerId
     * 
     * @return The bet that was placed or None if nothing has been placed.
     */
    def fillBet(betSizeLimit: Double, betPrice: Double, betType: BetTypeEnum, runnerId: Long): Option[IBet] = throw new UnsupportedOperationException("Not implemented yet")

    /** Cancels a bet on a betting exchange market.
     * 
     * @param betId Unique id of a bet to be cancelled.
     * 
     * @return amount cancelled
     */
    def cancelBet(betId: Long): Double = throw new UnsupportedOperationException("Not implemented yet")

    /**Place hedge bet on a market runner to make ifwin/iflose profits even. Either back or lay bet is placed on best available price.
     * 
     * @param runnerId
     * 
     * @return Hedge bet that was placed or none if no hedge bet was placed.
     */
    def placeHedgeBet(runnerId: Long): Option[IBet] = throw new UnsupportedOperationException("Not implemented yet")

    /**Returns all bets placed by user on that market.
     *
     *@param matchedBetsOnly If true then matched bets are returned only, 
     * otherwise all unmatched and matched bets for user are returned.
     */
    def getBets(matchedBetsOnly: Boolean): List[IBet] = throw new UnsupportedOperationException("Not implemented yet")

    /** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
     *  Prices with zero volume are not returned by this method.
     * 
     * @param runnerId Unique runner id that runner prices are returned for.
     * @return
     */
    def getRunnerPrices(runnerId: Long): List[IRunnerPrice] = throw new UnsupportedOperationException("Not implemented yet")

    /**Returns total traded volume for all prices on all runners in a market.*/
    def getRunnerTradedVolume(runnerId: Long): IRunnerTradedVolume = throw new UnsupportedOperationException("Not implemented yet")

    /**Returns total traded volume for a given runner.*/
    def getTotalTradedVolume(runnerId: Long): Double = throw new UnsupportedOperationException("Not implemented yet")

    def risk(): MarketExpectedProfit = throw new UnsupportedOperationException("Not implemented yet")

    /**see Kelly Criterion - http://en.wikipedia.org/wiki/Kelly_criterion.*/
    def wealth(bank: Double): MarketExpectedProfit = throw new UnsupportedOperationException("Not implemented yet")

    /**Registers new trader and return trader context. 
     * This context can be used to trigger some custom traders that are registered manually by a master trader, 
     * e.g. when testing some evolution algorithms for which more than one trader is required.
     * @return trader context
     */
    def registerTrader(): ITraderContext = throw new UnsupportedOperationException("Not implemented yet")

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
    def registerEPN(getEventTypes: => (Map[String, Map[String, Object]]), getEPLStatements: => Map[String, String], publish: (EPServiceProvider) => Unit): Boolean = throw new UnsupportedOperationException("Not implemented yet")

    /**Returns registered EPL statement for a given eplID. 
     * It could be used to iterate through the current state of EPL statement, e.g. get some delta or avg value from EPN.
     * 
     * @param eplID
     * @return EPStatement
     * */
    def getEPNStatement(eplID: String): EPStatement = throw new UnsupportedOperationException("Not implemented yet")

  }