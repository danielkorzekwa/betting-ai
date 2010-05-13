package dk.bettingai.marketsimulator.betex

import Bet._
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

	object BetStatusEnum extends Enumeration{
		type BetStatusEnum = Value
		val M = Value("M") //matched
		val U = Value("U") //unmatched
	}
}

class Bet(val betId:Long,val userId: Long, val betSize:Double, val betPrice:Double, val betType:BetTypeEnum.BetTypeEnum, val betStatus:BetStatusEnum.BetStatusEnum,val marketId:Long,val selectionId:Long) {
	require(betPrice>=1.01 && betPrice<=1000,"Bet price must be between 1.01 and 1000, betPrice=" + betPrice)

	/**Match two bets. The bet that the matchedBet method is executed on is always matched at the best available price. Examples: backBetWithPrice2.matchBet(layBetWithPrice3) = matched price 3,layBetWithPrice3.matchBet(backBetWithPrice2) = matched price 2. 
	 * 
	 * @param bet The bet to be matched with.
	 * @return Result of bets matching. In the general form it consists of 4 elements. 1 - matched portion of first bet, 2 - unmatched portion of first bet, 3 - matched portion of second bet, 4 - unmatched portion of second bet.
	 *   
	 * */
	def matchBet(bet:Bet):List[Bet] = {

		/**Do not match scenarios.*/
		if(betType==bet.betType || marketId!=bet.marketId || selectionId!=bet.selectionId || betStatus==Bet.BetStatusEnum.M || bet.betStatus==Bet.BetStatusEnum.M || (betType==Bet.BetTypeEnum.BACK && betPrice>bet.betPrice) || (betType==Bet.BetTypeEnum.LAY && betPrice<bet.betPrice)) {
			val firstBetUnmatchedPortion = new Bet(betId,userId,betSize,betPrice,betType,betStatus,marketId,selectionId)
			val secondBetUnmatchedPortion = new Bet(bet.betId,bet.userId,bet.betSize,bet.betPrice,bet.betType,bet.betStatus,bet.marketId,bet.selectionId)

			List(firstBetUnmatchedPortion,secondBetUnmatchedPortion)
		}
		else {
			/**Match on the best available price.*/
			val matchedPrice = bet.betPrice
			val matchedSize = betSize.min(bet.betSize)

			val firstBetMatchedPortion = new Bet(betId,userId,matchedSize,matchedPrice,betType,BetStatusEnum.M,marketId,selectionId)
			val firstBetUnmatchedSize = betSize - matchedSize
			val firstBetUnmatchedPortion = new Bet(betId,userId,firstBetUnmatchedSize,betPrice,betType,BetStatusEnum.U,marketId,selectionId)

			val secondBetMatchedPortion = new Bet(bet.betId,bet.userId,matchedSize,matchedPrice,bet.betType,BetStatusEnum.M,bet.marketId,bet.selectionId)
			val secondBetUnmatchedSize = bet.betSize - matchedSize
			val secondBetUnmatchedPortion = new Bet(bet.betId,bet.userId,secondBetUnmatchedSize,bet.betPrice,bet.betType,BetStatusEnum.U,bet.marketId,bet.selectionId)

			List(firstBetMatchedPortion,firstBetUnmatchedPortion,secondBetMatchedPortion,secondBetUnmatchedPortion).filter(b => b.betSize>0)
		}

	} 

	override def toString = "Bet [betId=%s, userId=%s, betSize=%s, betPrice=%s, betType=%s, betStatus=%s, marketId=%s, selectionId=%s]".format(betId,userId,betSize,betPrice,betType,betStatus,marketId,selectionId)
}

