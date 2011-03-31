package dk.bettingai.livetrader

import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketcollector.marketservice._
import IMarketService._
import scala.actors.Actor
import Actor._
import com.espertech.esper.client._
import com.espertech.esper.client.time._
import scala.collection._
import scala.collection.JavaConversions._

/**
 * This class allows for running trader on a betting exchange market. Trader observes a market and places some bets.
 *
 * @author korzekwad
 *
 * @param trader Trader, which listens on a betting exchange market.
 * @param marketId Unique identifier of a betting exchange market to listen on.
 * @param interval How often (in milliseconds) trader is triggered with a new data obtained from a betting exchange market.
 * @param marketService Adapter for a betting exchange.
 */

/**LiveTrader actor messages.*/
case object StartTrader
case object StopTrader
case object ExecuteTrader

case class LiveTrader(trader: ITrader, marketId: Long, interval: Long, marketService: IMarketService, commission: Double) {

  /**key - epnID.*/
  private var epnNetwork: Option[EPServiceProvider] = None
  /**Map[eplId,eplStatement].*/
  private val eplStatements: mutable.Map[String, EPStatement] = mutable.Map()
  private var epnPublisher: Option[(EPServiceProvider) => Unit] = None

  private var traderContext: Option[ITraderContext] = None

  private val liveTraderActor = actor {

    var marketDetails: Option[MarketDetails] = None

    loop {
      react {
        case StartTrader => {
          marketDetails = Some(marketService.getMarketDetails(marketId))
          traderContext = Option(LiveTraderContext(marketDetails.get, marketService, commission, this))
          traderContext.get.setEventTimestamp(System.currentTimeMillis)
          trader.init(traderContext.get)
          self ! ExecuteTrader
          reply
        }
        case StopTrader => {
          traderContext.get.setEventTimestamp(System.currentTimeMillis)
          trader.after(traderContext.get)
          epnNetwork.foreach(epn => epn.destroy())
          reply
          exit
        }
        case ExecuteTrader => {
          /**Send new time event to esper.*/
          val eventTimestamp = System.currentTimeMillis
          traderContext.get.setEventTimestamp(eventTimestamp)
          epnNetwork.foreach(epn => epn.getEPRuntime().sendEvent(new CurrentTimeEvent(eventTimestamp)))
          epnPublisher.foreach { publish => publish(epnNetwork.get) }
          trader.execute(traderContext.get)

          Thread.sleep(interval)
          self ! ExecuteTrader
        }
      }
    }
  }
  /**Start live trader.*/
  def start() = liveTraderActor !? StartTrader

  /**Stop live trader.*/
  def stop() = liveTraderActor !? StopTrader

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

    if (epnNetwork.isDefined) false
    else {
      val config = new Configuration()
      config.getEngineDefaults().getThreading().setInternalTimerEnabled(false)
      getEventTypes.foreach { case (eventTypeName, eventMap) => config.addEventType(eventTypeName, eventMap) }

      val epServiceProvider = EPServiceProviderManager.getProvider("" + marketId, config)
      epServiceProvider.initialize()

      getEPLStatements.foreach { case (eplID, eplStatement) => eplStatements(eplID) = epServiceProvider.getEPAdministrator().createEPL(eplStatement) }

      epnPublisher = Option(publish)
      epnNetwork = Option(epServiceProvider)
      true
    }

  }

  /**
   * Returns registered EPL statement for a given eplID.
   * It could be used to iterate through the current state of EPL statement, e.g. get some delta or avg value from EPN.
   *
   * @param eplID
   * @return EPStatement
   */
  def getEPNStatement(eplID: String): EPStatement = eplStatements(eplID)
}