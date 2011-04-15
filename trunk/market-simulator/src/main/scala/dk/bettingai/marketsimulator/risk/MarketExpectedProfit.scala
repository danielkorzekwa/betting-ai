package dk.bettingai.marketsimulator.risk

import scala.collection._

/** 
 * @param marketExpcetedProfit Market expected profit from bets and probabilities 
 * @param wealth (http://en.wikipedia.org/wiki/Kelly_criterion)
 * @param runnersIfWin If wins values for all market runners. IfWin = What was the market profit if the runner would win.
 * @param probabilities, key - runnerId, value - runner probability
 */
case class MarketExpectedProfit(val marketExpectedProfit:Double,val wealth:Double, val runnersIfWin:Map[Long,Double],val probabilities:Map[Long,Double]) {

	def ifLose(runnerId:Long):Double = {
			val ifWin = runnersIfWin(runnerId)
			val prob = probabilities(runnerId)
			(marketExpectedProfit - ifWin*prob)/(1-prob)
	}

	def ifWin(runnerId:Long):Double = runnersIfWin(runnerId)

	override def toString = "MarketExpectedProfit [marketExpectedProfit=%s, wealth=%s,runnersIfWin=%s, probabilities=%s]".format(marketExpectedProfit,wealth,runnersIfWin,probabilities)
}