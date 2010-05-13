package dk.bettingai.marketsimulator.betex

import org.junit._
import Assert._
import Bet.BetTypeEnum._
import Bet.BetStatusEnum._

class BetTest {

	@Test def testCreateBet{
		new Bet(10,123,2,1.01,BACK,U,1,11)
		new Bet(10,123,2,1.5,BACK,M,1,11)
		new Bet(10,123,2,1000,BACK,M,1,11)
		new Bet(10,123,100,3,LAY,U,1,11)
	}
	
	@Test(expected=classOf[IllegalArgumentException]) def testCreateBetPriceLessThanMin{
		new Bet(10,123,2,1,BACK,U,1,11)
	}
	
	@Test(expected=classOf[IllegalArgumentException]) def testCreateBetPriceMoreThanMax{
		new Bet(10,123,2,1001,BACK,U,1,11)
	}
	
	@Test def testMatchBetBackWithLay {
		val firstBet = new Bet(10,122,10,2,BACK,U,1,11)
		val secondBet = new Bet(11,123,10,2,LAY,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(BACK,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(LAY,matchResult(1).betType)
		assertEquals(M,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
	
	@Test def testMatchBetBackWithLayDifferentPrice {
		val firstBet = new Bet(10,122,10,1.5,BACK,U,1,11)
		val secondBet = new Bet(11,123,10,2,LAY,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(BACK,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(LAY,matchResult(1).betType)
		assertEquals(M,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
	
	@Test def testMatchBetBigBackWithLay {
		val firstBet = new Bet(10,122,12,2,BACK,U,1,11)
		val secondBet = new Bet(11,123,10,2,LAY,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(3,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(BACK,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(10,matchResult(1).betId)
		assertEquals(122,matchResult(1).userId)
		assertEquals(2,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(U,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
		assertEquals(11,matchResult(2).betId)
		assertEquals(123,matchResult(2).userId)
		assertEquals(10,matchResult(2).betSize,0)
		assertEquals(2,matchResult(2).betPrice,0)
		assertEquals(LAY,matchResult(2).betType)
		assertEquals(M,matchResult(2).betStatus)
		assertEquals(1,matchResult(2).marketId)
		assertEquals(11,matchResult(2).selectionId)
		
	}
	
	@Test def testMatchBetBigBackWithLayDifferentPrice {
		val firstBet = new Bet(10,122,12,2,BACK,U,1,11)
		val secondBet = new Bet(11,123,10,3,LAY,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(3,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(3,matchResult(0).betPrice,0)
		assertEquals(BACK,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(10,matchResult(1).betId)
		assertEquals(122,matchResult(1).userId)
		assertEquals(2,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(U,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
		assertEquals(11,matchResult(2).betId)
		assertEquals(123,matchResult(2).userId)
		assertEquals(10,matchResult(2).betSize,0)
		assertEquals(3,matchResult(2).betPrice,0)
		assertEquals(LAY,matchResult(2).betType)
		assertEquals(M,matchResult(2).betStatus)
		assertEquals(1,matchResult(2).marketId)
		assertEquals(11,matchResult(2).selectionId)
		
	}
	
	@Test def testMatchBetBackWithBigLay {
		val firstBet = new Bet(10,122,10,2,BACK,U,1,11)
		val secondBet = new Bet(11,123,15,2,LAY,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(3,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(BACK,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
			assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(LAY,matchResult(1).betType)
		assertEquals(M,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
		assertEquals(11,matchResult(2).betId)
		assertEquals(123,matchResult(2).userId)
		assertEquals(5,matchResult(2).betSize,0)
		assertEquals(2,matchResult(2).betPrice,0)
		assertEquals(LAY,matchResult(2).betType)
		assertEquals(U,matchResult(2).betStatus)
		assertEquals(1,matchResult(2).marketId)
		assertEquals(11,matchResult(2).selectionId)
		
	}
	
	@Test def testMatchBetBackWithBigLayDifferentPrice {
		val firstBet = new Bet(10,122,10,2,BACK,U,1,11)
		val secondBet = new Bet(11,123,15,3,LAY,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(3,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(3,matchResult(0).betPrice,0)
		assertEquals(BACK,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
			assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(3,matchResult(1).betPrice,0)
		assertEquals(LAY,matchResult(1).betType)
		assertEquals(M,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
		assertEquals(11,matchResult(2).betId)
		assertEquals(123,matchResult(2).userId)
		assertEquals(5,matchResult(2).betSize,0)
		assertEquals(3,matchResult(2).betPrice,0)
		assertEquals(LAY,matchResult(2).betType)
		assertEquals(U,matchResult(2).betStatus)
		assertEquals(1,matchResult(2).marketId)
		assertEquals(11,matchResult(2).selectionId)
		
	}
	
	@Test def testMatchBetLayWithBack {
		val firstBet = new Bet(10,122,10,2,LAY,U,1,11)
		val secondBet = new Bet(11,123,10,2,BACK,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(M,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
	
		@Test def testMatchBetLayWithBackDifferentPrice {
		val firstBet = new Bet(10,122,10,2,LAY,U,1,11)
		val secondBet = new Bet(11,123,10,1.5,BACK,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(1.5,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(1.5,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(M,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
	
	@Test def testMatchBetBigLayWithBack {
		val firstBet = new Bet(10,122,14,2,LAY,U,1,11)
		val secondBet = new Bet(11,123,10,2,BACK,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(3,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(10,matchResult(1).betId)
		assertEquals(122,matchResult(1).userId)
		assertEquals(4,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(LAY,matchResult(1).betType)
		assertEquals(U,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
		assertEquals(11,matchResult(2).betId)
		assertEquals(123,matchResult(2).userId)
		assertEquals(10,matchResult(2).betSize,0)
		assertEquals(2,matchResult(2).betPrice,0)
		assertEquals(BACK,matchResult(2).betType)
		assertEquals(M,matchResult(2).betStatus)
		assertEquals(1,matchResult(2).marketId)
		assertEquals(11,matchResult(2).selectionId)
		
	}
	
	@Test def testMatchBetBigLayWithBackDifferentPrice {
		val firstBet = new Bet(10,122,14,2,LAY,U,1,11)
		val secondBet = new Bet(11,123,10,1.5,BACK,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(3,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(1.5,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(10,matchResult(1).betId)
		assertEquals(122,matchResult(1).userId)
		assertEquals(4,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(LAY,matchResult(1).betType)
		assertEquals(U,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
		assertEquals(11,matchResult(2).betId)
		assertEquals(123,matchResult(2).userId)
		assertEquals(10,matchResult(2).betSize,0)
		assertEquals(1.5,matchResult(2).betPrice,0)
		assertEquals(BACK,matchResult(2).betType)
		assertEquals(M,matchResult(2).betStatus)
		assertEquals(1,matchResult(2).marketId)
		assertEquals(11,matchResult(2).selectionId)
		
	}
	
	@Test def testMatchBetLayWithBigBack {
		val firstBet = new Bet(10,122,10,2,LAY,U,1,11)
		val secondBet = new Bet(11,123,15,2,BACK,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(3,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(M,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
		assertEquals(11,matchResult(2).betId)
		assertEquals(123,matchResult(2).userId)
		assertEquals(5,matchResult(2).betSize,0)
		assertEquals(2,matchResult(2).betPrice,0)
		assertEquals(BACK,matchResult(2).betType)
		assertEquals(U,matchResult(2).betStatus)
		assertEquals(1,matchResult(2).marketId)
		assertEquals(11,matchResult(2).selectionId)
		
	}
	
	@Test def testMatchBetLayWithBigBackDifferentPrice {
		val firstBet = new Bet(10,122,10,2,LAY,U,1,11)
		val secondBet = new Bet(11,123,15,1.5,BACK,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(3,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(1.5,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(1.5,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(M,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
		assertEquals(11,matchResult(2).betId)
		assertEquals(123,matchResult(2).userId)
		assertEquals(5,matchResult(2).betSize,0)
		assertEquals(1.5,matchResult(2).betPrice,0)
		assertEquals(BACK,matchResult(2).betType)
		assertEquals(U,matchResult(2).betStatus)
		assertEquals(1,matchResult(2).marketId)
		assertEquals(11,matchResult(2).selectionId)
		
	}
	
	
	/**Bets not matched scenarios.*/
	
	@Test def testMatchBetTwoBackBets {
		val firstBet = new Bet(10,122,10,2,BACK,U,1,11)
		val secondBet = new Bet(11,123,10,2,BACK,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(BACK,matchResult(0).betType)
		assertEquals(U,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(U,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
	
	@Test def testMatchBetTwoLayBets {
		val firstBet = new Bet(10,122,10,2,LAY,U,1,11)
		val secondBet = new Bet(11,123,10,2,LAY,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(U,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(LAY,matchResult(1).betType)
		assertEquals(U,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
	
	@Test def testMatchBetDifferentMarketId {
		val firstBet = new Bet(10,122,10,2,LAY,U,1,11)
		val secondBet = new Bet(11,123,10,2,BACK,U,2,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(U,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(U,matchResult(1).betStatus)
		assertEquals(2,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
	
	@Test def testMatchBetDifferentSelectionId {
		val firstBet = new Bet(10,122,10,2,LAY,U,1,11)
		val secondBet = new Bet(11,123,10,2,BACK,U,1,12)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(U,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(U,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(12,matchResult(1).selectionId)
		
	}
	@Test def testMatchBetFirstBackSecondLayPriceNotMatching {
		val firstBet = new Bet(10,122,10,3,BACK,U,1,11)
		val secondBet = new Bet(11,123,10,2,LAY,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(3,matchResult(0).betPrice,0)
		assertEquals(BACK,matchResult(0).betType)
		assertEquals(U,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(LAY,matchResult(1).betType)
		assertEquals(U,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
	
	@Test def testMatchBetFirstLaySecondBackPriceNotMatching {
		val firstBet = new Bet(10,122,10,2,LAY,U,1,11)
		val secondBet = new Bet(11,123,10,3,BACK,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(U,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(3,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(U,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
	
	@Test def testMatchBetFirstBetAlreadyMatched{
		val firstBet = new Bet(10,122,10,2,LAY,M,1,11)
		val secondBet = new Bet(11,123,10,2,BACK,U,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(U,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
	
	@Test def testMatchBetSecondBetAlreadyMatched{
		val firstBet = new Bet(10,122,10,2,LAY,U,1,11)
		val secondBet = new Bet(11,123,10,2,BACK,M,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(U,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(M,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
	
	@Test def testMatchBetBothBetsAlreadyMatched{
		val firstBet = new Bet(10,122,10,2,LAY,M,1,11)
		val secondBet = new Bet(11,123,10,2,BACK,M,1,11)
		
		val matchResult = firstBet.matchBet(secondBet)
		
		assertEquals(2,matchResult.size)
		
		assertEquals(10,matchResult(0).betId)
		assertEquals(122,matchResult(0).userId)
		assertEquals(10,matchResult(0).betSize,0)
		assertEquals(2,matchResult(0).betPrice,0)
		assertEquals(LAY,matchResult(0).betType)
		assertEquals(M,matchResult(0).betStatus)
		assertEquals(1,matchResult(0).marketId)
		assertEquals(11,matchResult(0).selectionId)
		
		assertEquals(11,matchResult(1).betId)
		assertEquals(123,matchResult(1).userId)
		assertEquals(10,matchResult(1).betSize,0)
		assertEquals(2,matchResult(1).betPrice,0)
		assertEquals(BACK,matchResult(1).betType)
		assertEquals(M,matchResult(1).betStatus)
		assertEquals(1,matchResult(1).marketId)
		assertEquals(11,matchResult(1).selectionId)
		
	}
}