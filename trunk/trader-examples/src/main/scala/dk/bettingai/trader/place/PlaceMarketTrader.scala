package dk.bettingai.trader.place

import dk.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import dk.bettingai.marketsimulator.trader._
import dk.betex._
import dk.betting.risk.prob._
import dk.betting.risk.liability._
import dk.betting.risk.prob._
import dk.betting.risk.liability._

class PlaceMarketTrader extends ITrader {

  def execute(ctx: ITraderContext) = {

    val probs = ProbabilityCalculator.calculate(ctx.getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), ctx.numOfWinners)

    if (ctx.numOfWinners == 1) {
      for (runner <- ctx.runners) {
        val placeProb = OrderingProb.calcPlaceProb(runner.runnerId, probs)
        ctx.addChartValue(runner.runnerId.toString, placeProb)
      }

    } else {
      probs.foreach { case (runnerId, prob) => ctx.addChartValue(runnerId.toString, prob) }

    }

  }
}