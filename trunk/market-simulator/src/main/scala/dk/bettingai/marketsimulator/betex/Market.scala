package dk.bettingai.marketsimulator.betex

import java.util.Date


/**It's a domain model that represents a market on a betting exchange.
 * @author korzekwad
 *
 */
object Market {
	class Selection(val selectionId:Long, val selectionName:String) {
		override def toString = "Selection [selectionId=%s, selectionName=%s]".format(selectionId, selectionName)
	}
}

class Market(val marketId:Long, val marketName:String,val eventName:String,val numOfWinners:Int, val marketTime:Date,val selections:List[Market.Selection]) {
	require(numOfWinners>0,"numOfWinners should be bigger than 0, numOfWinners=" + numOfWinners)
	require(selections.size>1,"Number of market selections should be bigger than 1, numOfSelections=" + selections.size)
	
	override def toString = "Market [marketId=%s, marketName=%s, eventName=%s, numOfWinners=%s, marketTime=%s, selections=%s]".format(marketId,marketName,eventName,numOfWinners,marketTime,selections)
}