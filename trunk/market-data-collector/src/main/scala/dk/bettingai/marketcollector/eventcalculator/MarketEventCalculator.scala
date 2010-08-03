package dk.bettingai.marketcollector.eventcalculator

import dk.bettingai.marketsimulator.betex.api.IMarket._
import dk.bettingai.marketsimulator.betex.Market._
import dk.bettingai.marketsimulator.betex.api.IBet.BetTypeEnum._
/**This trait represents a function that calculates market events for the delta between the previous and the current state of the market runner.
 * 
 * @author KorzekwaD
 *
 */
object MarketEventCalculator  extends IMarketEventCalculator{

	/**Transforms delta between two states of market runner into the stream of events.
	 * 	
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for a market
	 * */
	def produce(marketId:Long,runnerId:Long,marketRunner:Tuple2[List[IRunnerPrice],List[IPriceTradedVolume]],prevMarketRunner:Tuple2[List[IRunnerPrice],List[IPriceTradedVolume]]):List[String] = {

			/**Round down all volumes using Math.floor*/
			val validatedMarketPrices = marketRunner._1.map(r => new RunnerPrice(r.price,r.totalToBack.floor,r.totalToLay.floor))
			val validatedPrevMarketPrices = prevMarketRunner._1.map(r => new RunnerPrice(r.price,r.totalToBack.floor,r.totalToLay.floor))
			val validatedTradedVolumeDelta = marketRunner._2.map(tv => new PriceTradedVolume(tv.price,tv.totalMatchedAmount.floor))
			val validatedPrevTradedVolumeDelta = prevMarketRunner._2.map(tv => new PriceTradedVolume(tv.price,tv.totalMatchedAmount.floor))
			val validatedMarketRunner = (validatedMarketPrices,validatedTradedVolumeDelta)
			val validatedPrevMarketRunner = (validatedPrevMarketPrices,validatedPrevTradedVolumeDelta)

			val tradedVolumeDelta = MarketEventCalculator.calculateTradedVolumeDelta(validatedMarketRunner._2.toList,validatedPrevMarketRunner._2)
			val intermediatePrices = MarketEventCalculator.calculateMarketEventsForTradedVolume(marketId, runnerId)(validatedPrevMarketRunner._1, tradedVolumeDelta)
			val runnerPricesDelta = MarketEventCalculator.calculateRunnerPricesDelta(validatedMarketRunner._1.toList,intermediatePrices._1)
			val marketEvents = MarketEventCalculator.calculateMarketEvents(marketId,runnerId)(runnerPricesDelta)

			intermediatePrices._2 ::: marketEvents 
	}


	/**Calculates market events for the delta between the previous and the current state of the market runner.
	 * 
	 * @param marketId The market id that the bet placement events are calculated for. 
	 * @param runnerId The market runner id that the bet placement events are calculated for. 
	 * @param marketRunnerDelta Delta between the new and the previous state of the market runner (both runner prices and traded volume combined to runner prices).
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for the market runner
	 */
	def calculateMarketEvents(marketId:Long,runnerId:Long)(marketRunnerDelta:List[IRunnerPrice]): List[String] = {
		require(marketRunnerDelta.find(p => p.totalToBack>0 && p.totalToLay>0).isEmpty,"Price with both totalToBack and totalToLay bigger than 0 is not allowed. RunnerPrices=" + marketRunnerDelta);
require(marketRunnerDelta.find(p => p.totalToBack<0 && p.totalToLay<0).isEmpty,"Price with both totalToBack and totalToLay less than 0 is not allowed. RunnerPrices=" + marketRunnerDelta);

		/**Create cancel events.*/
		val cancelBetEvents:List[Tuple2[Double,String]] = for {
			deltaRunnerPrice <- marketRunnerDelta 
			if(deltaRunnerPrice.totalToBack < 0 || deltaRunnerPrice.totalToLay<0) 
				val cancelBetEvent = if(deltaRunnerPrice.totalToBack<0)
					deltaRunnerPrice.price -> cancelBetsEvent(-deltaRunnerPrice.totalToBack,deltaRunnerPrice.price,"LAY",marketId,runnerId)				
					else	
						deltaRunnerPrice.price -> cancelBetsEvent(-deltaRunnerPrice.totalToLay,deltaRunnerPrice.price,"BACK",marketId,runnerId)				
		} yield cancelBetEvent

		/**Create bet events.*/
		val placeBetEvents:List[Tuple2[Double,String]] = for {
			deltaRunnerPrice <- marketRunnerDelta 
			if(deltaRunnerPrice.totalToBack > 0 || deltaRunnerPrice.totalToLay>0) 
				val betEvent = if(deltaRunnerPrice.totalToBack>0)
					deltaRunnerPrice.price -> placeBetEvent(deltaRunnerPrice.totalToBack,deltaRunnerPrice.price,"LAY",marketId,runnerId)
					else	
						deltaRunnerPrice.price -> placeBetEvent(deltaRunnerPrice.totalToLay,deltaRunnerPrice.price,"BACK",marketId,runnerId)	
		} yield betEvent

		/**Sort all tuples by price and map them to events.*/
		(cancelBetEvents ::: placeBetEvents).sortWith((a,b) => a._1<b._1).map(_._2)
	}

