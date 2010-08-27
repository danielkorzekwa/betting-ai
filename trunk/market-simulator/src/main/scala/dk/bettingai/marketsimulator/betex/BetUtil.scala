package dk.bettingai.marketsimulator.betex

import dk.bettingai.marketsimulator.betex.api.IBet

/**Provides some bet utilities.
 * 
 * @author korzekwad
 *
 */
object BetUtil {

	/**Calculate avg weighted price.
	 * 
	 * @param bets
	 * @return
	 */
	def avgPrice(bets:List[IBet]):Double = bets.foldLeft(0d)((sum,bet)=> sum + bet.betPrice*bet.betSize) /bets.foldLeft(0d)(_ + _.betSize)
}