package dk.bettingai.marketsimulator.betex

import java.util.Date


/**It's a domain model that represents a market on a betting exchange.
 * @author korzekwad
 *
 */
object Market {
	class Selection(val selectionId:Long, val selectionName:String)
}

class Market(val marketId:Long, val marketName:String,val eventName:String,val numOfWinners:Int, val marketTime:Date,val selections:List[Market.Selection])