package dk.bettingai.marketsimulator.betex

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex.api.IBet.BetTypeEnum._

class BetUtilTest {

	/**Tests for avgPrice */
	@Test def testAvgPriceNoBets {
		val bets = Nil
		assertTrue(BetUtil.avgPrice(bets).isNaN)
	}

	@Test def testAvgPrice {
		val bets = Bet(100,123,2,2,BACK,1,11) ::Bet(101,123,3,3,BACK,1,12) :: Nil
		assertEquals(2.6,BetUtil.avgPrice(bets),0)
	}

}