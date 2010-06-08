package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._

/** Trader that does nothing.
 * 
 * @author korzekwad
 *
 */
class NopTrader extends ITrader{

/**Executes trader implementation so it can analyse markets on a betting exchange and take appropriate bet placement decisions.
	 * 
	 * @param betex
	 * @param userId
	 * @param nextAvailableBetId
	 */
	def execute(betex:IBetex,userId:Long,nextAvailableBetId:Long) = {/**Do nothing.*/}
}