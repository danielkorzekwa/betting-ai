package dk.bettingai.marketcollector.eventproducer

import java.util.Date
import dk.bettingai.marketsimulator.betex._
import Market._
import dk.bettingai.marketcollector._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex.api.IMarket._
import dk.bettingai.marketcollector.eventcalculator._
import EventProducer._

/**This trait represents a service that transforms state of a market on a betting exchange into the stream of events.
 * 
 * @author KorzekwaD
 *
 */
object EventProducer {

	class EventProducerVerificationError(val message:String,val prevRunnerData:Tuple2[List[IRunnerPrice],IRunnerTradedVolume],
			val newRunnerData:Tuple2[List[IRunnerPrice],IRunnerTradedVolume],
			val toVerifyRunnerData:Tuple2[List[IRunnerPrice],IRunnerTradedVolume],val events:List[String]) extends RuntimeException(message)
}

class EventProducer extends IEventProducer{

	private val betex = new Betex()
	private val marketEventProcessor = new MarketEventProcessorImpl(betex)
	private var nextBetIdValue=1
	private val nextBetId = () => {nextBetIdValue = nextBetIdValue +1;nextBetIdValue}

	/**Transforms state of market on a betting exchange into the stream of events. 
	 * 
	 * This is a stateful service that is keeping recent states for all markets that this method has been called.
	 * First call of this method for a given market returns list of events that represents delta between empty state of a market(no prices and traded volume) and market state passed to this method.
	 * Next call of this method for the same market returns list of events that represents delta between previous and new state of a market.
	 * 
	 * @param timestamp for all generated events
	 * @param marketId Market id.
	 * @param marketRunners Market state represented by runner prices and price traded volume.
	 * 
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for a market
	 * */
	def produce(timestamp:Long,marketId:Long,marketRunners: Map[Long,Tuple2[List[IRunnerPrice],IRunnerTradedVolume]]):List[String] ={

			/**Round down all unmatched and matched volume.*/
			def floorMarketRunner(marketRunner: Tuple2[List[IRunnerPrice],IRunnerTradedVolume]):Tuple2[List[IRunnerPrice],IRunnerTradedVolume] = {
					val validatedMarketPrices = marketRunner._1.map(r => new RunnerPrice(r.price,r.totalToBack.floor,r.totalToLay.floor)).filterNot(p => p.totalToBack==0 && p.totalToLay==0)
					val validatedTradedVolumeDelta = new RunnerTradedVolume(marketRunner._2.pricesTradedVolume.map(tv => new RunnerTradedVolume.PriceTradedVolume(tv.price,tv.totalMatchedAmount.floor)).filterNot(p => p.totalMatchedAmount==0))
					val validatedMarketRunner = (validatedMarketPrices,validatedTradedVolumeDelta)
					validatedMarketRunner
			}
			val validatedMarketRunners = marketRunners.map(entry => (entry._1,floorMarketRunner(entry._2)))

			/**Add market if not exists yet.*/
			val betexMarket = try {
				betex.findMarket(marketId)
			}
			catch {
			case e:Exception => {
				val runners = validatedMarketRunners.keys.map(runnerId => new Market.Runner(runnerId,"n/a")).toList
				betex.createMarket(marketId, "n/a", "n/a", 1, new Date(0), runners)
			}
			}

			val marketEvents = generateMarketEvents(timestamp,marketId,validatedMarketRunners)
			processEvents(marketId, validatedMarketRunners, marketEvents)
			marketEvents.foldLeft(List[String]())((a,b) => a ::: b._2)
	}

