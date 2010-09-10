package dk.bettingai.marketsimulator.betex

import api._
import IRunnerTradedVolume._

/**This class represents total traded volume for all prices for a given runner in a market.
 * 
 * @author korzekwad
 *
 */
object  RunnerTradedVolume {
	
	class PriceTradedVolume(val price:Double, val totalMatchedAmount:Double) extends IPriceTradedVolume {
		override def toString = "PriceTradedVolume [price=%s, totalMatchedAmount=%s]".format(price,totalMatchedAmount)
	}
}
class RunnerTradedVolume(val pricesTradedVolume:List[IPriceTradedVolume]) extends IRunnerTradedVolume {

	/**Returns delta between this and that runner traded volume objects.*/
	def -(that:IRunnerTradedVolume): IRunnerTradedVolume = throw new UnsupportedOperationException("Not implemented yet")

	/**Returns total traded volume for all prices.*/
	def totalTradedVolume:Double = throw new UnsupportedOperationException("Not implemented yet")

	/**Returns volume weighed average price for traded volume on all prices.*/
	def avgPrice:Double = throw new UnsupportedOperationException("Not implemented yet")

	override def toString = "RunnerTradedVolume [pricesTradedVolume=%s]".format(pricesTradedVolume)
}
