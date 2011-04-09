package dk.bettingai.livetrader

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex._
import api._
import dk.bettingai.marketsimulator.betex.api.IBet._
import BetTypeEnum._
import BetStatusEnum._
import dk.bettingai.marketsimulator.betex.BetUtil._

class UserBetsTest {

  @Test
  def initialBets {
    val initialBets = Bet(100, 1000, 3, 2.2, BACK, U, 1, 11, None) :: Bet(100, 1000, 4, 2.4, BACK, M, 1, 11, Option(2500)) :: Bet(100, 1000, 3, 2.2, BACK, M, 1, 11, Option(2000)) :: Nil
    val userBets = UserBets(initialBets)

    assertEquals(2500, userBets.getLatestMatchedDate)
    assertBets(10, 1, 3, 2, 7, 2.314, userBets)
  }

  @Test
  def nothingPlacedYet {
    val userBets = UserBets(Nil)
    assertEquals(0, userBets.getLatestMatchedDate)
    assertBets(10, 0, 0, 0, 0, Double.NaN, userBets)
  }

  @Test
  def oneBetPlaced {
    val userBets = UserBets(Nil)
    userBets.betPlaced(new Bet(100, 1000, 10, 2.2, BACK, U, 1, 11, Some(1500)))

    assertEquals(0, userBets.getLatestMatchedDate)
    assertBets(10, 1, 10, 0, 0, Double.NaN, userBets)

  }

  @Test
  def oneBetPlacedandFullyMatched {

    val userBets = UserBets(Nil)
    userBets.betPlaced(Bet(100, 1000, 10, 2.2, BACK, U, 1, 11, None))
    userBets.betMatched(Bet(100, 1000, 10, 2.2, BACK, M, 1, 11, Option(2000)))

    assertEquals(2000, userBets.getLatestMatchedDate)
    assertBets(10, 0, 0, 1, 10, 2.2, userBets)
  }
  @Test
  def oneBetPlacedandFullyMatchedWithTwoPortions {

    val userBets = UserBets(Nil)
    userBets.betPlaced(Bet(100, 1000, 10, 2.2, BACK, U, 1, 11, None))
    userBets.betMatched(Bet(100, 1000, 4, 2.4, BACK, M, 1, 11, Option(2500)))
    userBets.betMatched(Bet(100, 1000, 6, 2.2, BACK, M, 1, 11, Option(2000)))

    assertEquals(2500, userBets.getLatestMatchedDate)
    assertBets(10, 0, 0, 2, 10, 2.28, userBets)
  }

  @Test
  def oneBetPlacedandPartiallyMatched {
    val userBets = UserBets(Nil)

    userBets.betPlaced(Bet(100, 1000, 10, 2.2, BACK, U, 1, 11, None))
    userBets.betMatched(Bet(100, 1000, 6, 2.2, BACK, M, 1, 11, Option(2000)))
    assertEquals(2000, userBets.getLatestMatchedDate)
    assertBets(10, 1, 4, 1, 6, 2.2, userBets)
  }
  @Test
  def oneBetPlacedandPartiallyMatchedWithTwoPortions {

    val userBets = UserBets(Nil)
    userBets.betPlaced(Bet(100, 1000, 10, 2.2, BACK, U, 1, 11, None))
    userBets.betMatched(Bet(100, 1000, 4, 2.4, BACK, M, 1, 11, Option(2500)))
    userBets.betMatched(Bet(100, 1000, 3, 2.2, BACK, M, 1, 11, Option(2000)))

    assertEquals(2500, userBets.getLatestMatchedDate)
    assertBets(10, 1, 3, 2, 7, 2.314, userBets)
  }

  private def assertBets(marketId: Long, unmatchedBetsNum: Int, unmatchedBetsSize: Double, matchedBetsNum: Int, matchedBetsSize: Double, matchedBetsAvgPrice: Double, userBets: UserBets) {

    assertEquals(unmatchedBetsNum, userBets.getUserBets(marketId, Option(U)).size)
    assertEquals(unmatchedBetsSize, totalStake(userBets.getUserBets(marketId, Option(U))), 0)

    assertEquals(matchedBetsNum, userBets.getUserBets(marketId, Option(M)).size)
    assertEquals(matchedBetsSize, totalStake(userBets.getUserBets(marketId, Option(M))), 0)
    assertEquals(matchedBetsAvgPrice, avgPrice(userBets.getUserBets(marketId, Option(M))), 0.001)
  }
}