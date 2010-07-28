package dk.bettingai.marketcollector.eventproducer

import org.junit._
import Assert._
import java.text._
import dk.bot.betfairservice._
import java.util.Date
import scala.collection.JavaConversions._
import dk.bot.betfairservice.model._
import org.joda.time._
import dk.bettingai.marketcollector.marketservice._
import dk.bettingai.marketsimulator.betex._
import Market._
import dk.bettingai.marketsimulator.marketevent._
import org.apache.commons.math.util.MathUtils._
import org.slf4j.LoggerFactory
import dk.bettingai.marketcollector._
import dk.bettingai.marketcollector.eventcalculator._
import EventProducer._

class EventProducerTest {

	val log = LoggerFactory.getLogger(getClass)

	val betfairServiceFactoryBean = new dk.bot.betfairservice.DefaultBetFairServiceFactoryBean();
	var betfairService:BetFairService = null
	var marketService:MarketService = null
	var eventProducer:EventProducer = null

	@Before
	def setUp {
		betfairServiceFactoryBean.setUser(System.getenv("bfUser"))
		betfairServiceFactoryBean.setPassword(System.getenv("bfPassword"))
		betfairServiceFactoryBean.setProductId(82)
		val loginResponse = betfairServiceFactoryBean.login
		betfairService = (betfairServiceFactoryBean.getObject.asInstanceOf[BetFairService])
		marketService = new MarketService(betfairService)
		eventProducer = new EventProducer()
	}

	@Test
	def testCreateMarketEventIsReturned {
		val now = new DateTime()
		val marketIds = new MarketService(betfairService).getMarkets(now.minusMinutes(120).toDate, now.plusMinutes(660).toDate)		
		val marketId = marketIds(1)

		for(i <- 0 to 3) {
			Thread.sleep(2000)
			/**key - selectionId, value - runner prices + price traded volume*/
			val marketRunnersMap:Map[Long,Tuple2[List[RunnerPrice],List[PriceTradedVolume]]] =  marketService.getMarketRunners(marketId)
			try {
			val events = eventProducer.produce(marketId,marketRunnersMap)
			println("events:" + events.size)
			val events2 = eventProducer.produce(marketId,marketRunnersMap)
			assertEquals(0,events2.size)
			} catch {
				case e:EventProducerVerificationError => printDebug(e)
			}
		}
	}
	
	private def printDebug(e:EventProducerVerificationError) {
		println("EventProducerVerificationError: " + e.getLocalizedMessage)
		println("Prev runner prices = " + e.prevRunnerData._1)
		println("New runner prices =  " + e.newRunnerData._1)
		println("Ver runner prices =  " + e.toVerifyRunnerData._1)
		
		println("Prev traded volume = " + e.prevRunnerData._2)
		println("New traded volume =  " + e.newRunnerData._2)
		println("Ver traded volume =  " + e.toVerifyRunnerData._2)
	}
}