package dk.bettingai.marketcollector.task

import org.joda.time._
import dk.bettingai.marketcollector.marketservice._
import IMarketService._
import MarketService._
import dk.betex._
import Market._
import dk.bettingai.marketcollector.eventproducer._
import EventProducer._
import org.apache.commons.io._
import java.io.File
import java.io.FileWriter
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory
import scala.util.parsing.json._
import java.text.SimpleDateFormat

/**
 * This class represents a task that writes markets events to files for given set of markets (one file per market).
 * Those market events represent delta between two market states, where a market state is defined by runner prices and traded volume.
 * Calling this task with a given interval allows collecting market data that can be used to replay market on a betting exchange.
 *
 * @author korzekwad
 *
 */
object EventCollectorTask {

  trait EventListener {
    def onEvents(marketId: Long, events: List[String]): Unit
  }

  /**
   * Writes market events to files
   *
   * @param marketDataDir The directory that market events are written to.
   */
  case class FilesystemEventListener(marketDataDir: String) extends EventListener {

    /**Map of FileWriters for markets. Key - marketId.*/
    private val marketFiles: scala.collection.mutable.Map[Long, FileWriter] = scala.collection.mutable.Map()

    def onEvents(marketId: Long, events: List[String]): Unit = {

      def createFileWriter(): FileWriter = {
        val file = new File(marketDataDir + "/" + marketId + ".csv")
        val fw = new FileWriter(file, false)
        fw
      }

      /**Writes events to a file*/
      val printWriter = marketFiles.getOrElseUpdate(marketId, createFileWriter)
      IOUtils.writeLines(events, null, printWriter)
      printWriter.flush()
    }

  }

}

/**
 * @param marketService This service returns list of markets, and market data (runner prices, traded volume) that is used to calculate market events.
 * @param startInMinutesFrom Market time that markets are monitored from, e.g. -60 means now-60 minutes.
 * @param startInMinutesTo Market time that markets are monitored to, e.g. 60 means now+60 minutes.
 * @param maxNumOfWinners Collect market data for markets with number of winners less or equal to maxNumOfWinners
 * @param marketDiscoveryInterval [ms] How often new markets are discovered, e.g. if it's set to 60000, then new markets will be discovered every 60 seconds, even though this task is executed every 1 second.
 */
class EventCollectorTask(marketService: MarketService, startInMinutesFrom: Int, startInMinutesTo: Int, maxNumOfWinners: Option[Int],
  marketDiscoveryInterval: Int, marketEventListener: EventCollectorTask.EventListener) extends IEventCollectorTask {

  private val log = LoggerFactory.getLogger(getClass)

  /**Calculate markets events for delta between two market states.*/
  private val eventProducer = new EventProducer()

  private val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  
  /**Set of collected markets.*/
  private val collectedMarket = scala.collection.mutable.Set[Long]()

  /**How often new markets are discovered, e.g. if it's set to 60, then new markets will be discovered every 60 seconds, even though this task is executed every 1 second.*/
  private var discoveryTime: Long = 0

  /**List of discovered markets that market data is currently collected for.*/
  private var marketIds: List[Long] = Nil

  /** Executes EventCollectorTask. For more details, read class level comments.*/
  def execute() = {
    /**Discover markets that the market events should be collected for.*/
    val now = new DateTime()
    if ((now.getMillis - discoveryTime) > marketDiscoveryInterval) {
      marketIds = marketService.getMarkets(now.plusMinutes(startInMinutesFrom).toDate, now.plusMinutes(startInMinutesTo).toDate, None, maxNumOfWinners)
      discoveryTime = now.getMillis
      log.info("Market discovery: " + marketIds)
    }

    /**For each market, write market events to the file.*/
    for (marketId <- marketIds) {

      try {

        /**Add CREATE_MARKET event if market has not been processed yet.*/
        if (!collectedMarket.contains(marketId)) {
          val marketDetails = marketService.getMarketDetails(marketId)
          val createMarketEvent = buildCreateMarketEvent(now.getMillis, marketDetails)
          marketEventListener.onEvents(marketId, createMarketEvent :: Nil)
          collectedMarket += marketId
        }

        /**Get marketRunners from betfair. Key - runnerId, value - runner prices + price traded volume*/
        val marketRunners = marketService.getMarketRunners(marketId)

        /**Collect data for not inplay markets only.*/
        if (marketRunners.inPlayDelay == 0) {
          /**Generate market events and add them to the file.*/
          val events = eventProducer.produce(now.getMillis, marketId, marketRunners.runnerPrices)
          val eventsCheck = eventProducer.produce(now.getMillis, marketId, marketRunners.runnerPrices)
          if (!eventsCheck.isEmpty) throw new IllegalStateException("Events size should be 0 here.")
          marketEventListener.onEvents(marketId, events)
        }

      } catch {
        case e: MarketClosedOrSuspendedException => //do nothing
        case e: EventProducerVerificationError => printError(e)
        case e: IllegalArgumentException => log.error(e.getLocalizedMessage)
      }
    }
  }

  private def printError(e: EventProducerVerificationError) {
    log.error("EventProducerVerificationError: " + e.getLocalizedMessage)
    log.error("Prev runner prices = " + e.prevRunnerData._1)
    log.error("New runner prices =  " + e.newRunnerData._1)
    log.error("Ver runner prices =  " + e.toVerifyRunnerData._1)

    log.error("Prev traded volume = " + e.prevRunnerData._2)
    log.error("New traded volume =  " + e.newRunnerData._2)
    log.error("Ver traded volume =  " + e.toVerifyRunnerData._2)
    log.error("Events = " + e.events)
  }

  private def buildCreateMarketEvent(timestamp: Long, marketDetails: MarketDetails): String = {
    import marketDetails._
    val runners = marketDetails.runners.map(r => """{"runnerId":%s,"runnerName":"%s"}""".format(r.runnerId, r.runnerName)).mkString("[", ",", "]")
    val createMarketEvent = """{"time":%s,"eventType":"CREATE_MARKET","marketId":%s,"marketName":"%s","eventName":"%s","numOfWinners":%s,"marketTime":"%s","runners": %s}""".
      format(timestamp, marketId, marketName, menuPath.replaceAll("\\\\", "/"), numOfWinners, df.format(marketTime), runners)
    createMarketEvent
  }
}