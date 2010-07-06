package dk.bettingai.marketcollector

import dk.bettingai.marketsimulator.betex.api.IMarket._
import dk.bettingai.marketsimulator.betex.Market._

/**This trait represents a function that calculates market events for the delta between the previous and the current state of the market runner.
 * 
 * @author KorzekwaD
 *
 */
object MarketEventCalculator  extends IMarketEventCalculator{


	/**Calculates market events for the delta between the previous and the current state of the market runner.
	 * 
	 * @param userId The user Id that the bet placement events are calculated for.
	 * @param marketId The market id that the bet placement events are calculated for. 
	 * @param runnerId The market runner id that the bet placement events are calculated for. 
	 * @param runnerPricesDelta Delta between the new and the previous state of the runner prices.
	 * @param runnerTradedVolumeDelta Delta between the new and the previous state of the runner traded volume.
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for the market runner
	 */
	def calculateRunnerDelta(userId:Long,marketId:Long,runnerId:Long)(runnerPricesDelta:List[IRunnerPrice], runnerTradedVolumeDelta:List[IPriceTradedVolume]): List[String] = {

		val allPrices = (runnerPricesDelta.map(_.price) :::runnerTradedVolumeDelta.map(_.price)).distinct

		/**Total delta represents both runnerPricesDelta and tradedVolumeDelta in a form of runner prices.*/
		val totalDelta = for {
			price <- allPrices
			val deltaRunnerPrice = runnerPricesDelta.find(_.price==price).getOrElse(new RunnerPrice(price,0,0))
			val deltaTradedVolume = runnerTradedVolumeDelta.find(_.price==price).getOrElse(new PriceTradedVolume(price,0))
			val totalRunnerPriceDelta = new RunnerPrice(deltaRunnerPrice.price,deltaTradedVolume.totalMatchedAmount + deltaRunnerPrice.totalToBack,deltaTradedVolume.totalMatchedAmount + deltaRunnerPrice.totalToLay)
		} yield totalRunnerPriceDelta

		/**Create lay bet events for delta between the new and the previous runner states.*/
		val placeLayBetEvents:List[String] = for {
			deltaMarketPrice <- totalDelta 
			if(deltaMarketPrice.totalToBack > 0) 
				val placeLayBetEvent = """{"eventType":"PLACE_BET","userId":%s,"betId":%s,"betSize":%s,"betPrice":%s,"betType":"%s","marketId":%s,"runnerId":%s}""".format(123,100,deltaMarketPrice.totalToBack,deltaMarketPrice.price,"LAY",marketId,runnerId)
		} yield placeLayBetEvent

		/**Create back bet events for delta between the new and the previous runner states.*/
		val placeBackBetEvents:List[String] = for {
			deltaMarketPrice <- totalDelta 
			if(deltaMarketPrice.totalToLay > 0) 
				val placeBackBetEvent = """{"eventType":"PLACE_BET","userId":%s,"betId":%s,"betSize":%s,"betPrice":%s,"betType":"%s","marketId":%s,"runnerId":%s}""".format(123,100,deltaMarketPrice.totalToLay,deltaMarketPrice.price,"BACK",marketId,runnerId)
		} yield placeBackBetEvent

		placeLayBetEvents ::: placeBackBetEvents
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
			val tradedVolumeDelta = new PriceTradedVolume(newTradedVolume.price,newTradedVolume.totalMatchedAmount-previousTradedVolume.totalMatchedAmount)
			if(tradedVolumeDelta.totalMatchedAmount != 0)
		} yield tradedVolumeDelta

		deltaForUpdatedAndNewTradedVolume
	}
}