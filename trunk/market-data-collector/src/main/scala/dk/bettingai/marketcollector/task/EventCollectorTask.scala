package dk.bettingai.marketcollector.task

import org.joda.time._
import dk.bettingai.marketcollector.marketservice._
import dk.bettingai.marketsimulator.betex._
import Market._
import dk.bettingai.marketcollector.eventproducer._
import EventProducer._
import org.apache.commons.io._
import java.io.File
import java.io.FileWriter
import scala.collection.JavaConversions._
import org.slf4j.LoggerFactory

class EventCollectorTask(marketService:MarketService, startInMinutesFrom:Int,startInMinutesTo:Int, marketDataDir:String,marketDiscoveryIntervalSec:Int) extends IEventCollectorTask{

	val log = LoggerFactory.getLogger(getClass)
	val eventProducer = new EventProducer()

	/**Map of FileWriters for markets. Key - marketId.*/
	val marketFiles:scala.collection.mutable.Map[Long,FileWriter] = scala.collection.mutable.Map()

	var discoveryTime:Long=0
	var marketIds:List[Long]=Nil

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
						val createMarketEvent = """{"eventType":"CREATE_MARKET","marketId":10,"marketName":"Match Odds","eventName":"Man Utd vs Arsenal","numOfWinners":1,"marketTime":"2010-04-15 14:00:00","runners": [{"runnerId":11,"runnerName":"Man Utd"},{"runnerId":12,"runnerName":"Arsenal"}]}"""
							IOUtils.writeLines(createMarketEvent::Nil,null,fw)
							fw
				}
				/**Create market file and add CREATE_MARKET event if market has not been processed yet.*/
				val printWriter = marketFiles.getOrElseUpdate(marketId,createFileWriter)

				/**Get marketRunners from betfair. Key - selectionId, value - runner prices + price traded volume*/
				val marketRunnersMap:Map[Long,Tuple2[List[RunnerPrice],List[PriceTradedVolume]]] =  marketService.getMarketRunners(marketId)

				/**Generate market events and add them to the file.*/
				val events = eventProducer.produce(marketId,marketRunnersMap)
				val eventsCheck = eventProducer.produce(marketId,marketRunnersMap)
				if(eventsCheck.size>0) throw new IllegalStateException("Events size should be 0 here.")
				IOUtils.writeLines(events,null,printWriter)
				printWriter.flush()
			} catch {
			case e:EventProducerVerificationError => printDebug(e)
			case e: IllegalStateException => log.error(e.getLocalizedMessage)
			}
		}

	}

	private def printDebug(e:EventProducerVerificationError) {
		log.error("EventProducerVerificationError: " + e.getLocalizedMessage)
		log.error("Prev runner prices = " + e.prevRunnerData._1)
		log.error("New runner prices =  " + e.newRunnerData._1)
		log.error("Ver runner prices =  " + e.toVerifyRunnerData._1)

		log.error("Prev traded volume = " + e.prevRunnerData._2)
		log.error("New traded volume =  " + e.newRunnerData._2)
		log.error("Ver traded volume =  " + e.toVerifyRunnerData._2)
		log.error("Events = " + e.events)
	}
}