	/**Combines delta for runner prices with delta for traded volume and represents it as runner prices.
	 * 
	 * @param  runnerPricesDelta
	 * @param runnerTradedVolumeDelta
	 * 
	 * Example:
	 * runner price[price,toBack,toLay] = 1.9,2,0
	 * traded volume [price,volume] = 1.9,5
	 * runner price + traded volume = [price,toBack+volume,toLay+volume] = 1.9,7,5
	 * */
	def combine(runnerPricesDelta:List[IRunnerPrice], runnerTradedVolumeDelta:List[IPriceTradedVolume]):List[IRunnerPrice] = {
		val allPrices = (runnerPricesDelta.map(_.price) :::runnerTradedVolumeDelta.map(_.price)).distinct

		/**Total delta represents both runnerPricesDelta and tradedVolumeDelta in a form of runner prices.*/
		val totalDelta = for {
			price <- allPrices
			val deltaRunnerPrice = runnerPricesDelta.find(_.price==price).getOrElse(new RunnerPrice(price,0,0))
			val deltaTradedVolume = runnerTradedVolumeDelta.find(_.price==price).getOrElse(new PriceTradedVolume(price,0))
			val totalRunnerPriceDelta = new RunnerPrice(deltaRunnerPrice.price,deltaTradedVolume.totalMatchedAmount + deltaRunnerPrice.totalToBack,deltaTradedVolume.totalMatchedAmount + deltaRunnerPrice.totalToLay)
		} yield totalRunnerPriceDelta

		totalDelta
	}
	/**Calculates delta between the new and the previous state of the runner prices.
	 * 
	 * @param newRunnerPrices
	 * @param previousRunnerPrices
	 * @return Delta between the new and the previous state of the runner prices.
	 */
	def calculateRunnerPricesDelta(newRunnerPrices:List[IRunnerPrice],previousRunnerPrices:List[IRunnerPrice]):List[IRunnerPrice] = {

		val allPrices = (newRunnerPrices.map(_.price) :::previousRunnerPrices.map(_.price)).distinct

		/**Get delta between new and previous market prices.*/
		val deltaForRunnerPrices = for {
			price <- allPrices
			val newRunnerPrice = newRunnerPrices.find(_.price==price).getOrElse(new RunnerPrice(price,0,0))
			val previousRunnerPrice = previousRunnerPrices.find(_.price==price).getOrElse(new RunnerPrice(price,0,0))
			val runnerPriceDelta = new RunnerPrice(newRunnerPrice.price,newRunnerPrice.totalToBack-previousRunnerPrice.totalToBack,newRunnerPrice.totalToLay-previousRunnerPrice.totalToLay)
			if(runnerPriceDelta.totalToBack != 0 || runnerPriceDelta.totalToLay !=0)
		} yield runnerPriceDelta

		deltaForRunnerPrices
	}

