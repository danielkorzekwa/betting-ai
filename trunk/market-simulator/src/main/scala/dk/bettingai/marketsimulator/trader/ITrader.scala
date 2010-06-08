package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._

/**This trait represents a trader that can place bets on a betting exchange.
 * 
 * @author korzekwad
 *
 */
trait ITrader {

	/**Executes trader implementation so it can analyse markets on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param betex
	 * @param userId
	 * @param nextAvailableBetId
	 */
	def execute(betex:IBetex,userId:Long,nextAvailableBetId:Long)
}