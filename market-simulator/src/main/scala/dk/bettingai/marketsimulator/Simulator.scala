package dk.bettingai.marketsimulator

import ISimulator._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.marketevent._
import dk.bettingai.marketsimulator.trader._
import ITrader._
import IMarket._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import dk.bettingai.marketsimulator.risk._
import scala.io._
import java.io.File
import java.io.BufferedReader
import java.io.FileReader
import scala.annotation._
import scala.collection._
import immutable.TreeMap
import scala.actors.Actor
import Actor._
import java.util.concurrent.atomic._
import MarketSimActor._

/**This trait represents a simulator that processes market events, analyses trader implementation and returns analysis report for trader implementation.
 * 
 * @author korzekwad
 *
 * 
 * @param MarketEventProcessor
 * @param betex
 * @param commission Commission on winnings in percentage.
 *
 */
class Simulator(betex: IBetex, commission: Double) extends ISimulator {

  var nextBetIdValue = new AtomicLong(1)
  def nextBetId() = nextBetIdValue.addAndGet(1)

  var lastTraderUserId = new AtomicInteger(1)
  def nextTraderUserId() = lastTraderUserId.addAndGet(1)
  val historicalDataUserId = nextTraderUserId()

  /** Processes market events, analyses traders and returns analysis reports.
   * 
   * @param marketDataContains market events that the market simulation is executed for. Key - marketId, value - market events
   * @param traders Traders to analyse, all are analysed on the same time, so they compete against each other
   * @param p Progress listener. Value between 0% and 100% is passed as an function argument.
   */
  def runSimulation(marketData: TreeMap[Long, File], traders: List[ITrader], p: (Int) => Unit): SimulationReport = {

    /**Register traders on a betting exchange by assigning user ids for them.*/
    val registeredTraders = traders.map(trader => RegisteredTrader(nextTraderUserId(), trader))

    val numOfMarkets = marketData.size

    p(0)

    def createTraderContext(userId:Int, market:IMarket):TraderContext = new TraderContext(nextBetId(), userId, market, commission, this)
    
    /**Process all markets in parallel and send back market reports.*/
    for ((marketId, marketFile) <- marketData) {

      val slave = new MarketSimActor(betex, nextBetId, historicalDataUserId, commission, createTraderContext).start
      slave ! MarketSimRequest(marketId, marketFile, registeredTraders)
    }

    /**Collect market reports from slaves.*/
    val marketReports = mutable.ListBuffer[Option[MarketReport]]()
    while (marketReports.size != numOfMarkets) {
      receive {
        case marketReport: Option[MarketReport] => {
          val prevProgress = ((marketReports.size - 1).max(0) * 100) / numOfMarkets
          marketReports += marketReport
          val newProgress = ((marketReports.size - 1).max(0) * 100) / numOfMarkets
          if (newProgress > prevProgress) p(newProgress)
        }
      }
    }
    val nonEmptyReports = marketReports.filter(r => r.isDefined).map(_.get).toList
    val sortedReports = nonEmptyReports.sortWith((a, b) => a.marketTime.getTime < b.marketTime.getTime)
    p(100)
    SimulationReport(sortedReports)

  }

  /**Registers new trader and return trader context. 
   * This context can be used to trigger some custom traders that are registered manually by a master trader, 
   * e.g. when testing some evolution algorithms for which more than one trader is required.
   * @return trader context
   */
  def registerTrader(market: IMarket): ITraderContext = new TraderContext(nextBetId(), nextTraderUserId(), market, commission, this)
}