package dk.bettingai.marketsimulator.betex

import java.util.Date
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex.api.IBet._
import scala.collection.mutable.ListBuffer
import IBet.BetStatusEnum._
import IBet.BetTypeEnum._
import Market._
import IMarket._
import IRunnerTradedVolume._

/**This class represents a market on a betting exchange.
 * @author korzekwad
 *
 */
object Market {
	class Runner(val runnerId:Long, val runnerName:String) extends IMarket.IRunner {
		override def toString = "Runner [runnerId=%s, runnerName=%s]".format(runnerId, runnerName)
	}

	class RunnerPrice(val price:Double,val totalToBack:Double,val totalToLay: Double) extends IMarket.IRunnerPrice {
		override def toString = "RunnerPrice [price=%s, totalToBack=%s, totalToLay=%s]".format(price,totalToBack,totalToLay)
	}

	
	
}

class Market(val marketId:Long, val marketName:String,val eventName:String,val numOfWinners:Int, val marketTime:Date,val runners:List[IMarket.IRunner]) extends IMarket{
	/**key - runnerId, value - runnerBackBetsPerPrice*/
	private val backBets = scala.collection.mutable.Map[Long,scala.collection.mutable.Map[Double,ListBuffer[IBet]]]()
	/**key - runnerId, value - runnerLayBetsPerPrice*/
	private val layBets = scala.collection.mutable.Map[Long,scala.collection.mutable.Map[Double,ListBuffer[IBet]]]()

	private val matchedBets = ListBuffer[IBet]()
	private val betsIds = scala.collection.mutable.Set[Long]()

	require(numOfWinners>0,"numOfWinners should be bigger than 0, numOfWinners=" + numOfWinners)
	require(runners.size>1,"Number of market runners should be bigger than 1, numOfRunners=" + runners.size)

	private def getRunnerBets(runnerId:Long,bets: scala.collection.mutable.Map[Long,scala.collection.mutable.Map[Double,ListBuffer[IBet]]]):scala.collection.mutable.Map[Double,ListBuffer[IBet]] = {
		bets.getOrElseUpdate(runnerId,scala.collection.mutable.Map[Double,ListBuffer[IBet]]())
	}

	/** Places a bet on a betting exchange market.
	 * 
	 * @param betId
	 * @param userId
	 * @param betSize
	 * @param betPrice
	 * @param betType
	 * @param runnerId
	 * 
	 * @return The bet that was placed.
	 */
	def placeBet(betId:Long,userId: Long, betSize:Double, betPrice:Double, betType:BetTypeEnum, runnerId:Long):IBet = {
		require(betSize>0, "Bet size must be >0, betSize=" + betSize)
		require(runners.exists(s => s.runnerId==runnerId),"Can't place bet on a market. Market runner not found for marketId/runnerId=" + marketId + "/" + runnerId)
		require(!betsIds.contains(betId),"Bet for betId=%s already exists".format(betId))

		val pricesToBeMatched = betType match {
		case LAY => getRunnerBets(runnerId,backBets).keys.filter(p => p <= betPrice).toList.sortWith((a,b) => a<b).iterator
		case BACK => getRunnerBets(runnerId,layBets).keys.filter(p => p >= betPrice).toList.sortWith((a,b) => a>b).iterator
		}
		val betsToBeMatched:scala.collection.mutable.Map[Double,ListBuffer[IBet]] = betType match {
		case LAY => getRunnerBets(runnerId,backBets)
		case BACK => getRunnerBets(runnerId,layBets)
		}

		val betsToBeAdded:scala.collection.mutable.Map[Double,ListBuffer[IBet]] = betType match {
		case LAY => getRunnerBets(runnerId,layBets)
		case BACK => getRunnerBets(runnerId,backBets)
		}

			def matchBet(newBet:IBet,priceToMatch:Double):Unit = {

				val priceBets = betsToBeMatched.getOrElseUpdate(priceToMatch,ListBuffer())
				if(!priceBets.isEmpty) {
					/**Get bet to be matched and remove it from the main list of bets - it will be added later as a result of matching.*/
					val betToBeMatched = priceBets.head
					priceBets.remove(0)

					/**Do the bets matching.*/
					val matchingResult = newBet.matchBet(betToBeMatched)
					matchingResult.filter(b => b.betStatus ==U && b.betId!=betId).foreach(b => priceBets.insert(0,b))
					matchingResult.filter(b => b.betStatus ==M).foreach(b => matchedBets += b)

					/**Find unmatched portion for a bet being placed.*/
					val unmatchedPortion = matchingResult.find(b => b.betId==betId && b.betStatus==U)
					if(!unmatchedPortion.isEmpty) {
						matchBet(new Bet(betId,userId, unmatchedPortion.get.betSize, betPrice, betType, U,marketId,runnerId),priceToMatch)
					}
				}
				else {
					if(pricesToBeMatched.hasNext) {
						matchBet(newBet,pricesToBeMatched.next)
					}
					else {
						betsToBeAdded.getOrElseUpdate(betPrice,ListBuffer()) += newBet
					}
				}
		}
		
		betsIds += betId
		
		val bet = new Bet(betId,userId, betSize, betPrice, betType, U,marketId,runnerId)
		
		if(pricesToBeMatched.hasNext) matchBet(bet,pricesToBeMatched.next)
		else betsToBeAdded.getOrElseUpdate(betPrice,ListBuffer()) += bet
		
		bet
	}

