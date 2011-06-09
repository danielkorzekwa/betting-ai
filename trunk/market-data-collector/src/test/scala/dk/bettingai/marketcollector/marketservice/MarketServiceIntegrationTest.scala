package dk.bettingai.marketcollector.marketservice

import org.junit._
import Assert._
import dk.bot.betfairservice._
import java.util.Date
import dk.betex.api._
import IBet._
import BetTypeEnum._
import BetStatusEnum._

class MarketServiceIntegrationTest {

  require(System.getenv("bfUser") != null, "The bfUser property is not defined")
  require(System.getenv("bfPassword") != null, "The bfPassword property is not defined")

  /**Create betfair service and login to betfair account.*/
  val betfairServiceFactoryBean = new dk.bot.betfairservice.DefaultBetFairServiceFactoryBean();
  betfairServiceFactoryBean.setUser(System.getenv("bfUser"))
  betfairServiceFactoryBean.setPassword(System.getenv("bfPassword"))
  val productId = if (System.getenv("bfProductId") != null) System.getenv("bfProductId").toInt else 82
  betfairServiceFactoryBean.setProductId(productId)
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
  def getMarketsForMenuPathFilter {
    val dateFrom = new Date(System.currentTimeMillis)
    val dateTo = new Date(System.currentTimeMillis + (1000 * 3600 * 48))
    val allMarkets = marketService.getMarkets(dateFrom, dateTo)
    if (!allMarkets.isEmpty) {
      val menuPathFilter = marketService.getMarketDetails(allMarkets.head).menuPath
      val filteredMarkets = marketService.getMarkets(new Date(System.currentTimeMillis), new Date(System.currentTimeMillis + (1000 * 3600 * 48)), Option(menuPathFilter))

      /**Check if all filtered markets have the same menuPath.*/
      for (marketId <- filteredMarkets) {
        val menuPath = marketService.getMarketDetails(marketId).menuPath
        assertEquals(menuPathFilter, menuPath)
      }
    }
  }

  @Test
  def getMarkets_for_max_num_of_winners {
    val dateFrom = new Date(System.currentTimeMillis)
    val dateTo = new Date(System.currentTimeMillis + (1000 * 3600 * 48))
    val maxNumOfWinners = 1
    val allMarkets = marketService.getMarkets(dateFrom, dateTo)
    if (!allMarkets.isEmpty) {
      val menuPathFilter = marketService.getMarketDetails(allMarkets.head).menuPath
      val filteredMarkets = marketService.getMarkets(new Date(System.currentTimeMillis), new Date(System.currentTimeMillis + (1000 * 3600 * 48)), Option(menuPathFilter), Option(maxNumOfWinners))

      /**Check if all filtered markets are winner markets.*/
      for (marketId <- filteredMarkets) {
        val numOfWinners = marketService.getMarketDetails(marketId).numOfWinners
        assertEquals(1, numOfWinners)
      }
    }
  }

  @Test
  def getMatchedBets {
    val markets = marketService.getMarkets(new Date(System.currentTimeMillis), new Date(System.currentTimeMillis + (1000 * 3600 * 48)))
    if (!markets.isEmpty) {
      val matchedBets = marketService.getUserBets(markets.head, Option(M))
      matchedBets.foreach(b => assertTrue("Only matched bets should be returned here=" + b, b.betStatus == M))
    }
  }

  @Test
  def getMatchedUnmatchedBets {
    val markets = marketService.getMarkets(new Date(System.currentTimeMillis), new Date(System.currentTimeMillis + (1000 * 3600 * 48)))
    if (!markets.isEmpty) {
      val unmatchedBets = marketService.getUserBets(markets.head)
    }
  }

  @Test
  def getBet {
    val markets = marketService.getMarkets(new Date(System.currentTimeMillis), new Date(System.currentTimeMillis + (1000 * 3600 * 48)))
    if (!markets.isEmpty) {
      val unmatchedBets = marketService.getUserBets(markets.head)
      if (!unmatchedBets.isEmpty) {
        val bets = marketService.getBet(unmatchedBets.head.betId)
        assertTrue(bets.size > 0)
      }
    }
  }

  @Test
  def getMarketPrices {
    val markets = marketService.getMarkets(new Date(System.currentTimeMillis), new Date(System.currentTimeMillis + (1000 * 3600 * 48)))
    if (!markets.isEmpty) {
      val marketPrices = marketService.getMarketPrices(markets.head)
      assertTrue(marketPrices.runnerPrices.size > 2)
      marketPrices.runnerPrices.values.foreach(runnerPrices => assertTrue(runnerPrices.size > 0))
    }
  }

  @Test
  def getRunnerTradedVolume {
    val markets = marketService.getMarkets(new Date(System.currentTimeMillis), new Date(System.currentTimeMillis + (1000 * 3600 * 48)))
    if (!markets.isEmpty) {
      val tradedVolume = marketService.getMarketTradedVolume(markets.head)
      assertTrue(tradedVolume.size > 2)
      //tradedVolume.values.foreach(tv => assertTrue("No traded volume for runner", tv.pricesTradedVolume.size > 0))
    }
  }

  @Test
  def cancelBet_betNotFound {
    marketService.cancelBet(1234)
  }
}