	/**Generate events for all market runners.
	 * 
	 * @param timestamp for all generated events
	 * @param marketId
	 * @param validatedMarketRunners
	 * 
	 * @return List of Tuple2[runnerId, runnerEvents]
	 * */
	private def generateMarketEvents(timestamp:Long, marketId:Long,validatedMarketRunners:Map[Long,Tuple2[List[IRunnerPrice],IRunnerTradedVolume]]):Iterable[Tuple2[Long,List[String]]] = {
			val betexMarket = betex.findMarket(marketId)	
			val marketEvents = for{(runnerId,runnerData) <- validatedMarketRunners

				val previousRunnerPrices = betexMarket.getRunnerPrices(runnerId)
				val previousTradedVolume = betexMarket.getRunnerTradedVolume(runnerId)
				val prevRunnerData = (previousRunnerPrices,previousTradedVolume)

				val runnerEvents = MarketEventCalculator.produce(timestamp,marketId,runnerId,runnerData,prevRunnerData)		
			} yield (runnerId, runnerEvents)
			marketEvents
	}

	/**Add all events to betex and verify events correctness.
	 * 
	 * @param marketId
	 * @param validatedMarketRunners
	 * @param marketEvents List of Tuple2[runnerId, runnerEvents]
	 * @throw EventProducerVerificationError
	 * */
	private def processEvents(
			marketId:Long,validatedMarketRunners:Map[Long,Tuple2[List[IRunnerPrice],IRunnerTradedVolume]],
			marketEvents: Iterable[Tuple2[Long,List[String]]]) {
		
		val betexMarket = betex.findMarket(marketId)		

		for((runnerId,runnerEvents) <- marketEvents) {
			val previousRunnerPrices = betexMarket.getRunnerPrices(runnerId)
			val previousTradedVolume = betexMarket.getRunnerTradedVolume(runnerId)

			/**Add events to betex.*/
			runnerEvents.foreach(event => marketEventProcessor.process(event,nextBetId(),100))

			/**Verify events correctness.*/
			val toVerifyRunnerPrices = betexMarket.getRunnerPrices(runnerId)
			val toVerifyTradedVolume = betexMarket.getRunnerTradedVolume(runnerId)

			def throwVerificationError(message:String) {
				throw new EventProducerVerificationError(message,
						(previousRunnerPrices,previousTradedVolume),validatedMarketRunners(runnerId),
						(toVerifyRunnerPrices,toVerifyTradedVolume),runnerEvents)
			}

			/**Verify of runner events represents delta between two runner states.*/
			if(validatedMarketRunners(runnerId)._1.size != toVerifyRunnerPrices.size) throwVerificationError("Runner prices sizes are not the same: " + validatedMarketRunners(runnerId)._1.size + "!=" + toVerifyRunnerPrices.size)
			for((newRunnerPrice,toVerifyRunnerPrice) <- validatedMarketRunners(runnerId)._1.zip(toVerifyRunnerPrices)) {
				if(newRunnerPrice.price!=toVerifyRunnerPrice.price) throwVerificationError("Price is not the same=" + (newRunnerPrice,toVerifyRunnerPrice))
				if(newRunnerPrice.totalToBack!=toVerifyRunnerPrice.totalToBack) throwVerificationError("TotalToBack is not the same=" + (newRunnerPrice,toVerifyRunnerPrice))
				if(newRunnerPrice.totalToLay!=toVerifyRunnerPrice.totalToLay) throwVerificationError("TotalToLay is not the same=" + (newRunnerPrice,toVerifyRunnerPrice))
			}
			if(validatedMarketRunners(runnerId)._2.pricesTradedVolume.size != toVerifyTradedVolume.pricesTradedVolume.size) throwVerificationError("Traded volume sizes are not the same: " + validatedMarketRunners(runnerId)._2.pricesTradedVolume.size + "!=" + toVerifyTradedVolume.pricesTradedVolume.size)
			for((newTradedVolume,toVerifyTradedVolume) <-validatedMarketRunners(runnerId)._2.pricesTradedVolume.zip(toVerifyTradedVolume.pricesTradedVolume)) {
				if(newTradedVolume.price!=toVerifyTradedVolume.price) throwVerificationError("Price is not the same=" + (newTradedVolume,toVerifyTradedVolume))
				if(newTradedVolume.totalMatchedAmount!=toVerifyTradedVolume.totalMatchedAmount) throwVerificationError("TotalAmountMatched is not the same=" + (newTradedVolume,toVerifyTradedVolume))
			}
		}
	}
}