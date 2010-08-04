package dk.bettingai.marketcollector

import org.slf4j.LoggerFactory
import dk.bot.betfairservice._
import dk.bettingai.marketcollector.marketservice._
import dk.bettingai.marketcollector.task._

/**Starts the MarketSimulator application.*/
object MarketCollectorApp {

	private val log = LoggerFactory.getLogger(getClass)

	def main(args:Array[String]) {

		printHeader()

		/**Parse input data.*/
		val inputData:Map[String,String] = try {
			UserInputParser.parse(args)
		}
		catch {
		case e:Exception => printHelpMessage();println("\n" + e);return
		}

		println("Market data collection is started.\n")

		/**Create betfair service and login to betfair account.*/
		val betfairServiceFactoryBean = new dk.bot.betfairservice.DefaultBetFairServiceFactoryBean();
		betfairServiceFactoryBean.setUser(inputData("bfUser"))
		betfairServiceFactoryBean.setPassword(inputData("bfPassword"))
		betfairServiceFactoryBean.setProductId(inputData("bfProductId").toInt)
		val loginResponse = betfairServiceFactoryBean.login
		val betfairService:BetFairService = (betfairServiceFactoryBean.getObject.asInstanceOf[BetFairService])

		/**Create event collector task.*/
		val marketService = new MarketService(betfairService)
		val eventCollectorTask = new EventCollectorTask(marketService,inputData("startInMinutesFrom").toInt,inputData("startInMinutesTo").toInt,inputData("marketDataDir"),inputData("discoveryInterval").toInt)

		val collectionInterval = inputData("collectionInterval").toLong		
		while(true) {
			log.info("EventCollectorTask is running.")
				eventCollectorTask.execute()
			Thread.sleep(collectionInterval*1000)
		}

	}

	private def printHeader() {
		println("")
		println("***********************************************************************************")
		println("*Market Data Collector Copyright 2010 Daniel Korzekwa(http://danmachine.com)      *")      
		println("*Project homepage: http://code.google.com/p/betting-ai/                           *")
		println("*Licenced under Apache License 2.0(http://www.apache.org/licenses/LICENSE-2.0)    *")
		println("***********************************************************************************")
		println("")
	}

	private def printHelpMessage() {
		println("Usage:")
		println("marketdata_collector marketDataDir=? bfUser=? bfPassword=? bfProductId=? collectionInterval=? discoveryInterval=? startInMinutesFrom=? startInMinutesTo=?\n")
		println("marketDataDir - The directory that market events are written to")
		println("bfUser/bfPassword/bfProductId - Betfair account that is used to collect market data using Betfair public API.")
		println("collectionInterval - Market data collection interval.")
		println("discoveryInterval - New markets discovery interval.")
		println("startInMinutesFrom/startInMinutesTo - Market data is collected for markets with market time between startInMinutesFrom and startInMinutesTo.")	
	}
}