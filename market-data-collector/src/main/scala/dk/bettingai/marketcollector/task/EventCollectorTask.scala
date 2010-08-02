package dk.bettingai.marketcollector.task

import org.joda.time._
import dk.bettingai.marketcollector.marketservice._
import IMarketService._
import dk.bettingai.marketsimulator.betex._
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

/**This trait represents a task that writes markets events to files for given set of markets (one file per market). 
 * Those market events represent delta between two market states, where a market state is defined by runner prices and traded volume. 
 * Calling this task with a given interval allows collecting market data that can be used to replay market on a betting exchange.
 * 
 * @author korzekwad
 *
 * @param marketService This service returns list of markets, and market data (runner prices, traded volume) that is used to calculate market events.
 * @param startInMinutesFrom Market time that markets are monitored from, e.g. -60 means now-60 minutes.
 * @param startInMinutesTo Market time that markets are monitored to, e.g. 60 means now+60 minutes.
 * @param marketDataDir The directory that market events are written to.
 * @param marketDiscoveryIntervalSec How often new markets are discovered, e.g. if it's set to 60, then new markets will be discovered every 60 seconds, even though this task is executed every 1 second.
 */
class EventCollectorTask(marketService:MarketService, startInMinutesFrom:Int,startInMinutesTo:Int, marketDataDir:String,marketDiscoveryIntervalSec:Int) extends IEventCollectorTask{

	private val log = LoggerFactory.getLogger(getClass)
	
	/**Calculate markets events for delta between two market states.*/
	private val eventProducer = new EventProducer()

	private val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**Map of FileWriters for markets. Key - marketId.*/
	private val marketFiles:scala.collection.mutable.Map[Long,FileWriter] = scala.collection.mutable.Map()

	/**How often new markets are discovered, e.g. if it's set to 60, then new markets will be discovered every 60 seconds, even though this task is executed every 1 second.*/
	private var discoveryTime:Long=0
	
	/**List of discovered markets that market data is collected for.*/
	private var marketIds:List[Long]=Nil

	/** Executes EventCollectorTask. For more details, read class level comments.*/ 
	def execute() = {
		/**Discover markets that the market events should be collected for.*/
		val now = new DateTime()
		if((now.getMillis-discoveryTime)/1000>marketDiscoveryIntervalSec) {
			marketIds = marketService.getMarkets(now.minusMinutes(startInMinutesFrom).toDate, now.plusMinutes(startInMinutesTo).toDate)		
			discoveryTime = now.getMillis
			log.info("Market discovery: " + marketIds)
		}

		/**For each market, write market events to the file.*/
		for(marketId <- marketIds) {

			try {
				def createFileWriter():FileWriter = {
						val file = new File(marketDataDir + "/" + marketId)
						val fw = new FileWriter(file, false)
						val marketDetails = marketService.getMarketDetails(marketId)
						val createMarketEvent = buildCreateMarketEvent(marketDetails)
							IOUtils.writeLines(createMarketEvent::Nil,null,fw)
							fw
				}
				/**Create market file and add CREATE_MARKET event if market has not been processed yet.*/
				val printWriter = marketFiles.getOrElseUpdate(marketId,createFileWriter)

				/**Get marketRunners from betfair. Key - runnerId, value - runner prices + price traded volume*/
				val marketRunnersMap:Map[Long,Tuple2[List[RunnerPrice],List[PriceTradedVolume]]] =  marketService.getMarketRunners(marketId)

				/**Generate market events and add them to the file.*/
				val events = eventProducer.produce(marketId,marketRunnersMap)
				val eventsCheck = eventProducer.produce(marketId,marketRunnersMap)
				if(!eventsCheck.isEmpty) throw new IllegalStateException("Events size should be 0 here.")
				IOUtils.writeLines(events,null,printWriter)
				printWriter.flush()
			} catch {
			case e:EventProducerVerificationError => printError(e)
			case e: IllegalStateException => log.error(e.getLocalizedMessage)
			}
		}
	}

	private def printError(e:EventProducerVerificationError) {
		log.error("EventProducerVerificationError: " + e.getLocalizedMessage)
		log.error("Prev runner prices = " + e.prevRunnerData._1)
		log.error("New runner prices =  " + e.newRunnerData._1)
		log.error("Ver runner prices =  " + e.toVerifyRunnerData._1)

		log.error("Prev traded volume = " + e.prevRunnerData._2)
		log.error("New traded volume =  " + e.newRunnerData._2)
		log.error("Ver traded volume =  " + e.toVerifyRunnerData._2)
		log.error("Events = " + e.events)
	}
	
	private def buildCreateMarketEvent(marketDetails:MarketDetails):String = {
		import marketDetails._
		val runners = marketDetails.runners.map(r => """{"runnerId":%s,"runnerName":"%s"}""".format(r.runnerId,r.runnerName)).mkString("[",",","]")
		val createMarketEvent = """{"eventType":"CREATE_MARKET","marketId":%s,"marketName":"%s","eventName":"%s","numOfWinners":%s,"marketTime":"%s","runners": %s}""".
		format(marketId,marketName,menuPath,numOfWinners,df.format(marketTime),runners)
		createMarketEvent
	}
}