package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._

/** Place back bet if priceToBack>2, place lay bet if priceToLay<2.
 * 
 * @author korzekwad
 *
 */
class SimpleTrader extends ITrader{

	/**Executes trader implementation so it can analyse markets on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param betex
	 * @param userId
	 * @param nextAvailableBetId
	 */
	def execute(betex:IBetex,userId:Long,nextAvailableBetId:Long) = {

		var betId = nextAvailableBetId

		def placeBet(market:IMarket) = {
			for(runner <- market.runners) {
				val bestPrices = market.getBestPrices(runner.runnerId)

				if(bestPrices._1>2) {
					market.placeBet(betId,userId,2,bestPrices._1,BACK,runner.runnerId)
					betId = betId+1
				}
				if(bestPrices._2<2) {
					market.placeBet(betId,userId,2,bestPrices._2,LAY,runner.runnerId)
					betId = betId+1
				}
			}	
		}

		val markets = betex.getMarkets.foreach(placeBet)
	}
}