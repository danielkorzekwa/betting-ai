package dk.bettingai.marketsimulator.betex

import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex.api.IBet._
import dk.bettingai.marketsimulator.betex.api.IBet.BetTypeEnum._
import dk.bettingai.marketsimulator.betex.api.IBet.BetStatusEnum._
/** This class represents a bet on a betting exchange.
 * 
 * @author korzekwad
 *
 */
class Bet(val betId:Long,val userId: Long, val betSize:Double, val betPrice:Double, val betType:BetTypeEnum, val betStatus:BetStatusEnum,val marketId:Long,val runnerId:Long) extends IBet{
	require(betPrice>=1.01 && betPrice<=1000,"Bet price must be between 1.01 and 1000, betPrice=" + betPrice)

	/**Match two bets. Bet that the matchedBet method is executed on is always matched at the best available price. 
	 * Examples: backBetWithPrice2.matchBet(layBetWithPrice3) = matched price 3,layBetWithPrice3.matchBet(backBetWithPrice2) = matched price 2. 
	 * 
	 * @param bet The bet to be matched with.
	 * @return Result of bets matching. In the general form it consists of 4 elements. 
	 * 1 - matched portion of first bet, 2 - unmatched portion of first bet, 3 - matched portion of second bet, 4 - unmatched portion of second bet.
	 *   
	 * */
	def matchBet(bet:IBet):List[IBet] = {

		/**Do not match scenarios.*/
		if(betType==bet.betType || marketId!=bet.marketId || runnerId!=bet.runnerId || betStatus==IBet.BetStatusEnum.M || bet.betStatus==IBet.BetStatusEnum.M || (betType==IBet.BetTypeEnum.BACK && betPrice>bet.betPrice) || (betType==IBet.BetTypeEnum.LAY && betPrice<bet.betPrice)) {
			val firstBetUnmatchedPortion = new Bet(betId,userId,betSize,betPrice,betType,betStatus,marketId,runnerId)
			val secondBetUnmatchedPortion = new Bet(bet.betId,bet.userId,bet.betSize,bet.betPrice,bet.betType,bet.betStatus,bet.marketId,bet.runnerId)

			List(firstBetUnmatchedPortion,secondBetUnmatchedPortion)
		}
		else {
			/**Match on the best available price.*/
			val matchedPrice = bet.betPrice
			val matchedSize = betSize.min(bet.betSize)

			val firstBetMatchedPortion = new Bet(betId,userId,matchedSize,matchedPrice,betType,BetStatusEnum.M,marketId,runnerId)
			val firstBetUnmatchedSize = betSize - matchedSize
			val firstBetUnmatchedPortion = new Bet(betId,userId,firstBetUnmatchedSize,betPrice,betType,BetStatusEnum.U,marketId,runnerId)

			val secondBetMatchedPortion = new Bet(bet.betId,bet.userId,matchedSize,matchedPrice,bet.betType,BetStatusEnum.M,bet.marketId,bet.runnerId)
			val secondBetUnmatchedSize = bet.betSize - matchedSize
			val secondBetUnmatchedPortion = new Bet(bet.betId,bet.userId,secondBetUnmatchedSize,bet.betPrice,bet.betType,BetStatusEnum.U,bet.marketId,bet.runnerId)

			List(firstBetMatchedPortion,firstBetUnmatchedPortion,secondBetMatchedPortion,secondBetUnmatchedPortion).filter(b => b.betSize>0)
		}

	} 

	override def toString = "Bet [betId=%s, userId=%s, betSize=%s, betPrice=%s, betType=%s, betStatus=%s, marketId=%s, runnerId=%s]".format(betId,userId,betSize,betPrice,betType,betStatus,marketId,runnerId)
}

