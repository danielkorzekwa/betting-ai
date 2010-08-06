package dk.bettingai.marketsimulator.marketevent

import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.api._
import java.util.Date
import  dk.bettingai.marketsimulator.betex.Market._
import scala.util.parsing.json._
import java.text._
import org.codehaus.jackson._
import org.codehaus.jackson.map._
import org.codehaus.jackson.map.`type`
import scala.collection.JavaConversions._
/**Processes market event in a json format and calls appropriate method on a betting exchange.
 * @author korzekwad
 *
 */
class MarketEventProcessorImpl(betex:IBetex) extends MarketEventProcessor{

	private val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	val f = new JsonFactory();

	val objectMapper = new ObjectMapper()
	/**Processes market event in a json format and calls appropriate method on a betting exchange.
	 * 
	 * @param marketEvent
	 * @param nextBetId Returns betId for PLACE_BET event
	 * @param userId It is used for bet placement events
	 */
	def process(marketEvent:String, nextBetId: => Long,userId:Int) {	

		val jp = f.createJsonParser(marketEvent);
		jp.nextToken
		jp.nextToken 
		jp.nextToken //eventType
		require(jp.getCurrentName()=="eventType","No 'eventType' attribute for event: " + marketEvent)

		if(jp.getText=="CREATE_MARKET") {
			val eventHashMap = objectMapper.readValue(marketEvent,classOf[java.util.HashMap[Any,Any]])
			val runnersMap = eventHashMap("runners").asInstanceOf[java.util.List[java.util.Map[String,Any]]]
			val runners = runnersMap.map(s => new Market.Runner(s("runnerId").asInstanceOf[Int],s("runnerName").asInstanceOf[String])).toList
			
			val marketId = eventHashMap("marketId").asInstanceOf[Int].toLong
			val marketName = eventHashMap("marketName").asInstanceOf[String]
			val eventName = eventHashMap("eventName").asInstanceOf[String]
			val numOfWinners = eventHashMap("numOfWinners").asInstanceOf[Int]
			val marketTime = df.parse(eventHashMap("marketTime").asInstanceOf[String])
			betex.createMarket(marketId,marketName,eventName,numOfWinners,marketTime,runners)
		}
		else {
			val eventMap:scala.collection.mutable.Map[String,String] = scala.collection.mutable.Map()
			eventMap += jp.getCurrentName() -> jp.getText
			while (jp.nextToken() != JsonToken.END_OBJECT) {
				jp.nextToken 
				eventMap += jp.getCurrentName() -> jp.getText
			}

			eventMap("eventType") match {
			case "PLACE_BET" => {	
				val market = betex.findMarket( eventMap("marketId").toLong)
				market.placeBet(nextBetId,userId,eventMap("betSize").toDouble,eventMap("betPrice").toDouble, IBet.BetTypeEnum.valueOf(eventMap("betType")).get,eventMap("runnerId").toLong)
			}
			case "CANCEL_BETS" => {
				val market = betex.findMarket( eventMap("marketId").toLong)
				market.cancelBets(userId,eventMap("betsSize").toDouble,eventMap("betPrice").toDouble,IBet.BetTypeEnum.valueOf(eventMap("betType")).get,eventMap("runnerId").toLong)
			}
			case _ =>	throw new IllegalArgumentException("Event type is not supported: " + eventMap("eventType"))
			}
			 
		}

	}

}