	/** Cancels a bet on a betting exchange market.
	 *
	 * @param betId Unique id of a bet to be cancelled.
	 * 
	 * @return amount cancelled
	 * @throws NoSuchElementException is thrown if no unmatched bet for betId/userId found.
	 */
	def cancelBet(betId:Long):Double = {

			def cancelBet2(bets: ListBuffer[IBet]): Double = {
					val betToBeCancelled = bets.find(b => b.betId==betId)
					if(!betToBeCancelled.isEmpty) {
						bets -=  betToBeCancelled.get
						betToBeCancelled.get.betSize
					}
					else {
						0
					}
			}

			def cancelBet(bets: scala.collection.mutable.Map[Double,ListBuffer[IBet]]): Double = {

					val value = for {
						bets <-bets.values
						val s = cancelBet2(bets)
						if(s>0)
					} yield s

					if(!value.isEmpty) value.head else 0
			}


			val value = for {
				betsMap <- backBets.values
				val s = cancelBet(betsMap)
				if(s>0)
			} yield s

			if(!value.isEmpty) {
				value.head
			}
			else {
				val value = for {
					betsMap <- layBets.values
					val s = cancelBet(betsMap)
					if(s>0)
				} yield s

				if(!value.isEmpty) {
					value.head
				}
				else {
					throw new NoSuchElementException("Bet not found for bet id=" + betId)
				}

			}
	}

	/** Cancels bets on a betting exchange market.
	 * 
	 * @param userId 
	 * @param betsSize Total size of bets to be cancelled.
	 * @param betPrice The price that bets are cancelled on.
	 * @param betType
	 * @param runnerId 
	 * 
	 * @return Amount cancelled. Zero is returned if nothing is available to cancel.
	 */
	def cancelBets(userId:Long,betsSize:Double,betPrice:Double,betType:BetTypeEnum,runnerId:Long):Double = {

			val bets = betType match {
			case BACK => backBets
			case LAY => layBets
			}

			val runnerBets = bets.getOrElseUpdate(runnerId,scala.collection.mutable.Map[Double,ListBuffer[IBet]]())
			val priceBets = runnerBets.getOrElseUpdate(betPrice,new ListBuffer[IBet]())

			val betsToBeCancelled = priceBets.filter(b => b.userId==userId).reverseIterator

			def cancelRecursively(amountToCancel:Double,amountCancelled:Double):Double = {
				val betToCancel = betsToBeCancelled.next
				val betCanceledAmount = if(amountToCancel>=betToCancel.betSize) {
					priceBets -= betToCancel
					betToCancel.betSize
				}
				else {
					val updatedBet = Bet(betToCancel.betId,betToCancel.userId,betToCancel.betSize - amountToCancel,betToCancel.betPrice,betToCancel.betType,betToCancel.marketId,betToCancel.runnerId)
					priceBets.update(priceBets.indexOf(betToCancel,0),updatedBet)
					amountToCancel
				}
				val newAmountToCancel = amountToCancel-betCanceledAmount
				val newAmountCancelled = amountCancelled+betCanceledAmount
				if(betsToBeCancelled.hasNext && newAmountToCancel>0) cancelRecursively(newAmountToCancel,newAmountCancelled)
				else newAmountCancelled
			}

			val totalCancelled = if(betsToBeCancelled.hasNext) cancelRecursively(betsSize,0) else 0
			totalCancelled
	}

	/** Returns total unmatched volume to back and to lay at all prices for all runners in a market on a betting exchange. 
	 *  Prices with zero volume are not returned by this method.
	 * 
	 * @param runnerId Unique runner id that runner prices are returned for.
	 * @return
	 */
	def getRunnerPrices(runnerId:Long):List[IMarket.IRunnerPrice] = {
			require(runners.exists(s => s.runnerId==runnerId),"Market runner not found for marketId/runnerId=" + marketId + "/" + runnerId)

			val allBackBets = for{
				runnerBackBetsMap <- backBets.values
				val runnerBets = runnerBackBetsMap.values.foldLeft(List[IBet]())((a,b) => a.toList ::: b.toList).filter(b => b.runnerId == runnerId)
			} yield runnerBets

			val allLayBets = for{
				runnerLayBetsMap <- layBets.values
				val runnerBets = runnerLayBetsMap.values.foldLeft(List[IBet]())((a,b) => a.toList ::: b.toList).filter(b => b.runnerId == runnerId)
			} yield runnerBets

			val allBackBetsList = allBackBets.foldLeft(List[IBet]())((a,b) => a ::: b)
			val allLayBetsList = allLayBets.foldLeft(List[IBet]())((a,b) => a ::: b)

			val betsByPriceMap = (allBackBetsList.toList ::: allLayBetsList.toList).toList.groupBy(b => b.betPrice) 

			def totalStake(bets: List[IBet],betType:BetTypeEnum) = bets.filter(b => b.betType==betType).foldLeft(0d)(_ + _.betSize)
			betsByPriceMap.map( entry => new RunnerPrice(entry._1,totalStake(entry._2,LAY),totalStake(entry._2,BACK))).toList.sortWith(_.price<_.price)
	}

