package dk.bettingai.marketsimulator.marketevent

import dk.bettingai.marketsimulator.betex._
import java.util.Date
import  dk.bettingai.marketsimulator.betex.Market._
import scala.util.parsing.json._
import java.text._

/**Processes market event in a json format and calls appropriate method on a betting exchange.
 * @author korzekwad
 *
 */
class MarketEventProcessorImpl(betex:Betex) extends MarketEventProcessor{

	private val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/**Processes market event in a json format and calls appropriate method on a betting exchange.
	 * 
	 * @param marketEvent
	 */
	def process(marketEvent:String) {

		val eventMap = JSON.parseFull(marketEvent).get.asInstanceOf[Map[String,Any]];

		if(eventMap("eventType")== "CREATE_MARKET") {
			val selections = eventMap("selections").asInstanceOf[List[Map[String,String]]].map(s => new Market.Selection(s("selectionId").asInstanceOf[Double].toInt,s("selectionName")))
			val market = new Market(eventMap("marketId").asInstanceOf[Double].toInt,eventMap("marketName").asInstanceOf[String],eventMap("eventName").asInstanceOf[String],eventMap("numOfWinners").asInstanceOf[Double].toInt,df.parse(eventMap("marketTime").asInstanceOf[String]),selections)
			betex.createMarket(market)
		}

	}


}