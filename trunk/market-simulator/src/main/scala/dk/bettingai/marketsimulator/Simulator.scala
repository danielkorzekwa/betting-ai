package dk.bettingai.marketsimulator

import ISimulator._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.trader._
import IMarket._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import java.io.File
import scala.collection._
import immutable.TreeMap
import scala.actors.Actor
import Actor._
import java.util.concurrent.atomic._
import MarketSimActor._
import dk.bettingai.marketsimulator.betex._

/**
 * This trait represents a simulator that processes market events, analyses trader implementation and returns analysis report for trader implementation.
 *
 * @author korzekwad
 *
 *
 * @param MarketEventProcessor
 * @param betex
 * @param commission Commission on winnings in percentage.
 *
 */
object Simulator {

  /**
   * @param commission Betting Exchanger commission
   * @param bank Amount of money in a bank (http://en.wikipedia.org/wiki/Kelly_criterion)
   * @return
   */
  def apply(commission: Double, bank: Double): Simulator = {
    val betex = new Betex()
    val simulator = new Simulator(betex, commission, bank)
    simulator
  }
}

class Simulator(betex: IBetex, commission: Double, bank: Double) extends ISimulator {

  var nextBetIdValue = new AtomicLong(1)
  def nextBetId() = nextBetIdValue.addAndGet(1)

  var lastTraderUserId = new AtomicInteger(1)
  def nextTraderUserId() = lastTraderUserId.addAndGet(1)
  val historicalDataUserId = nextTraderUserId()

  /**
   * Processes market events, analyses traders and returns analysis reports.
   *
   * @param marketDataContains market events that the market simulation is executed for. Key - marketId, value - market events
   * @param traders Traders to analyse, all are analysed on the same time, so they compete against each other
   * @param p Progress listener. Value between 0% and 100% is passed as an function argument.
   */
  def runSimulation(marketData: TreeMap[Long, File], traders: List[TraderFactory[_ <: ITrader]], p: (Int) => Unit): SimulationReport = {

    /**Register traders on a betting exchange by assigning user ids for them.*/
    val registeredTradersFactories = traders.map(trader => (nextTraderUserId(), trader))

    val numOfMarkets = marketData.size

    p(0)

    def createTraderContext(userId: Int, market: IMarket, marketSimActor: MarketSimActor): TraderContext = new TraderContext(nextBetId(), userId, market, commission, bank, this, marketSimActor)

    /**Process all markets in parallel and send back market reports.*/
    for ((marketId, marketFile) <- marketData) {

      val slave = new MarketSimActor(marketId, betex, nextBetId, historicalDataUserId, commission, createTraderContext, bank).start
      val registeredTraders = registeredTradersFactories.map { case (userId, traderFactory) => RegisteredTrader(userId, traderFactory.create()) }
      slave ! MarketSimRequest(marketId, marketFile, registeredTraders)
    }

    /**Collect market reports from slaves.*/
    val marketReports = mutable.ListBuffer[Option[MarketReport]]()
    var collectedReports = 0
    while (collectedReports != numOfMarkets) {
      receive {
        case marketReport: Option[MarketReport] => {
          val prevProgress = ((collectedReports - 1).max(0) * 100) / numOfMarkets
          marketReports += marketReport
          collectedReports += 1
          val newProgress = ((collectedReports - 1).max(0) * 100) / numOfMarkets
          if (newProgress > prevProgress) p(newProgress)
        }
      }
    }
    val nonEmptyReports = marketReports.filter(r => r.isDefined).map(_.get).toList
    val sortedReports = nonEmptyReports.sortWith((a, b) => a.marketTime.getTime < b.marketTime.getTime)
    p(100)
    SimulationReport(sortedReports)

  }

  /**
   * Registers new trader and return trader context.
   * This context can be used to trigger some custom traders that are registered manually by a master trader,
   * e.g. when testing some evolution algorithms for which more than one trader is required.
   * @return trader context
   */
  def registerTrader(market: IMarket, marketSimActor: MarketSimActor): ITraderContext = new TraderContext(nextBetId(), nextTraderUserId(), market, commission, bank, this, marketSimActor)
}