package dk.bettingai.marketsimulator.risk

import org.junit._
import Assert._

class MarketExpectedProfitTest {

	@Test
	def testIfLose {
		val marketExpectedProfit = new MarketExpectedProfit(0d,Map(11l -> 200,12l -> -100),Map(11l -> 1/3d,12l -> 1/1.5d))
		assertEquals(-100,marketExpectedProfit.ifLose(11),0.001)
		assertEquals(200,marketExpectedProfit.ifLose(12),0.001)
	}
}