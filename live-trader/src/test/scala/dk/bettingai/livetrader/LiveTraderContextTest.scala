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
import java.io.File

@RunWith(value = classOf[JMock])
class LiveTraderContextTest {

  private val marketId = 1l
  private val numOfWinners = 3
  private val interval = 10
  private val marketTime = new Date(2000)
  private val marketRunners: List[RunnerDetails] = new RunnerDetails(11, "Max") :: new RunnerDetails(12, "Jordan") :: Nil
  private val marketDetails = new MarketDetails(marketId, "Soccer Jacpot", "Plump 28th Mar - 14:10 2m Mdn Hrd", numOfWinners, marketTime, marketRunners)

  private val commission = 0.05

  private val mockery = new Mockery()
  private val marketService = mockery.mock(classOf[IMarketService])

  mockery.checking(new SExpectations() {
    {
      one(marketService).getUserBets(marketId, None); will(returnValue(Nil))
    }
  })
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
        one(marketService).placeBet(existingBet.betSize, existingBet.betPrice, existingBet.betType, existingBet.marketId, existingBet.runnerId); will(returnValue(existingBet))
        one(marketService).placeBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.marketId, betToBePlaced.runnerId); will(returnValue(betToBePlaced))
      }
    })

    liveCtx.fillBet(existingBet.betSize, existingBet.betPrice, existingBet.betType, existingBet.runnerId)
    val placedBet = liveCtx.fillBet(10, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.runnerId)

    assertEquals(betToBePlaced, placedBet.get)
  }

  @Test
  def fillBetNothingPlaced {
    val betToBePlaced = Bet(100, 1000, 10, 2.2, BACK, 1, 11)

    mockery.checking(new SExpectations() {
      {
        one(marketService).placeBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.marketId, betToBePlaced.runnerId); will(returnValue(betToBePlaced))
      }
    })

    liveCtx.fillBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.runnerId)
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
        one(marketService).getMarketPrices(1); will(returnValue(MarketPrices(0, marketPrices)))
      }
    })

    val bestPrices = liveCtx.getBestPrices()
    assertEquals(2, bestPrices.size)
    assertEquals(Tuple2(RunnerPrice(2.3, 200, 0), RunnerPrice(2.4, 0, 300)), bestPrices(11))
    assertEquals(Tuple2(RunnerPrice(1.3, 20, 0), RunnerPrice(1.4, 0, 30)), bestPrices(12))
  }

  @Test(expected = classOf[IllegalStateException])
  def getBestPricesMarketIsInplay {
    val marketPrices = Map(
      11l -> (new RunnerPrice(2.2, 100, 0) :: new RunnerPrice(2.3, 200, 0) :: new RunnerPrice(2.4, 0, 300) :: new RunnerPrice(2.5, 0, 400) :: Nil),
      12l -> (new RunnerPrice(1.2, 10, 0) :: new RunnerPrice(1.3, 20, 0) :: new RunnerPrice(1.4, 0, 30) :: new RunnerPrice(1.5, 0, 40) :: Nil))

    mockery.checking(new SExpectations() {
      {
        one(marketService).getMarketPrices(1); will(returnValue(MarketPrices(1, marketPrices)))
      }
    })

    val bestPrices = liveCtx.getBestPrices()
  }

  @Test
  def getBestPricesForRunner {
    val marketPrices = Map(
      11l -> (new RunnerPrice(2.2, 100, 0) :: new RunnerPrice(2.3, 200, 0) :: new RunnerPrice(2.4, 0, 300) :: new RunnerPrice(2.5, 0, 400) :: Nil),
      12l -> (new RunnerPrice(1.2, 10, 0) :: new RunnerPrice(1.3, 20, 0) :: new RunnerPrice(1.4, 0, 30) :: new RunnerPrice(1.5, 0, 40) :: Nil))

    mockery.checking(new SExpectations() {
      {
        /**Only one call to this method is expected because market prices are cached.*/
        exactly(1).of(marketService).getMarketPrices(1); will(returnValue(MarketPrices(0, marketPrices)))
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

    val bet1 = new Bet(100, 1000, 10, 2.2, BACK, M, 1, 11, None)
    val bet2 = new Bet(100, 1000, 10, 2.4, LAY, M, 1, 11, None)

    mockery.checking(new SExpectations() {
      {
        one(marketService).placeBet(bet1.betSize, bet1.betPrice, bet1.betType, bet1.marketId, bet1.runnerId); will(returnValue(bet1))
        one(marketService).placeBet(bet2.betSize, bet2.betPrice, bet2.betType, bet2.marketId, bet2.runnerId); will(returnValue(bet2))
      }
    })

    liveCtx.placeBet(bet1.betSize, bet1.betPrice, bet1.betType, bet1.runnerId)
    liveCtx.placeBet(bet2.betSize, bet2.betPrice, bet2.betType, bet2.runnerId)

    mockery.checking(new SExpectations() {
      {
        one(marketService).getMarketPrices(1); will(returnValue(MarketPrices(0, marketPrices)))
      }
    })

    val risk = liveCtx.risk(100)
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

    /**Call again (should be already cached, so no extra call to market service should be required.*/
    liveCtx.getTotalTradedVolume(11)
  }

  @Test
  def getSetEventTimestamp {
    mockery.checking(new SExpectations() {
      {
        one(marketService).getUserMatchedBets(withArg(marketId), withArg(Matchers.any(classOf[Date]))); will(returnValue(Nil))
      }
    })

    liveCtx.setEventTimestamp(1000)
    assertEquals(1000, liveCtx.getEventTimestamp)
  }

  @Test
  def saveChart {
    mockery.checking(new SExpectations() {
      {
        exactly(3).of(marketService).getUserMatchedBets(withArg(marketId), withArg(Matchers.any(classOf[Date]))); will(returnValue(Nil))
      }
    })

    liveCtx.setEventTimestamp(1000)
    liveCtx.addChartValue("a", 1)
    liveCtx.addChartValue("b", 1)

    liveCtx.setEventTimestamp(2000)
    liveCtx.addChartValue("a", 2)
    liveCtx.addChartValue("b", 3)

    liveCtx.setEventTimestamp(5000)
    liveCtx.addChartValue("a", 3)
    liveCtx.addChartValue("b", 5)

    val chartFileName = "./target/" + getClass.getSimpleName + ".html"
    liveCtx.saveChart(chartFileName)
    val chartFile = new File(chartFileName)
    assertTrue("Chart file doesn't exist", chartFile.exists)
    assertTrue("Chart file is empty", chartFile.length > 0)

  }

  @Test
  def getBets {
    val bet1 = new Bet(100, 1000, 10, 2.2, BACK, U, 1, 11, None)
    val bet2 = new Bet(100, 1000, 10, 2.4, LAY, U, 1, 11, None)

    mockery.checking(new SExpectations() {
      {
        one(marketService).placeBet(bet1.betSize, bet1.betPrice, bet1.betType, bet1.marketId, bet1.runnerId); will(returnValue(bet1))
        one(marketService).placeBet(bet2.betSize, bet2.betPrice, bet2.betType, bet2.marketId, bet2.runnerId); will(returnValue(bet2))
      }
    })

    liveCtx.placeBet(bet1.betSize, bet1.betPrice, bet1.betType, bet1.runnerId)
    liveCtx.placeBet(bet2.betSize, bet2.betPrice, bet2.betType, bet2.runnerId)

    val bets = liveCtx.getBets(false)

    assertEquals(bet1 :: bet2 :: Nil, bets)
  }

  @Test
  def getBetsMatchedOnly {
    /**Place bets.*/
    val bet1 = Bet(100, 1000, 10, 2.2, BACK, U, 1, 11, None)
    val bet2 = Bet(101, 1000, 10, 2.4, LAY, U, 1, 11, None)

    mockery.checking(new SExpectations() {
      {
        one(marketService).placeBet(bet1.betSize, bet1.betPrice, bet1.betType, bet1.marketId, bet1.runnerId); will(returnValue(bet1))
        one(marketService).placeBet(bet2.betSize, bet2.betPrice, bet2.betType, bet2.marketId, bet2.runnerId); will(returnValue(bet2))
      }
    })

    liveCtx.placeBet(bet1.betSize, bet1.betPrice, bet1.betType, bet1.runnerId)
    liveCtx.placeBet(bet2.betSize, bet2.betPrice, bet2.betType, bet2.runnerId)

    /**Bet is matched.*/
    val matchedBet = new Bet(101, 1000, 4, 2.4, LAY, M, 1, 11, None)
    mockery.checking(new SExpectations() {
      {
        one(marketService).getUserMatchedBets(withArg(marketId), withArg(Matchers.any(classOf[Date]))); will(returnValue(matchedBet :: Nil))
      }
    })

    liveCtx.setEventTimestamp(1000)

    /**Check matched bets.*/
    val matchedBets = liveCtx.getBets(true)
    assertEquals(matchedBet :: Nil, matchedBets)

    /**Check all bets.*/
    val allBets = liveCtx.getBets(false)
    assertEquals(Bet(100, 1000, 10, 2.2, BACK, U, 1, 11, None) :: Bet(101, 1000, 6, 2.4, LAY, U, 1, 11, None) :: matchedBet :: Nil, allBets)
  }

  /**Tests for getBet*/
  @Test
  def getBet {
    val bet = Bet(1, 200, 3, 2.1, BACK, U, 1, 11, None) :: Bet(1, 200, 7, 2.1, BACK, M, 1, 11, None) :: Nil
    mockery.checking(new SExpectations() {
      {
        one(marketService).getBet(100); will(returnValue(bet))
      }
    })
    assertEquals(bet, liveCtx.getBet(100))
  }

  @Test
  def cancelBet {
    mockery.checking(new SExpectations() {
      {
        one(marketService).cancelBet(1234);
      }
    })

    liveCtx.cancelBet(1234)
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