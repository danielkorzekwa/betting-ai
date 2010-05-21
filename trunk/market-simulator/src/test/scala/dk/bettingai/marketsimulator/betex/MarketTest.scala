package dk.bettingai.marketsimulator.betex

import org.junit._
import Assert._
import java.util.Date
import dk.bettingai.marketsimulator.betex.api._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._

class MarketTest {

	/**
	 * Tests for market creation. 
	 */

	@Test
	def testCreateMarket {
		new Market(10,"Match Odds","Man Utd vs Arsenal",2,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testCreateMarketWrongNumOfWinners {
		new Market(10,"Match Odds","Man Utd vs Arsenal",0,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testCreateMarketWrongNumOfRunners {
		new Market(10,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd")))
	}


	/** 
	 *  Tests for getBets.
	 * 
	 * */

	@Test def testGetBetsForNotExistingUser {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,123,2,1.5,BACK,11)
		assertEquals(0, market.getBets(1234).size)
	}

	@Test def testGetBetsNoBets {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		assertEquals(0, market.getBets(123).size)
	}

	/** 
	 *  Tests for placeBet and matching.
	 * 
	 * */

	@Test(expected=classOf[IllegalArgumentException]) 
	def testPlaceBetBetSizeLessThanMin {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,1.9,1.01,BACK,11)
	}


	@Test(expected=classOf[IllegalArgumentException]) 
	def testPlaceBetMarketRunnerNotFound {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))
		market.placeBet(100,123,2,1.01,BACK,13)
	}

	@Test def testPlaceBackBet {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))
		market.placeBet(100,123,2,1.5,BACK,11)