	/**Calculates delta between the new and the previous state of the runner traded volume.
	 * 
	 * @param newTradedVolumes
	 * @param previousTradedVolumes
	 * @return Delta between the new and the previous state of the runner traded volume.
	 */
	def calculateTradedVolumeDelta(newTradedVolumes:List[IPriceTradedVolume],previousTradedVolumes:List[IPriceTradedVolume]):List[IPriceTradedVolume] = {
		val allPrices = (newTradedVolumes.map(_.price) :::previousTradedVolumes.map(_.price)).distinct

		/**Get delta between new and previous prices traded volume.*/
		val deltaForUpdatedAndNewTradedVolume = for {
			price <- allPrices
			val newTradedVolume = newTradedVolumes.find(_.price==price).getOrElse(new PriceTradedVolume(price,0))
			val previousTradedVolume = previousTradedVolumes.find(_.price==price).getOrElse(new PriceTradedVolume(price,0))
			val tradedVolumeDelta = new PriceTradedVolume(newTradedVolume.price,(newTradedVolume.totalMatchedAmount-previousTradedVolume.totalMatchedAmount))

			if(tradedVolumeDelta.totalMatchedAmount != 0)
		} yield tradedVolumeDelta

		deltaForUpdatedAndNewTradedVolume
	}

	/**This function transforms runner from state A to B. 
	 * State A is represented by runner prices in state A and traded volume delta between states B and A. 
	 * The state B is represented the list of market events that reflect traded volume delta between states B and A
	 *  and by runner prices in state B.
	 * */
	def calculateMarketEventsForTradedVolume(marketId:Long,runnerId:Long)(previousRunnerPrices:List[IRunnerPrice],runnerTradedVolumeDelta:List[IPriceTradedVolume]):Tuple2[List[IRunnerPrice],List[String]] = {

		require(previousRunnerPrices.find(p => p.totalToBack>0 && p.totalToLay>0).isEmpty,"Price with both totalToBack and totalToLay bigger than 0 is not allowed. RunnerPrices=" + previousRunnerPrices);
		require(previousRunnerPrices.find(p => p.totalToBack==0 && p.totalToLay==0).isEmpty,"Price with both totalToBack and totalToLay to be 0 is not allowed. RunnerPrices=" + previousRunnerPrices);
		require(runnerTradedVolumeDelta.find(tv =>tv.totalMatchedAmount<0).isEmpty,"Price with negative traded volume is not allowed. TradedVolumeDelta=" + runnerTradedVolumeDelta)

		val bestPriceToBack =previousRunnerPrices.filter(p => p.totalToBack>0).foldLeft(1.0)((a,b) => if(b.price>a) b.price else a)
		val bestPriceToLay = previousRunnerPrices.filter(p => p.totalToLay>0).foldLeft(1001d)((a,b) => if(b.price<a) b.price else a)
		require(bestPriceToBack<bestPriceToLay,"Best price to back is on higher price than best price to lay. Runner prices=" + previousRunnerPrices)

		def calculateRecursive(betType:BetTypeEnum,updatedRunnerPrices:List[IRunnerPrice],runnerTradedVolumes: Iterator[IPriceTradedVolume],marketEvents:List[String]):Tuple2[List[IRunnerPrice],List[String]] = {

			val runnerTradedVolume = runnerTradedVolumes.next
			val runnerPrice = updatedRunnerPrices.find(p => p.price==runnerTradedVolume.price).getOrElse(new RunnerPrice(runnerTradedVolume.price,0,0))
			val toBackDelta = (runnerTradedVolume.totalMatchedAmount - runnerPrice.totalToBack).max(0)
			val toLayDelta = (runnerTradedVolume.totalMatchedAmount - runnerPrice.totalToLay).max(0)
			val newTotalToBack = (runnerPrice.totalToBack - toLayDelta).max(0)
			val newTotalToLay=(runnerPrice.totalToLay - toBackDelta).max(0)

			val placeBackBetEvents:List[String] = if(toLayDelta>0) placeBetEvent(toLayDelta,runnerTradedVolume.price,"BACK",marketId,runnerId) :: Nil else Nil
			val placeLayBetEvents = if(toBackDelta>0) placeBetEvent(toBackDelta,runnerTradedVolume.price,"LAY",marketId,runnerId)  :: Nil else Nil

			val newUpdateRunnerPrices = if(newTotalToBack>0 || newTotalToLay>0) new RunnerPrice(runnerTradedVolume.price,newTotalToBack,newTotalToLay) :: updatedRunnerPrices.filterNot(p => p.price==runnerTradedVolume.price)
			else updatedRunnerPrices.filterNot(p => p.price==runnerTradedVolume.price)

			/**Cancel bets on higher prices*/
			val partition = betType match {
			case BACK =>newUpdateRunnerPrices.partition(p => p.price<=runnerTradedVolume.price)
			case LAY =>newUpdateRunnerPrices.partition(p => p.price>=runnerTradedVolume.price)
			}

			val cancelBetsEvents = betType match {
			case BACK => partition._2.map(p =>cancelBetsEvent(p.totalToBack,p.price,"LAY",marketId,runnerId))
			case LAY => partition._2.map(p =>cancelBetsEvent(p.totalToLay,p.price,"BACK",marketId,runnerId))
			}

			val newEvents = cancelBetsEvents ::: placeLayBetEvents ::: placeBackBetEvents
			if(runnerTradedVolumes.hasNext) calculateRecursive(betType,partition._1,runnerTradedVolumes,marketEvents:::newEvents)
			else Tuple2(partition._1,marketEvents:::newEvents)
		}

		/**Process traded volume on pricesToBack moving from the highest price to the lowest.*/
		val tradedVolumeOnPricesToBack = runnerTradedVolumeDelta.filter(tv => tv.price<bestPriceToLay && tv.totalMatchedAmount>0).sortWith((a,b) => a.price>b.price).iterator
		val pricesToBack = previousRunnerPrices.filter(p => p.price<bestPriceToLay)
		val resultForPricesToBack = if(!tradedVolumeOnPricesToBack.isEmpty)
			calculateRecursive(BACK,pricesToBack,tradedVolumeOnPricesToBack,List())
			else Tuple2(pricesToBack,List())

			/**Process traded volume on pricesToLay moving from the highest price to the lowest.*/
			val tradedVolumeOnPricesToLay = runnerTradedVolumeDelta.filter(tv => tv.price >=bestPriceToLay && tv.totalMatchedAmount>0 ).sortWith((a,b) => a.price<b.price).iterator
			val pricesToLay = previousRunnerPrices.filter(p => p.price>=bestPriceToLay)
			val resultForPricesToLay = if(!tradedVolumeOnPricesToLay.isEmpty)
				calculateRecursive(LAY,pricesToLay,tradedVolumeOnPricesToLay,List())
				else Tuple2(pricesToLay,List())

				(resultForPricesToBack._1 ::: resultForPricesToLay._1, resultForPricesToBack._2 ::: resultForPricesToLay._2)
	}

	private def placeBetEvent(betSize:Double,betPrice:Double,betType:String,marketId:Long,runnerId:Long):String = {
		"""{"eventType":"PLACE_BET","betSize":%s,"betPrice":%s,"betType":"%s","marketId":%s,"runnerId":%s}""".format(betSize,betPrice,betType,marketId,runnerId)
	}

	private def cancelBetsEvent(betsSize:Double,betPrice:Double,betType:String,marketId:Long,runnerId:Long):String = {
		"""{"eventType":"CANCEL_BETS","betsSize":%s,"betPrice":%s,"betType":"%s","marketId":%s,"runnerId":%s}""".format(betsSize,betPrice,betType,marketId,runnerId)
	}

}