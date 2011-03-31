package dk.bettingai.livetrader

import org.junit.Test
import org.junit.Assert._
import java.util.Date
import dk.bettingai.marketcollector.marketservice._
import IMarketService._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.api._
import Market._
import dk.bettingai.marketsimulator.betex.api.IBet._
import BetTypeEnum._
import BetStatusEnum._
import org.junit.runner._
import org.jmock.integration.junit4._
import org.jmock._
import org.hamcrest._
import org.jmock.Expectations._
import dk.bettingai.marketsimulator.betex._
import RunnerTradedVolume._

@RunWith(value = classOf[JMock])
class LiveTraderContextTest {

  private val marketId = 1
  private val numOfWinners = 3
  private val interval = 10
  private val marketTime = new Date(2000)
  private val marketRunners: List[RunnerDetails] = new RunnerDetails(11, "Max") :: new RunnerDetails(12, "Jordan") :: Nil
  private val marketDetails = new MarketDetails(marketId, "Soccer Jacpot", "Plump 28th Mar - 14:10 2m Mdn Hrd", numOfWinners, marketTime, marketRunners)

  private val commission = 0.05

  private val mockery = new Mockery()
  private val marketService = mockery.mock(classOf[IMarketService])

  private val liveCtx = LiveTraderContext(marketDetails, marketService, commission, null)

  @Test
  def marketDetailsAreCorrect {
    assertEquals(1, liveCtx.marketId)
    assertEquals("Soccer Jacpot", liveCtx.marketName)
    assertEquals("Plump 28th Mar - 14:10 2m Mdn Hrd", liveCtx.eventName)
    assertEquals(3, liveCtx.numOfWinners)
    assertEquals(new Date(2000), liveCtx.marketTime)
  }

  @Test
  def runners {
    val runners = liveCtx.runners;
    assertEquals(2, runners.size)
    assertEquals(Market.Runner(11, "Max"), runners(0))
    assertEquals(Market.Runner(12, "Jordan"), runners(1))
  }

  @Test
  def placeBet {
    val bet = Bet(100, 1000, 10, 2.2, BACK, 1, 11)
    mockery.checking(new SExpectations() {
      {
        one(marketService).placeBet(bet.betSize, bet.betPrice, bet.betType, bet.marketId, bet.runnerId); will(returnValue(bet))
      }
    })

    val placedBet = liveCtx.placeBet(bet.betSize, bet.betPrice, bet.betType, bet.runnerId)
    assertEquals(bet, placedBet)
  }

  @Test
  def fillBetFullyPlaced {
    val betToBePlaced = Bet(100, 1000, 10, 2.2, BACK, 1, 11)
    mockery.checking(new SExpectations() {
      {
        one(marketService).getUserBets(marketId, Option(U)); will(returnValue(Nil))
        one(marketService).placeBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.marketId, betToBePlaced.runnerId); will(returnValue(betToBePlaced))
      }
    })