		val bets = market.getBets(123)
		assertEquals(1, bets.size)
		assertEquals(100,bets(0).betId)
		assertEquals(123,bets(0).userId)
		assertEquals(2,bets(0).betSize,0)
		assertEquals(1.5,bets(0).betPrice,0)
		assertEquals(BACK,bets(0).betType)
		assertEquals(U,bets(0).betStatus)
		assertEquals(1,bets(0).marketId)
		assertEquals(11,bets(0).runnerId)
	}

	@Test def testPlaceLayBet {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,123,2,1.5,LAY,11)

		val bets = market.getBets(123)
		assertEquals(1, bets.size)
		assertEquals(100,bets(0).betId)
		assertEquals(123,bets(0).userId)
		assertEquals(2,bets(0).betSize,0)
		assertEquals(1.5,bets(0).betPrice,0)
		assertEquals(LAY,bets(0).betType)	
		assertEquals(U,bets(0).betStatus)
		assertEquals(1,bets(0).marketId)
		assertEquals(11,bets(0).runnerId)
	}

	@Test def testPlaceAFewBets {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,13,2.1,LAY,11)
		market.placeBet(101,121,3,2.2,LAY,11)
		market.placeBet(102,122,5,2.2,LAY,11)
		market.placeBet(103,121,8,2.4,BACK,11)
		market.placeBet(104,122,25,2.5,BACK,11)

		val bets = market.getBets(121)
		assertEquals(2, bets.size)

		assertEquals(101,bets(0).betId)
		assertEquals(121,bets(0).userId)
		assertEquals(3,bets(0).betSize,0)
		assertEquals(2.2,bets(0).betPrice,0)
		assertEquals(LAY,bets(0).betType)
		assertEquals(U,bets(0).betStatus)
		assertEquals(1,bets(0).marketId)
		assertEquals(11,bets(0).runnerId)

		assertEquals(103,bets(1).betId)
		assertEquals(121,bets(0).userId)
		assertEquals(8,bets(1).betSize,0)
		assertEquals(2.4,bets(1).betPrice,0)
		assertEquals(BACK,bets(1).betType)
		assertEquals(U,bets(1).betStatus)
		assertEquals(1,bets(1).marketId)
		assertEquals(11,bets(1).runnerId)

		val bets122 = market.getBets(122)
		assertEquals(3, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(13,bets122(0).betSize,0)
		assertEquals(2.1,bets122(0).betPrice,0)
		assertEquals(LAY,bets122(0).betType)
		assertEquals(U,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		assertEquals(102,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(5,bets122(1).betSize,0)
		assertEquals(2.2,bets122(1).betPrice,0)
		assertEquals(LAY,bets122(1).betType)
		assertEquals(U,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).runnerId)

		assertEquals(104,bets122(2).betId)
		assertEquals(122,bets122(2).userId)
		assertEquals(25,bets122(2).betSize,0)
		assertEquals(2.5,bets122(2).betPrice,0)
		assertEquals(BACK,bets122(2).betType)
		assertEquals(U,bets122(2).betStatus)
		assertEquals(1,bets122(2).marketId)
		assertEquals(11,bets122(2).runnerId)
	}

	@Test def testMatchLayBetWithBackBet {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,2,5,BACK,11)
		market.placeBet(101,122,2,4,BACK,11)
		market.placeBet(102,123,2,7,LAY,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(2, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(2,bets122(0).betSize,0)
		assertEquals(5,bets122(0).betPrice,0)
		assertEquals(BACK,bets122(0).betType)
		assertEquals(U,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		assertEquals(101,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(2,bets122(1).betSize,0)
		assertEquals(4,bets122(1).betPrice,0)
		assertEquals(BACK,bets122(1).betType)
		assertEquals(M,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(1, bets123.size)

		assertEquals(102,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(2,bets123(0).betSize,0)
		assertEquals(4,bets123(0).betPrice,0)
		assertEquals(LAY,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)
	}

	@Test def testMatchBackBetWithLayBet {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,2,4,LAY,11)
		market.placeBet(101,122,2,5,LAY,11)
		market.placeBet(102,123,2,3,BACK,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(2, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(2,bets122(0).betSize,0)
		assertEquals(4,bets122(0).betPrice,0)
		assertEquals(LAY,bets122(0).betType)
		assertEquals(U,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		assertEquals(101,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(2,bets122(1).betSize,0)
		assertEquals(5,bets122(1).betPrice,0)
		assertEquals(LAY,bets122(1).betType)
		assertEquals(M,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(1, bets123.size)

		assertEquals(102,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(2,bets123(0).betSize,0)
		assertEquals(5,bets123(0).betPrice,0)
		assertEquals(BACK,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)
	}

	@Test def testMatchLayBetWithTwoBackBets {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,2,5,BACK,11)
		market.placeBet(101,122,2,4,BACK,11)
		market.placeBet(102,123,4,7,LAY,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(2, bets122.size)

		assertEquals(101,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(2,bets122(0).betSize,0)
		assertEquals(4,bets122(0).betPrice,0)
		assertEquals(BACK,bets122(0).betType)
		assertEquals(M,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		assertEquals(100,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(2,bets122(1).betSize,0)
		assertEquals(5,bets122(1).betPrice,0)
		assertEquals(BACK,bets122(1).betType)
		assertEquals(M,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(2, bets123.size)

		assertEquals(102,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(2,bets123(0).betSize,0)
		assertEquals(4,bets123(0).betPrice,0)
		assertEquals(LAY,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)

		assertEquals(102,bets123(1).betId)
		assertEquals(123,bets123(1).userId)
		assertEquals(2,bets123(1).betSize,0)
		assertEquals(5,bets123(1).betPrice,0)
		assertEquals(LAY,bets123(1).betType)
		assertEquals(M,bets123(1).betStatus)
		assertEquals(1,bets123(1).marketId)
		assertEquals(11,bets123(1).runnerId)
	}

	@Test def testMatchBackBetWithTwoLaysBets {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,2,4,LAY,11)
		market.placeBet(101,122,2,5,LAY,11)
		market.placeBet(102,123,4,3,BACK,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(2, bets122.size)

		assertEquals(101,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(2,bets122(0).betSize,0)
		assertEquals(5,bets122(0).betPrice,0)
		assertEquals(LAY,bets122(0).betType)
		assertEquals(M,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		assertEquals(100,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(2,bets122(1).betSize,0)
		assertEquals(4,bets122(1).betPrice,0)
		assertEquals(LAY,bets122(1).betType)
		assertEquals(M,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(2, bets123.size)

		assertEquals(102,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(2,bets123(0).betSize,0)
		assertEquals(5,bets123(0).betPrice,0)
		assertEquals(BACK,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)

		assertEquals(102,bets123(1).betId)
		assertEquals(123,bets123(1).userId)
		assertEquals(2,bets123(1).betSize,0)
		assertEquals(4,bets123(1).betPrice,0)
		assertEquals(BACK,bets123(1).betType)
		assertEquals(M,bets123(1).betStatus)
		assertEquals(1,bets123(1).marketId)
		assertEquals(11,bets123(1).runnerId)
	}

	@Test def testMatchBackBetPartiallyMatchedWithLayBet {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,2,5,LAY,11)
		market.placeBet(101,123,6,3,BACK,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(1, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(2,bets122(0).betSize,0)
		assertEquals(5,bets122(0).betPrice,0)
		assertEquals(LAY,bets122(0).betType)
		assertEquals(M,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(2, bets123.size)

		assertEquals(101,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(2,bets123(0).betSize,0)
		assertEquals(5,bets123(0).betPrice,0)
		assertEquals(BACK,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)

		assertEquals(101,bets123(1).betId)
		assertEquals(123,bets123(1).userId)
		assertEquals(4,bets123(1).betSize,0)
		assertEquals(3,bets123(1).betPrice,0)
		assertEquals(BACK,bets123(1).betType)
		assertEquals(U,bets123(1).betStatus)
		assertEquals(1,bets123(1).marketId)
		assertEquals(11,bets123(1).runnerId)
	}
	@Test def testMatchBackBetPartiallyMatchedWithTwoLayBets {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,7,2,LAY,11)
		market.placeBet(101,122,2,4,LAY,11)
		market.placeBet(102,122,3,5,LAY,11)
		market.placeBet(103,123,4,3,BACK,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(4, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(7,bets122(0).betSize,0)
		assertEquals(2,bets122(0).betPrice,0)
		assertEquals(LAY,bets122(0).betType)
		assertEquals(U,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		assertEquals(102,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(3,bets122(1).betSize,0)
		assertEquals(5,bets122(1).betPrice,0)
		assertEquals(LAY,bets122(1).betType)
		assertEquals(M,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).runnerId)

		assertEquals(101,bets122(2).betId)
		assertEquals(122,bets122(2).userId)
		assertEquals(1,bets122(2).betSize,0)
		assertEquals(4,bets122(2).betPrice,0)
		assertEquals(LAY,bets122(2).betType)
		assertEquals(M,bets122(2).betStatus)
		assertEquals(1,bets122(2).marketId)
		assertEquals(11,bets122(2).runnerId)

		assertEquals(101,bets122(3).betId)
		assertEquals(122,bets122(3).userId)
		assertEquals(1,bets122(3).betSize,0)
		assertEquals(4,bets122(3).betPrice,0)
		assertEquals(LAY,bets122(3).betType)
		assertEquals(U,bets122(3).betStatus)
		assertEquals(1,bets122(3).marketId)
		assertEquals(11,bets122(3).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(2, bets123.size)

		assertEquals(103,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(3,bets123(0).betSize,0)
		assertEquals(5,bets123(0).betPrice,0)
		assertEquals(BACK,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)

		assertEquals(103,bets123(1).betId)
		assertEquals(123,bets123(1).userId)
		assertEquals(1,bets123(1).betSize,0)
		assertEquals(4,bets123(1).betPrice,0)
		assertEquals(BACK,bets123(1).betType)
		assertEquals(M,bets123(1).betStatus)
		assertEquals(1,bets123(1).marketId)
		assertEquals(11,bets123(1).runnerId)

	}
	@Test def testMatchBackBetPartiallyMatchedWithTwoLayBetsNoBetsRemainsAtMatchingPrice {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,7,2,LAY,11)
		market.placeBet(101,122,2,4,LAY,11)
		market.placeBet(102,122,3,5,LAY,11)
		market.placeBet(103,123,6,3,BACK,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(3, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(7,bets122(0).betSize,0)
		assertEquals(2,bets122(0).betPrice,0)
		assertEquals(LAY,bets122(0).betType)
		assertEquals(U,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		assertEquals(102,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(3,bets122(1).betSize,0)
		assertEquals(5,bets122(1).betPrice,0)
		assertEquals(LAY,bets122(1).betType)
		assertEquals(M,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).runnerId)

		assertEquals(101,bets122(2).betId)
		assertEquals(122,bets122(2).userId)
		assertEquals(2,bets122(2).betSize,0)
		assertEquals(4,bets122(2).betPrice,0)
		assertEquals(LAY,bets122(2).betType)
		assertEquals(M,bets122(2).betStatus)
		assertEquals(1,bets122(2).marketId)
		assertEquals(11,bets122(2).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(3, bets123.size)

		assertEquals(103,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(3,bets123(0).betSize,0)
		assertEquals(5,bets123(0).betPrice,0)
		assertEquals(BACK,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)

		assertEquals(103,bets123(1).betId)
		assertEquals(123,bets123(1).userId)
		assertEquals(2,bets123(1).betSize,0)
		assertEquals(4,bets123(1).betPrice,0)
		assertEquals(BACK,bets123(1).betType)
		assertEquals(M,bets123(1).betStatus)
		assertEquals(1,bets123(1).marketId)
		assertEquals(11,bets123(1).runnerId)

		assertEquals(103,bets123(2).betId)
		assertEquals(123,bets123(2).userId)
		assertEquals(1,bets123(2).betSize,0)
		assertEquals(3,bets123(2).betPrice,0)
		assertEquals(BACK,bets123(2).betType)
		assertEquals(U,bets123(2).betStatus)
		assertEquals(1,bets123(2).marketId)
		assertEquals(11,bets123(2).runnerId)
	}

	@Test def testMatchBackBetFullyMatchedWithBiggerLayBet {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,6,5,LAY,11)
		market.placeBet(101,123,2,3,BACK,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(2, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(2,bets122(0).betSize,0)
		assertEquals(5,bets122(0).betPrice,0)
		assertEquals(LAY,bets122(0).betType)
		assertEquals(M,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		assertEquals(100,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(4,bets122(1).betSize,0)
		assertEquals(5,bets122(1).betPrice,0)
		assertEquals(LAY,bets122(1).betType)
		assertEquals(U,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(1, bets123.size)

		assertEquals(101,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(2,bets123(0).betSize,0)
		assertEquals(5,bets123(0).betPrice,0)
		assertEquals(BACK,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)

	}

	@Test def testMatchLayBetPartiallyMatchedWithBackBet {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,2,8,BACK,11)
		market.placeBet(101,123,6,11,LAY,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(1, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(2,bets122(0).betSize,0)
		assertEquals(8,bets122(0).betPrice,0)
		assertEquals(BACK,bets122(0).betType)
		assertEquals(M,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(2, bets123.size)

		assertEquals(101,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(2,bets123(0).betSize,0)
		assertEquals(8,bets123(0).betPrice,0)
		assertEquals(LAY,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)

		assertEquals(101,bets123(1).betId)
		assertEquals(123,bets123(1).userId)
		assertEquals(4,bets123(1).betSize,0)
		assertEquals(11,bets123(1).betPrice,0)
		assertEquals(LAY,bets123(1).betType)
		assertEquals(U,bets123(1).betStatus)
		assertEquals(1,bets123(1).marketId)
		assertEquals(11,bets123(1).runnerId)
	}

	@Test def testMatchLayBetPartiallyMatchedWithTwoBackBets {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,7,9,BACK,11)
		market.placeBet(101,122,2,6,BACK,11)
		market.placeBet(102,122,3,5,BACK,11)
		market.placeBet(103,123,4,7,LAY,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(4, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(7,bets122(0).betSize,0)
		assertEquals(9,bets122(0).betPrice,0)
		assertEquals(BACK,bets122(0).betType)
		assertEquals(U,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		assertEquals(102,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(3,bets122(1).betSize,0)
		assertEquals(5,bets122(1).betPrice,0)
		assertEquals(BACK,bets122(1).betType)
		assertEquals(M,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).runnerId)

		assertEquals(101,bets122(2).betId)
		assertEquals(122,bets122(2).userId)
		assertEquals(1,bets122(2).betSize,0)
		assertEquals(6,bets122(2).betPrice,0)
		assertEquals(BACK,bets122(2).betType)
		assertEquals(M,bets122(2).betStatus)
		assertEquals(1,bets122(2).marketId)
		assertEquals(11,bets122(2).runnerId)

		assertEquals(101,bets122(3).betId)
		assertEquals(122,bets122(3).userId)
		assertEquals(1,bets122(3).betSize,0)
		assertEquals(6,bets122(3).betPrice,0)
		assertEquals(BACK,bets122(3).betType)
		assertEquals(U,bets122(3).betStatus)
		assertEquals(1,bets122(3).marketId)
		assertEquals(11,bets122(3).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(2, bets123.size)

		assertEquals(103,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(3,bets123(0).betSize,0)
		assertEquals(5,bets123(0).betPrice,0)
		assertEquals(LAY,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)

		assertEquals(103,bets123(1).betId)
		assertEquals(123,bets123(1).userId)
		assertEquals(1,bets123(1).betSize,0)
		assertEquals(6,bets123(1).betPrice,0)
		assertEquals(LAY,bets123(1).betType)
		assertEquals(M,bets123(1).betStatus)
		assertEquals(1,bets123(1).marketId)
		assertEquals(11,bets123(1).runnerId)
	}

	@Test def testMatchLayBetPartiallyMatchedWithTwoBackBetsNoBetsRemainsAtMatchingPrice {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,7,9,BACK,11)
		market.placeBet(101,122,2,6,BACK,11)
		market.placeBet(102,122,3,5,BACK,11)
		market.placeBet(103,123,6,7,LAY,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(3, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(7,bets122(0).betSize,0)
		assertEquals(9,bets122(0).betPrice,0)
		assertEquals(BACK,bets122(0).betType)
		assertEquals(U,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		assertEquals(102,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(3,bets122(1).betSize,0)
		assertEquals(5,bets122(1).betPrice,0)
		assertEquals(BACK,bets122(1).betType)
		assertEquals(M,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).runnerId)

		assertEquals(101,bets122(2).betId)
		assertEquals(122,bets122(2).userId)
		assertEquals(2,bets122(2).betSize,0)
		assertEquals(6,bets122(2).betPrice,0)
		assertEquals(BACK,bets122(2).betType)
		assertEquals(M,bets122(2).betStatus)
		assertEquals(1,bets122(2).marketId)
		assertEquals(11,bets122(2).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(3, bets123.size)

		assertEquals(103,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(3,bets123(0).betSize,0)
		assertEquals(5,bets123(0).betPrice,0)
		assertEquals(LAY,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)

		assertEquals(103,bets123(1).betId)
		assertEquals(123,bets123(1).userId)
		assertEquals(2,bets123(1).betSize,0)
		assertEquals(6,bets123(1).betPrice,0)
		assertEquals(LAY,bets123(1).betType)
		assertEquals(M,bets123(1).betStatus)
		assertEquals(1,bets123(1).marketId)
		assertEquals(11,bets123(1).runnerId)

		assertEquals(103,bets123(2).betId)
		assertEquals(123,bets123(2).userId)
		assertEquals(1,bets123(2).betSize,0)
		assertEquals(7,bets123(2).betPrice,0)
		assertEquals(LAY,bets123(2).betType)
		assertEquals(U,bets123(2).betStatus)
		assertEquals(1,bets123(2).marketId)
		assertEquals(11,bets123(2).runnerId)
	}

	@Test def testMatchLayBetFullyMatchedWithBiggerBackBet {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,6,8,BACK,11)
		market.placeBet(101,123,2,11,LAY,11)

		/**Check bets for user 122.*/
		val bets122 = market.getBets(122)
		assertEquals(2, bets122.size)

		assertEquals(100,bets122(0).betId)
		assertEquals(122,bets122(0).userId)
		assertEquals(2,bets122(0).betSize,0)
		assertEquals(8,bets122(0).betPrice,0)
		assertEquals(BACK,bets122(0).betType)
		assertEquals(M,bets122(0).betStatus)
		assertEquals(1,bets122(0).marketId)
		assertEquals(11,bets122(0).runnerId)

		assertEquals(100,bets122(1).betId)
		assertEquals(122,bets122(1).userId)
		assertEquals(4,bets122(1).betSize,0)
		assertEquals(8,bets122(1).betPrice,0)
		assertEquals(BACK,bets122(1).betType)
		assertEquals(U,bets122(1).betStatus)
		assertEquals(1,bets122(1).marketId)
		assertEquals(11,bets122(1).runnerId)

		/**Check bets for user 123.*/
		val bets123 = market.getBets(123)
		assertEquals(1, bets123.size)

		assertEquals(101,bets123(0).betId)
		assertEquals(123,bets123(0).userId)
		assertEquals(2,bets123(0).betSize,0)
		assertEquals(8,bets123(0).betPrice,0)
		assertEquals(LAY,bets123(0).betType)
		assertEquals(M,bets123(0).betStatus)
		assertEquals(1,bets123(0).marketId)
		assertEquals(11,bets123(0).runnerId)

	}

	/** 
	 *  Tests for getRunnerPrices.
	 * 
	 * */

	@Test def testGetRunnerPricesNoBets {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		assertEquals(0,market.getRunnerPrices(11).size)
	}

	@Test  def testGetRunnerPricesForUnmatchedBetsOnly {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,13,2.1,LAY,11)
		market.placeBet(101,121,3,2.2,LAY,11)
		market.placeBet(102,122,5,2.2,LAY,11)
		market.placeBet(103,121,8,2.4,BACK,11)
		market.placeBet(104,122,25,2.5,BACK,11)

		val runnerPrices = market.getRunnerPrices(11)

		assertEquals(4,runnerPrices.size)

		assertEquals(2.1, runnerPrices(0).price,0)
		assertEquals(13, runnerPrices(0).totalToBack,0)
		assertEquals(0, runnerPrices(0).totalToLay,0)

		assertEquals(2.2, runnerPrices(1).price,0)
		assertEquals(8, runnerPrices(1).totalToBack,0)
		assertEquals(0, runnerPrices(1).totalToLay,0)

		assertEquals(2.4, runnerPrices(2).price,0)
		assertEquals(0, runnerPrices(2).totalToBack,0)
		assertEquals(8, runnerPrices(2).totalToLay,0)

		assertEquals(2.5, runnerPrices(3).price,0)
		assertEquals(0, runnerPrices(3).totalToBack,0)
		assertEquals(25, runnerPrices(3).totalToLay,0)
	}

	@Test  def testGetRunnerPricesForUnmatchedBetsOnMoreThanOneRunner {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		/**Unmatched bets on runner 11.*/
		market.placeBet(100,122,13,2.1,LAY,11)
		market.placeBet(101,121,3,2.2,LAY,11)
		market.placeBet(102,122,5,2.2,LAY,11)
		market.placeBet(103,121,8,2.4,BACK,11)
		market.placeBet(104,122,25,2.5,BACK,11)

		/**Unmatched bets on runner 12.*/
		market.placeBet(100,122,13,2.1,LAY,12)

		val runnerPrices = market.getRunnerPrices(11)

		assertEquals(4,runnerPrices.size)

		assertEquals(2.1, runnerPrices(0).price,0)
		assertEquals(13, runnerPrices(0).totalToBack,0)
		assertEquals(0, runnerPrices(0).totalToLay,0)

		assertEquals(2.2, runnerPrices(1).price,0)
		assertEquals(8, runnerPrices(1).totalToBack,0)
		assertEquals(0, runnerPrices(1).totalToLay,0)

		assertEquals(2.4, runnerPrices(2).price,0)
		assertEquals(0, runnerPrices(2).totalToBack,0)
		assertEquals(8, runnerPrices(2).totalToLay,0)

		assertEquals(2.5, runnerPrices(3).price,0)
		assertEquals(0, runnerPrices(3).totalToBack,0)
		assertEquals(25, runnerPrices(3).totalToLay,0)
	}

	@Test  def testGetRunnerPricesForUnmatchedAndMatchedBets {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		/**Unmatched bets.*/
		market.placeBet(100,122,13,2.1,LAY,11)
		market.placeBet(104,122,25,2.5,BACK,11)

		/**Matching bets.*/
		market.placeBet(104,122,10,2.5,LAY,11)

		val runnerPrices = market.getRunnerPrices(11)

		assertEquals(2,runnerPrices.size)

		assertEquals(2.1, runnerPrices(0).price,0)
		assertEquals(13, runnerPrices(0).totalToBack,0)
		assertEquals(0, runnerPrices(0).totalToLay,0)

		assertEquals(2.5, runnerPrices(1).price,0)
		assertEquals(0, runnerPrices(1).totalToBack,0)
		assertEquals(15, runnerPrices(1).totalToLay,0)
	}

	@Test  def testGetRunnerLayAndBackBetsonTheSamePrice {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))

		market.placeBet(100,122,5,2.4,BACK,11)
		market.placeBet(101,122,8,2.4,LAY,11)

		val runnerPrices = market.getRunnerPrices(11)

		assertEquals(1,runnerPrices.size)

		assertEquals(2.4, runnerPrices(0).price,0)
		assertEquals(3, runnerPrices(0).totalToBack,0)
		assertEquals(0, runnerPrices(0).totalToLay,0)
	}
}