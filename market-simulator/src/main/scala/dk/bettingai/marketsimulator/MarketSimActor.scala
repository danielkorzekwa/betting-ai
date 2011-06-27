package dk.bettingai.marketsimulator

import scala.actors._
import java.io.File
import ISimulator._
import dk.betex.eventcollector.eventprocessor._
import dk.betex.api._
import java.io.BufferedReader
import java.io.FileReader
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.risk._
import IBet.BetStatusEnum._
import scala.annotation._
import MarketSimActor._
import com.espertech.esper.client._
import scala.collection._
import scala.collection.JavaConversions._
import com.espertech.esper.client.time._

/**This is an actor that performs simulation for a given market, then returns simulation report to the sender and finally dies.
 * 
 * @author korzekwad
 *
 */
object MarketSimActor {
  case class MarketSimRequest(
    marketId: Long,
    marketFile: File,
    registeredTraders: List[RegisteredTrader])
}

/**
 * @param newTraderContext (traderUserId,market) => trader context
 * @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)
 */
class MarketSimActor(marketId: Long, betex: IBetex, nextBetId: () => Long, historicalDataUserId: Int, commission: Double,
  newTraderContext: (Int, IMarket, MarketSimActor) => TraderContext, bank: Double) extends Actor {

  /**key - epnID.*/
  private var epnNetwork:Option[EPServiceProvider] = None
  /**Map[eplId,eplStatement].*/
  private val eplStatements: mutable.Map[String, EPStatement] = mutable.Map()
  private var epnPublisher: Option[(EPServiceProvider) => Unit] = None

  def act {
    react {
      case req: MarketSimRequest => {
        try {
          val marketReport = processMarketFile(req.marketFile, req.registeredTraders)
          sender ! marketReport
        } catch {
          case e: Exception => { sender ! None; throw e }
        }
      }
    }
  }

  /**Process all market events and returns market reports.*/
  private def processMarketFile(marketFile: File, traders: List[RegisteredTrader]): Option[MarketReport] = {

    val marketEventProcessor = new MarketEventProcessorImpl(betex)

    val marketDataReader = new BufferedReader(new FileReader(marketFile))

    /**Process CREATE_MARKET EVENT*/
    val createMarketEvent = marketDataReader.readLine

    val marketReport = if (createMarketEvent != null) {
      val processedEventTimestamp = marketEventProcessor.process(createMarketEvent, nextBetId(), historicalDataUserId)
      val market = betex.findMarket(marketId)

      val traderContexts: List[Tuple2[RegisteredTrader, TraderContext]] = for (trader <- traders) yield trader -> newTraderContext(trader.userId, market,this)
      traderContexts.foreach { case (trader, ctx) => ctx.setEventTimestamp(processedEventTimestamp) }
      traderContexts.foreach { case (trader, ctx) => trader.init(ctx) }

      @tailrec
      def processMarketEvents(marketEvent: String, eventTimestamp: Long): Unit = {

        val processedEventTimestamp = marketEventProcessor.process(marketEvent, nextBetId(), historicalDataUserId)

        /**Triggers traders for all markets on a betting exchange.
         * Warning! traders actually should be called within marketEventProcessor.process, after event time stamp is parsed and before event is added to betting exchange.*/
        if (processedEventTimestamp > eventTimestamp) {
          /**Send new time event to esper.*/
          epnNetwork.foreach(epn => epn.getEPRuntime().sendEvent(new CurrentTimeEvent(eventTimestamp)))

          epnPublisher.foreach { publish => publish(epnNetwork.get) }
          traderContexts.foreach { case (trader, ctx) => trader.execute(ctx) }
          traderContexts.foreach { case (trader, ctx) => ctx.setEventTimestamp(processedEventTimestamp) }
        }

        /**Recursive  call.*/
        val nextMarketEvent = marketDataReader.readLine
        if (nextMarketEvent == null) {
          /**Process remaining events*/
          epnPublisher.foreach {publish => publish(epnNetwork.get) }
          if (processedEventTimestamp <= eventTimestamp) traderContexts.foreach { case (trader, ctx) => trader.execute(ctx) }
        } else processMarketEvents(nextMarketEvent, processedEventTimestamp)
      }

      /**Process remaining market events.*/
      val marketEvent = marketDataReader.readLine
      if (marketEvent != null) processMarketEvents(marketEvent, processedEventTimestamp)
      traderContexts.foreach { case (trader, ctx) => trader.after(ctx) }
      epnNetwork.foreach(epn => epn.destroy())
      /**Create market report.*/
      val report = Option(createMarketReport(marketId, traderContexts))

      /**Remove market from betex after market simulation is finished.*/
      betex.removeMarket(marketId)

      report
    } else None

    marketReport
  }

  /** Calculates market expected profit based on all bets that are placed by traders on all betting exchange markets.
   * 
   * @param marketId
   * @param traderContext
   * @return Market report with market expected profit for all traders and with a few others statistics.
   */
  private def createMarketReport(marketId: Long, traderContexts: List[Tuple2[RegisteredTrader, TraderContext]]): MarketReport = {
    val market = betex.findMarket(marketId)

    val traderReports = for {
      (trader, traderCtx) <- traderContexts

      val marketPrices = market.getBestPrices().mapValues(prices => prices._1.price -> prices._2.price)
      val marketProbs = ProbabilityCalculator.calculate(marketPrices, market.numOfWinners)
      val matchedBets = market.getBets(traderCtx.userId).filter(_.betStatus == M)
      val unmatchedBets = market.getBets(traderCtx.userId).filter(_.betStatus == U)
      val marketExpectedProfit = ExpectedProfitCalculator.calculate(matchedBets, marketProbs, commission,bank)

    } yield TraderReport(trader, marketExpectedProfit, matchedBets.size, unmatchedBets.size, traderCtx.getChartLabels, traderCtx.getChartValues)

    MarketReport(market.marketId, market.marketName, market.eventName, market.marketTime, traderReports)
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
    def registerEPN(getEventTypes: => (Map[String,Map[String,Object]]), getEPLStatements: => Map[String,String],publish: (EPServiceProvider) => Unit):Boolean = {

    if (epnNetwork.isDefined) false
    else {
      val config = new Configuration()
      config.getEngineDefaults().getThreading().setInternalTimerEnabled(false)
      getEventTypes.foreach { case (eventTypeName, eventMap) => config.addEventType(eventTypeName, eventMap) }

      val epServiceProvider = EPServiceProviderManager.getProvider("" + this.hashCode + marketId, config)
      epServiceProvider.initialize()

      getEPLStatements.foreach { case (eplID, eplStatement) => eplStatements(eplID) = epServiceProvider.getEPAdministrator().createEPL(eplStatement) }

      epnPublisher = Option(publish)
      epnNetwork = Option(epServiceProvider)
      true
    }

  }

  /**Returns registered EPL statement for a given eplID. 
     * It could be used to iterate through the current state of EPL statement, e.g. get some delta or avg value from EPN.
     * 
     * @param eplID
     * @return EPStatement
     * */
    def getEPNStatement(eplID:String):EPStatement = eplStatements(eplID)
}