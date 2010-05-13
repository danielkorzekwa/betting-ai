package dk.bettingai.marketsimulator.betex

import org.junit._
import Assert._
import java.util.Date
import Bet.BetTypeEnum._
import Bet.BetStatusEnum._

class BetexImplTest {

	private val betex = new BetexImpl()

	/** 
	 *  Tests for createMarket.
	 * 
	 * */

	@Test def testCreateMarket {
		val market = new Market(10,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		betex.createMarket(market)
		assertEquals(1,betex.getActiveMarkets.size)

		val marketFromBetex = betex.getActiveMarkets()(0)
		assertEquals(10,marketFromBetex.marketId)
		assertEquals("Match Odds",marketFromBetex.marketName)
		assertEquals("Man Utd vs Arsenal",marketFromBetex.eventName)
		assertEquals(1,marketFromBetex.numOfWinners)
		assertEquals(new Date(2000),marketFromBetex.marketTime)

		assertEquals(11,marketFromBetex.selections(0).selectionId)
		assertEquals("Man Utd",marketFromBetex.selections(0).selectionName)
		assertEquals(12,marketFromBetex.selections(1).selectionId)
		assertEquals("Arsenal",marketFromBetex.selections(1).selectionName)
	}

	@Test def testCreateTwoMarkets {
		val market1 = new Market(10,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		val market2 = new Market(20,"Match Odds","Fulham vs Wigan",1,new Date(2000),List(new Market.Selection(31,"Fulham"),new Market.Selection(42,"Wigan")))

		betex.createMarket(market1)
		betex.createMarket(market2)
		assertEquals(2,betex.getActiveMarkets.size)

		val marketFromBetex1 = betex.getActiveMarkets()(0)
		assertEquals(20,marketFromBetex1.marketId)
		assertEquals("Match Odds",marketFromBetex1.marketName)
		assertEquals("Fulham vs Wigan",marketFromBetex1.eventName)
		assertEquals(1,marketFromBetex1.numOfWinners)
		assertEquals(new Date(2000),marketFromBetex1.marketTime)

		assertEquals(31,marketFromBetex1.selections(0).selectionId)
		assertEquals("Fulham",marketFromBetex1.selections(0).selectionName)
		assertEquals(42,marketFromBetex1.selections(1).selectionId)
		assertEquals("Wigan",marketFromBetex1.selections(1).selectionName)

		val marketFromBetex2 = betex.getActiveMarkets()(1)
		assertEquals(10,marketFromBetex2.marketId)
		assertEquals("Match Odds",marketFromBetex2.marketName)
		assertEquals("Man Utd vs Arsenal",marketFromBetex2.eventName)
		assertEquals(1,marketFromBetex2.numOfWinners)
		assertEquals(new Date(2000),marketFromBetex2.marketTime)

		assertEquals(11,marketFromBetex2.selections(0).selectionId)
		assertEquals("Man Utd",marketFromBetex2.selections(0).selectionName)
		assertEquals(12,marketFromBetex2.selections(1).selectionId)
		assertEquals("Arsenal",marketFromBetex2.selections(1).selectionName)
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testCreateMarketAlreadyExist {
		val market1 = new Market(10,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		val market2 = new Market(10,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))

		betex.createMarket(market1)
		betex.createMarket(market2)
	}

	/** 
	 *  Tests for getBets.
	 * 
	 * */

	@Test def testGetBetsForNotExistingUser {
		val market1 = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		betex.createMarket(market1)

		betex.placeBet(100,123,2,1.5,BACK,1,11)

		assertEquals(0, betex.getBets(1234).size)
	}

	@Test def testGetBetsNoBets {
		assertEquals(0, betex.getBets(123).size)
	}

	/** 
	 *  Tests for placeBet and matching.
	 * 
	 * */

	@Test(expected=classOf[IllegalArgumentException]) 
	def testPlaceBetBetSizeLessThanMin {
		betex.placeBet(100,123,1.9,1.01,BACK,1,11)
	}
	
	@Test(expected=classOf[IllegalArgumentException]) 
	def testPlaceBetMarketNotFound {
		betex.placeBet(100,123,2,1.01,BACK,1,11)
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testPlaceBetMarketSelectionNotFound {
		val market1 = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		betex.createMarket(market1)	

		betex.placeBet(100,123,2,1.01,BACK,1,13)
	}

	@Test def testPlaceBackBet {
		val market1 = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		betex.createMarket(market1)

		betex.placeBet(100,123,2,1.5,BACK,1,11)

		val bets = betex.getBets(123)
		assertEquals(1, bets.size)
		assertEquals(100,bets(0).betId)
		assertEquals(123,bets(0).userId)
		assertEquals(2,bets(0).betSize,0)
		assertEquals(1.5,bets(0).betPrice,0)
		assertEquals(BACK,bets(0).betType)
		assertEquals(U,bets(0).betStatus)
		assertEquals(1,bets(0).marketId)
		assertEquals(11,bets(0).selectionId)
	}

	@Test def testPlaceLayBet {
		val market1 = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		betex.createMarket(market1)

		betex.placeBet(100,123,2,1.5,LAY,1,11)

		val bets = betex.getBets(123)
		assertEquals(1, bets.size)
		assertEquals(100,bets(0).betId)
		assertEquals(123,bets(0).userId)
		assertEquals(2,bets(0).betSize,0)
		assertEquals(1.5,bets(0).betPrice,0)
		assertEquals(LAY,bets(0).betType)	
		assertEquals(U,bets(0).betStatus)
		assertEquals(1,bets(0).marketId)
		assertEquals(11,bets(0).selectionId)
	}

	@Test def testPlaceAFewBets {
		val market1 = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		betex.createMarket(market1)

		betex.placeBet(100,122,13,2.1,LAY,1,11)
		betex.placeBet(101,121,3,2.2,LAY,1,11)
		betex.placeBet(102,122,5,2.2,LAY,1,11)
		betex.placeBet(103,121,8,2.4,BACK,1,11)
		betex.placeBet(104,122,25,2.5,BACK,1,11)

		val bets = betex.getBets(121)
		assertEquals(2, bets.size)

		assertEquals(101,bets(0).betId)
		assertEquals(121,bets(0).userId)
		assertEquals(3,bets(0).betSize,0)
		assertEquals(2.2,bets(0).betPrice,0)
		assertEquals(LAY,bets(0).betType)
		assertEquals(U,bets(0).betStatus)
		assertEquals(1,bets(0).marketId)
		assertEquals(11,bets(0).selectionId)

		assertEquals(103,bets(1).betId)
		assertEquals(121,bets(0).userId)
		assertEquals(8,bets(1).betSize,0)
		assertEquals(2.4,bets(1).betPrice,0)
		assertEquals(BACK,bets(1).betType)
		assertEquals(U,bets(1).betStatus)
		assertEquals(1,bets(1).marketId)
		assertEquals(11,bets(1).selectionId)
	}

	@Test def testMatchingPlaceBackThenLay {
		val market1 = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		betex.createMarket(market1)

		betex.placeBet(100,122,2,5,BACK,1,11)
		betex.placeBet(101,122,2,4,BACK,1,11)
		betex.placeBet(102,123,2,7,LAY,1,11)

		/**Check bets for user 122.*/
		val bets122 = betex.getBets(122)
		assertEquals(2, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(2,bets122(0).betSize,0)
		assertEquals(5,bets122(0).betPrice,0)
		assertEquals(BACK,bets122(0).betType)
		assertEquals(U,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).selectionId)

		assertEquals(101,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(2,bets122(1).betSize,0)
		assertEquals(4,bets122(1).betPrice,0)
		assertEquals(BACK,bets122(1).betType)
		assertEquals(M,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).selectionId)

		/**Check bets for user 123.*/
		val bets123 = betex.getBets(123)
		assertEquals(1, bets123.size)

		assertEquals(102,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(2,bets123(0).betSize,0)
		assertEquals(4,bets123(0).betPrice,0)
		assertEquals(LAY,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).selectionId)
	}
	
	@Test def testMatchingPlaceLayThenBack {
		val market1 = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		betex.createMarket(market1)

		betex.placeBet(100,122,2,4,LAY,1,11)
		betex.placeBet(101,122,2,5,LAY,1,11)
		betex.placeBet(102,123,2,3,BACK,1,11)

		/**Check bets for user 122.*/
		val bets122 = betex.getBets(122)
		assertEquals(2, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(2,bets122(0).betSize,0)
		assertEquals(4,bets122(0).betPrice,0)
		assertEquals(LAY,bets122(0).betType)
		assertEquals(U,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).selectionId)

		assertEquals(101,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(2,bets122(1).betSize,0)
		assertEquals(5,bets122(1).betPrice,0)
		assertEquals(LAY,bets122(1).betType)
		assertEquals(M,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).selectionId)

		/**Check bets for user 123.*/
		val bets123 = betex.getBets(123)
		assertEquals(1, bets123.size)

		assertEquals(102,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(2,bets123(0).betSize,0)
		assertEquals(5,bets123(0).betPrice,0)
		assertEquals(BACK,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).selectionId)
	}

}
