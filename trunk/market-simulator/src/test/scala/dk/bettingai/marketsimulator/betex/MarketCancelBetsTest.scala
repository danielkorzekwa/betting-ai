package dk.bettingai.marketsimulator.betex

import org.junit._
import Assert._
import java.util.Date
import dk.bettingai.marketsimulator.betex.api._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._

class MarketCancelBetsTest {

	/**
	 *Tests for cancel bets.
	 */

	@Test def testCancelBackBetNothingToCancel {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		assertEquals(0,market.cancelBets(123,10,1.95,BACK,11),0)
	}

	@Test def testCancelMatchedBackBetIsCancelled {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,10,1.95,BACK,11)
		market.placeBet(101,123,10,1.95,LAY,11)

		assertEquals(2,market.getBets(123).size)
		assertEquals(0,market.cancelBets(123,10,1.95,BACK,11),0)
		assertEquals(2,market.getBets(123).size)
	}

	@Test def testCancelUnmatchedBackBetIsCancelled {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,10,1.95,BACK,11)
	
		assertEquals(1,market.getBets(123).size)
		assertEquals(10,market.cancelBets(123,10,1.95,BACK,11),0)
		assertEquals(0,market.getBets(123).size)
	}
	@Test def testCancelUnmatchedBackBetIsCancelled2 {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,10,1.95,BACK,11)

		assertEquals(1,market.getBets(123).size)
		assertEquals(10,market.cancelBets(123,20,1.95,BACK,11),0)
		assertEquals(0,market.getBets(123).size)
	}
	
	@Test def testCancelUnmatchedLayBetIsCancelled {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,10,1.95,LAY,11)

		assertEquals(1,market.getBets(123).size)
		assertEquals(10,market.cancelBets(123,10,1.95,LAY,11),0)
		assertEquals(0,market.getBets(123).size)
	}
	@Test def testCancelUnmatchedLayBetIsCancelled2 {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,10,1.95,LAY,11)

		assertEquals(1,market.getBets(123).size)
		assertEquals(10,market.cancelBets(123,20,1.95,LAY,11),0)
		assertEquals(0,market.getBets(123).size)
	}

	@Test def testCancelPartOfUnmatchedBackBetIsCancelled {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,10,1.95,BACK,11)

		assertEquals(1,market.getBets(123).size)
		assertEquals(3,market.cancelBets(123,3,1.95,BACK,11),0)
		assertEquals(1,market.getBets(123).size)
		assertEquals(100,market.getBets(123)(0).betId)
		assertEquals(7,market.getBets(123)(0).betSize,0)
	}

	@Test def testCancelPartiallyMatchedBackBetIsCancelled {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,3,1.95,BACK,11)
		market.placeBet(101,123,2,1.95,LAY,11)

		assertEquals(3,market.getBets(123).size)
		assertEquals(1,market.cancelBets(123,1,1.95,BACK,11),0)
		assertEquals(2,market.getBets(123).size)
	}
	@Test def testCancelPartiallyMatchedBackBetIsCancelled2 {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,3,1.95,BACK,11)
		market.placeBet(101,123,2,1.95,LAY,11)

		assertEquals(3,market.getBets(123).size)
		assertEquals(1,market.cancelBets(123,10,1.95,BACK,11),0)
		assertEquals(2,market.getBets(123).size)
	}

	@Test def testCancelUnsettledBackBetWhenTwoBetsExists {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,3,1.95,BACK,11)
		market.placeBet(101,123,2,1.95,BACK,11)

		assertEquals(2,market.getBets(123).size)
		assertEquals(2,market.cancelBets(123,2,1.95,BACK,11),0)
		assertEquals(1,market.getBets(123).size)
		assertEquals(100,market.getBets(123)(0).betId)
	}
	@Test def testCancelUnsettledBackBetWhenTwoBetsExists2 {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,3,1.95,BACK,11)
		market.placeBet(101,123,2,1.95,BACK,11)

		assertEquals(2,market.getBets(123).size)
		assertEquals(5,market.cancelBets(123,6,1.95,BACK,11),0)
		assertEquals(0,market.getBets(123).size)
	}

	@Test def testCancelOneAndHalfOfUnsettledBackBetsWhenTwoBetsExists {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,3,1.95,BACK,11)
		market.placeBet(101,123,2,1.95,BACK,11)

		assertEquals(2,market.getBets(123).size)
		assertEquals(4,market.cancelBets(123,4,1.95,BACK,11),0)
		assertEquals(1,market.getBets(123).size)
		assertEquals(100,market.getBets(123)(0).betId)
		assertEquals(1,market.getBets(123)(0).betSize,1)
	}

	@Test def testCancelOneAndHalfOfUnsettledBackBetsWhenThreeBetsExists {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,4,1.95,BACK,11)
		market.placeBet(101,123,3,1.95,BACK,11)
		market.placeBet(102,123,2,1.95,BACK,11)

		assertEquals(3,market.getBets(123).size)
		assertEquals(4,market.cancelBets(123,4,1.95,BACK,11),0)
		assertEquals(2,market.getBets(123).size)
		
		assertEquals(100,market.getBets(123)(0).betId)
		assertEquals(4,market.getBets(123)(0).betSize,1)
		
		assertEquals(101,market.getBets(123)(1).betId)
		assertEquals(1,market.getBets(123)(1).betSize,1)
	}
	
	@Test def testCancelAllUnsettledBackBetsWhenThreeBetsExists {
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))	
		market.placeBet(100,123,4,1.95,BACK,11)
		market.placeBet(101,123,3,1.95,BACK,11)
		market.placeBet(102,123,2,1.95,BACK,11)

		assertEquals(3,market.getBets(123).size)
		assertEquals(9,market.cancelBets(123,20,1.95,BACK,11),0)
		assertEquals(0,market.getBets(123).size)
	}

}