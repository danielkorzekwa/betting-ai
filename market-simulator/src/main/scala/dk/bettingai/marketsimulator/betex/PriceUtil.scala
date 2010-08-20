package dk.bettingai.marketsimulator.betex

import PriceUtil._

/**Exchange price utils.
 * 
 */
object PriceUtil {

	object PriceRoundEnum extends Enumeration{
		type PriceRoundEnum = Value
		val ROUND_UP = Value("ROUND_UP")
		val ROUND_DOWN = Value("ROUND_DOWN")
	}


case class PriceRange(val start:Double, val stop:Double,val step:Double)

/**Round input price to the nearest valid betfair price.
 * 
 * @param priceRanges List of valid prices
 * @param round Which side (up or down) rounding should be done.
 * 
 * */
def validate(priceRanges: List[PriceRange])(price:Double,round:PriceRoundEnum.PriceRoundEnum): Double = {
	require(!priceRanges.isEmpty,"Price range list is empty.")

	val minPrice = priceRanges.head.start
	val maxPrice = priceRanges.last.stop
	
	val validatedPrice = if(price<minPrice) minPrice
	else if(price > maxPrice) maxPrice
	else {
		val priceRange = priceRanges.find(r => price >= r.start && price <= r.stop) 

		val reminded = ((price*100) % (priceRange.get.step*100))/100
		if(reminded==0) 
			price
			else round match {
			case PriceRoundEnum.ROUND_UP => price - (price % priceRange.get.step) + priceRange.get.step
			case PriceRoundEnum.ROUND_DOWN =>price - (price % priceRange.get.step)
			}
	}
	
	(validatedPrice*100)/100
}

/**Returns list of betfair price ranges.*/
def getPricRanges:List[PriceRange] = {
			val priceRanges = PriceRange(1.01, 2.0, 0.01) ::
				PriceRange(2.0, 3.0, 0.02) ::
					PriceRange(3.0, 4.0, 0.05) ::
						PriceRange(4.0, 6.0, 0.1) ::
							PriceRange(6.0, 10.0, 0.2) ::
								PriceRange(10.0, 20.0, 0.5) ::
									PriceRange(20.0, 30.0, 1.0) ::
										PriceRange(30.0, 50.0, 2.0) ::
											PriceRange(50.0, 100.0, 5.0) ::
												PriceRange(100.0, 1000.0, 10.0) :: Nil
												priceRanges
	}
}