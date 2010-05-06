package dk.bettingai.marketsimulator.betex

/** It is a domain model that represents a bet on a betting exchange.
 * 
 * @author korzekwad
 *
 */

object Bet {

	object BetTypeEnum extends Enumeration{
		type BetTypeEnum = Value
		val BACK = Value("BACK")
		val LAY = Value("LAY")
	}
}

class Bet(val betSize:Double, val betPrice:Double, val betType:Bet.BetTypeEnum.BetTypeEnum, val marketId:Long,val selectionId:Long) {
  require(betSize>=2, "Bet size must be >=2, betSize=" + 2)
	require(betPrice>=1.01 && betPrice<=1000,"Bet price must be between 1.01 and 1000, betPrice=" + betPrice)
	
	override def toString = "Bet [betSize=%s, betPrice=%s, betType=%s, marketId=%s, selectionId=%s]".format(betSize,betPrice,betType,marketId,selectionId)
}

