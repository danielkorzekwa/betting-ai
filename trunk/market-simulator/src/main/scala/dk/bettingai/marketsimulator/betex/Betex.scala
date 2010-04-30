package dk.bettingai.marketsimulator.betex


/** This interface represents a betting exchange. It allows to create market, place bet, cancel bet, etc.
 * @author korzekwad
 *
 */
trait Betex {

	/**Creates market on a betting exchange.
	 * 
	 */
	def createMarket(market:Market)
}