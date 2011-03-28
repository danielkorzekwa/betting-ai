package dk.bettingai.marketcollector.marketservice

import org.junit._
import Assert._
import dk.bot.betfairservice._
import java.util.Date
import dk.bettingai.marketsimulator.betex.api._
import IBet._
import BetTypeEnum._
import BetStatusEnum._

class MarketServiceIntegrationTest {
  require(System.getProperty("bfUser") != null, "The bfUser property is not defined")
  require(System.getProperty("bfPassword") != null, "The bfPassword property is not defined")
  require(System.getProperty("bfProductId") != null, "The bfProductId property is not defined")

  /**Create betfair service and login to betfair account.*/
  val betfairServiceFactoryBean = new dk.bot.betfairservice.DefaultBetFairServiceFactoryBean();
  betfairServiceFactoryBean.setUser(System.getProperty("bfUser"))
  betfairServiceFactoryBean.setPassword(System.getProperty("bfPassword"))
  betfairServiceFactoryBean.setProductId(System.getProperty("bfProductId").toInt)
  val loginResponse = betfairServiceFactoryBean.login
  val betfairService: BetFairService = (betfairServiceFactoryBean.getObject.asInstanceOf[BetFairService])

  private val marketService = new MarketService(betfairService)

  @Test
  def getUnmatchedBets {
    val markets = marketService.getMarkets(new Date(System.currentTimeMillis), new Date(System.currentTimeMillis + (1000 * 3600 * 48)))
    if (!markets.isEmpty) {
      val unmatchedBets = marketService.getUserBets(markets.head, Option(U))
      unmatchedBets.foreach(b => assertTrue("Only unmatched bets should be returned here=" + b, b.betStatus == U))
    }
  }

  @Test
  def getMatchedBets {
    val markets = marketService.getMarkets(new Date(System.currentTimeMillis), new Date(System.currentTimeMillis + (1000 * 3600 * 48)))
    if (!markets.isEmpty) {
      val unmatchedBets = marketService.getUserBets(markets.head, Option(M))
      unmatchedBets.foreach(b => assertTrue("Only matched bets should be returned here=" + b, b.betStatus == M))
    }
  }

  @Test
  def getMatchedUnmatchedBets {
    val markets = marketService.getMarkets(new Date(System.currentTimeMillis), new Date(System.currentTimeMillis + (1000 * 3600 * 48)))
    if (!markets.isEmpty) {
      val unmatchedBets = marketService.getUserBets(markets.head)
    }
  }
}