	/**Returns best toBack/toLay prices for market runner.
	 * Element 1 - best price to back, element 2 - best price to lay
	 * Double.NaN is returned if price is not available.
	 * @return 
	 * */
	def getBestPrices(runnerId: Long):Tuple2[IRunnerPrice,IRunnerPrice] = {
			require(runners.exists(s => s.runnerId==runnerId),"Market runner not found for marketId/runnerId=" + marketId + "/" + runnerId)

			val runnerLayBetsMap = getRunnerBets(runnerId,layBets)
			val runnerBackBetsMap = getRunnerBets(runnerId,backBets)

			val pricesToBack = runnerLayBetsMap.filter(entry => !entry._2.isEmpty).keys
			val pricesToLay = runnerBackBetsMap.filter(entry => !entry._2.isEmpty).keys

			val bestPriceToBack = if(!pricesToBack.isEmpty) {
				val price = pricesToBack.max 
				val totalStake = BetUtil.totalStake(runnerLayBetsMap(price).toList)
				new RunnerPrice(price,totalStake,0d)
				
			}
			else new RunnerPrice(Double.NaN,0d,0d)
			val bestPriceToLay = if(!pricesToLay.isEmpty) {
				val price = pricesToLay.min 
				val totalStake = BetUtil.totalStake(runnerBackBetsMap(price).toList)
				new RunnerPrice(price,0d,totalStake)
			}
			else new RunnerPrice(Double.NaN,0d,0d)
			

			new Tuple2(bestPriceToBack,bestPriceToLay)
	}
	
	/**Returns best toBack/toLay prices for market.
	 * 
	 * @return Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
	 */
	def getBestPrices():Map[Long,Tuple2[IRunnerPrice,IRunnerPrice]] = {
		Map(runners.map(r => r.runnerId -> getBestPrices(r.runnerId)) : _*)
	}

	/**Returns total traded volume for all prices on all runners in a market.*/
	def getRunnerTradedVolume(runnerId:Long): IRunnerTradedVolume = {
			require(runners.exists(s => s.runnerId==runnerId),"Market runner not found for marketId/runnerId=" + marketId + "/" + runnerId)

			/**Take only BACK bets to not double count traded volume (each matched back bet has corresponding matched lay bet.*/
			val betsByPrice = matchedBets.toList.filter(b => b.betType==BACK && b.runnerId==runnerId).groupBy(b => b.betPrice)

			/**Map betsByPrice to list of PriceTradedVolume.*/
			val pricesTradedVolume = betsByPrice.map( entry => new RunnerTradedVolume.PriceTradedVolume(entry._1,entry._2.foldLeft(0d)(_ + _.betSize))).toList.sortWith(_.price<_.price)
			new RunnerTradedVolume(pricesTradedVolume)
	}

	/**Returns all bets placed by user on that market.
	 *
	 *@param userId
	 */
	def getBets(userId:Int):List[IBet] = {

			val allBackBets = for{
				runnerBackBetsMap <- backBets.values
				val runnerBets = runnerBackBetsMap.values.foldLeft(List[IBet]())((a,b) => a.toList ::: b.toList).filter(b => b.userId == userId)
			} yield runnerBets

			val allLayBets = for{
				runnerLayBetsMap <- layBets.values
				val runnerBets = runnerLayBetsMap.values.foldLeft(List[IBet]())((a,b) => a.toList ::: b.toList).filter(b => b.userId == userId)
			} yield runnerBets

			val allBackBetsList = allBackBets.foldLeft(List[IBet]())((a,b) => a ::: b)
			val allLayBetsList = allLayBets.foldLeft(List[IBet]())((a,b) => a ::: b)

			allBackBetsList ::: allLayBetsList ::: matchedBets.filter(b => b.userId == userId).toList

	}

	/**Returns all bets placed by user on that market.
	 *
	 *@param userId
	 *@param matchedBetsOnly If true then matched bets are returned only, 
	 * otherwise all unmatched and matched bets for user are returned.
	 */
	def getBets(userId:Int,matchedBetsOnly:Boolean):List[IBet] = {
		val bets = matchedBetsOnly match {
			case true => matchedBets.filter(b => b.userId == userId).toList
			case false => getBets(userId)
		}
		bets
	}
	override def toString = "Market [marketId=%s, marketName=%s, eventName=%s, numOfWinners=%s, marketTime=%s, runners=%s]".format(marketId,marketName,eventName,numOfWinners,marketTime,runners)
}