    val placedBet = liveCtx.fillBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.runnerId)

    assertEquals(betToBePlaced, placedBet.get)
  }

  @Test
  def fillBetFullyPlacedOtherBetsAreAvailable {
    val existingBets =
      Bet(100, 1000, 10, 2.2, LAY, 1, 11) ::
        Bet(100, 1000, 10, 2.2, BACK, 1, 12) ::
        Bet(100, 1000, 10, 2.21, BACK, 1, 11) :: Nil
    val betToBePlaced = Bet(100, 1000, 10, 2.2, BACK, 1, 11)

    mockery.checking(new SExpectations() {
      {
        one(marketService).getUserBets(marketId, Option(U)); will(returnValue(existingBets))
        one(marketService).placeBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.marketId, betToBePlaced.runnerId); will(returnValue(betToBePlaced))
      }
    })

    val placedBet = liveCtx.fillBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.runnerId)

    assertEquals(betToBePlaced, placedBet.get)
  }

  @Test
  def fillBetPartiallyPlaced {
    val existingBet = Bet(100, 1000, 6, 2.2, BACK, 1, 11)
    val betToBePlaced = Bet(100, 1000, 4, 2.2, BACK, 1, 11)
    mockery.checking(new SExpectations() {
      {
        one(marketService).getUserBets(marketId, Option(U)); will(returnValue(existingBet :: Nil))
        one(marketService).placeBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.marketId, betToBePlaced.runnerId); will(returnValue(betToBePlaced))
      }
    })

    val placedBet = liveCtx.fillBet(10, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.runnerId)

    assertEquals(betToBePlaced, placedBet.get)
  }

  @Test
  def fillBetNothingPlaced {
    val betToBePlaced = Bet(100, 1000, 10, 2.2, BACK, 1, 11)
    mockery.checking(new SExpectations() {
      {
        one(marketService).getUserBets(marketId, Option(U)); will(returnValue(betToBePlaced :: Nil))
      }
    })

    val placedBet = liveCtx.fillBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.runnerId)

    assertTrue(placedBet.isEmpty)
  }

  @Test
  def getBestPrices {
    val marketPrices = Map(
      11l -> (new RunnerPrice(2.2, 100, 0) :: new RunnerPrice(2.3, 200, 0) :: new RunnerPrice(2.4, 0, 300) :: new RunnerPrice(2.5, 0, 400) :: Nil),
      12l -> (new RunnerPrice(1.2, 10, 0) :: new RunnerPrice(1.3, 20, 0) :: new RunnerPrice(1.4, 0, 30) :: new RunnerPrice(1.5, 0, 40) :: Nil))

    mockery.checking(new SExpectations() {
      {
        one(marketService).getMarketPrices(1); will(returnValue(marketPrices))
      }
    })

    val bestPrices = liveCtx.getBestPrices()
    assertEquals(2, bestPrices.size)
    assertEquals(Tuple2(RunnerPrice(2.3, 200, 0), RunnerPrice(2.4, 0, 300)), bestPrices(11))
    assertEquals(Tuple2(RunnerPrice(1.3, 20, 0), RunnerPrice(1.4, 0, 30)), bestPrices(12))
  }

  @Test
  def getBestPricesForRunner {
    val marketPrices = Map(
      11l -> (new RunnerPrice(2.2, 100, 0) :: new RunnerPrice(2.3, 200, 0) :: new RunnerPrice(2.4, 0, 300) :: new RunnerPrice(2.5, 0, 400) :: Nil),
      12l -> (new RunnerPrice(1.2, 10, 0) :: new RunnerPrice(1.3, 20, 0) :: new RunnerPrice(1.4, 0, 30) :: new RunnerPrice(1.5, 0, 40) :: Nil))

    mockery.checking(new SExpectations() {
      {
        exactly(2).of(marketService).getMarketPrices(1); will(returnValue(marketPrices))
      }
    })

    val bestPrices11 = liveCtx.getBestPrices(11)
    val bestPrices12 = liveCtx.getBestPrices(12)
    assertEquals(Tuple2(RunnerPrice(2.3, 200, 0), RunnerPrice(2.4, 0, 300)), bestPrices11)
    assertEquals(Tuple2(RunnerPrice(1.3, 20, 0), RunnerPrice(1.4, 0, 30)), bestPrices12)
  }

  @Test
  def risk {
    val marketPrices = Map(
      11l -> (new RunnerPrice(2.2, 100, 0) :: new RunnerPrice(2.3, 200, 0) :: new RunnerPrice(2.4, 0, 300) :: new RunnerPrice(2.5, 0, 400) :: Nil),
      12l -> (new RunnerPrice(1.2, 10, 0) :: new RunnerPrice(1.3, 20, 0) :: new RunnerPrice(1.4, 0, 30) :: new RunnerPrice(1.5, 0, 40) :: Nil))

    val bets = new Bet(100, 1000, 10, 2.2, BACK, M, 1, 11) :: new Bet(100, 1000, 10, 2.4, LAY, M, 1, 11) :: Nil

    mockery.checking(new SExpectations() {
      {
        one(marketService).getUserBets(marketId, Option(M)); will(returnValue(bets))
        one(marketService).getMarketPrices(1); will(returnValue(marketPrices))
      }
    })

    val risk = liveCtx.risk
    assertEquals(-0.768, risk.marketExpectedProfit, 0.001)
  }

  @Test
  def getTotalTraderVolumeForRunner {
    val marketTradedVolume = Map(11 -> new RunnerTradedVolume(new PriceTradedVolume(2.2, 50) :: new PriceTradedVolume(3.45, 25) :: Nil), 12 -> new RunnerTradedVolume(Nil))

    mockery.checking(new SExpectations() {
      {
        one(marketService).getMarketTradedVolume(1); will(returnValue(marketTradedVolume))
      }
    })

    val totalTradedVolume = liveCtx.getTotalTradedVolume(11)
    assertEquals(75, totalTradedVolume, 0)
  }

  @Test
  def getSetEventTimestamp {
    liveCtx.setEventTimestamp(1000)
    assertEquals(1000, liveCtx.getEventTimestamp)
  }
  /**The 'with' method from jmock can't be used in Scala, therefore it's changed to 'withArg' method*/
  private class SExpectations extends Expectations {
    def withArg[T](matcher: Matcher[T]): T = super.`with`(matcher)
    def withArg(value: Long): Long = super.`with`(value)
    def withArg(value: String): String = super.`with`(value)
    def withArg(value: Int): Int = super.`with`(value)
    def withArg[Any](matcher: Any): Any = super.`with`(matcher)
  }

}