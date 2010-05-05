package dk.bettingai.marketsimulator.marketevent

import dk.bettingai.marketsimulator.betex._
import java.util.Date
import  dk.bettingai.marketsimulator.betex.Market._
import  dk.bettingai.marketsimulator.betex.Bet.BetTypeEnum._
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

		val eventMap = JSON.parseFull(marketEvent).getOrElse(Map()).asInstanceOf[Map[String,Any]];

		if(!eventMap.contains("eventType")) {
			throw new IllegalArgumentException("No 'eventType' attribute for event: " + marketEvent)
		}
		else if(eventMap("eventType") == "CREATE_MARKET") {
			val selections = eventMap("selections").asInstanceOf[List[Map[String,String]]].map(s => new Market.Selection(s("selectionId").asInstanceOf[Double].toLong,s("selectionName")))
			val market = new Market(eventMap("marketId").asInstanceOf[Double].toLong,eventMap("marketName").asInstanceOf[String],eventMap("eventName").asInstanceOf[String],eventMap("numOfWinners").asInstanceOf[Double].toInt,df.parse(eventMap("marketTime").asInstanceOf[String]),selections)
			betex.createMarket(market)
		}
		else if(eventMap("eventType") == "PLACE_BET") {
			val bet = new Bet(eventMap("betSize").asInstanceOf[Double],eventMap("betPrice").asInstanceOf[Double], Bet.BetTypeEnum.valueOf(eventMap("betType").asInstanceOf[String]).get, eventMap("marketId").asInstanceOf[Double].toLong,eventMap("selectionId").asInstanceOf[Double].toLong)
			betex.placeBet(eventMap("userId").asInstanceOf[Double].toInt, bet)
		}
		else {
			throw new IllegalArgumentException("Event type is not supported: " + eventMap("eventType"))
		}
	}


}