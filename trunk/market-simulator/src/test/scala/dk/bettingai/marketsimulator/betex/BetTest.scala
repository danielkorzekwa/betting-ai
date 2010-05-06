package dk.bettingai.marketsimulator.betex

import org.junit._
import Assert._
import Bet.BetTypeEnum._

class BetTest {

	@Test def testCreateBet{
		new Bet(2,1.01,BACK,1,11)
		new Bet(2,1.5,BACK,1,11)
		new Bet(2,1000,BACK,1,11)
		new Bet(100,3,LAY,1,11)
	}
	
	@Test(expected=classOf[IllegalArgumentException]) def testCreateBetSizeLessThanMin{
		new Bet(1.9,2,BACK,1,11)
	}
	
	@Test(expected=classOf[IllegalArgumentException]) def testCreateBetPriceLessThanMin{
		new Bet(2,1,BACK,1,11)
	}
	
	@Test(expected=classOf[IllegalArgumentException]) def testCreateBetPriceMoreThanMax{
		new Bet(2,1001,BACK,1,11)
	}
}