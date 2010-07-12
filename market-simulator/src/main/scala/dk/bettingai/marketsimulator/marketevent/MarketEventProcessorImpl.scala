package dk.bettingai.marketsimulator.marketevent

import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.api._
import java.util.Date
import  dk.bettingai.marketsimulator.betex.Market._
import scala.util.parsing.json._
import java.text._

/**Processes market event in a json format and calls appropriate method on a betting exchange.
 * @author korzekwad
 *
 */
class MarketEventProcessorImpl(betex:IBetex) extends MarketEventProcessor{

	private val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**Processes market event in a json format and calls appropriate method on a betting exchange.
	 * 
	 * @param marketEvent
	 * @param nextBetId Returns betId for PLACE_BET event
	 * @param userId It is used for bet placement events
	 */
	def process(marketEvent:String, nextBetId: => Long,userId:Int) {		
		val eventMap = JSON.parseFull(marketEvent).getOrElse(Map()).asInstanceOf[Map[String,Any]];
		require(eventMap.contains("eventType"),"No 'eventType' attribute for event: " + marketEvent)

		eventMap("eventType") match {
		case "CREATE_MARKET" => {
			val runners = eventMap("runners").asInstanceOf[List[Map[String,String]]].map(s => new Market.Runner(s("runnerId").asInstanceOf[Double].toLong,s("runnerName")))
			betex.createMarket(eventMap("marketId").asInstanceOf[Double].toLong,eventMap("marketName").asInstanceOf[String],eventMap("eventName").asInstanceOf[String],eventMap("numOfWinners").asInstanceOf[Double].toInt,df.parse(eventMap("marketTime").asInstanceOf[String]),runners)
		}
		case "PLACE_BET" => {	
			val market = betex.findMarket( eventMap("marketId").asInstanceOf[Double].toLong)
			market.placeBet(nextBetId,userId,eventMap("betSize").asInstanceOf[Double],eventMap("betPrice").asInstanceOf[Double], IBet.BetTypeEnum.valueOf(eventMap("betType").asInstanceOf[String]).get,eventMap("runnerId").asInstanceOf[Double].toLong)
		}
		case "CANCEL_BETS" => {
			val market = betex.findMarket( eventMap("marketId").asInstanceOf[Double].toLong)
			market.cancelBets(userId,eventMap("betsSize").asInstanceOf[Double],eventMap("betPrice").asInstanceOf[Double],IBet.BetTypeEnum.valueOf(eventMap("betType").asInstanceOf[String]).get,eventMap("runnerId").asInstanceOf[Double].toLong)
		}
		case _ =>	throw new IllegalArgumentException("Event type is not supported: " + eventMap("eventType"))
		}
	}


}