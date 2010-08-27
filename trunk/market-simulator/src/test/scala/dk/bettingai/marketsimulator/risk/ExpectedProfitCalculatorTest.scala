package dk.bettingai.marketsimulator.risk

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.api.IBet.BetTypeEnum._
class ExpectedProfitCalculatorTest {

	@Test def testCalculateNoBets {
		val expectedProfit = ExpectedProfitCalculator.calculate(List(), Map(11l->1.5,12l->3))
		assertEquals(0,expectedProfit.marketExpectedProfit,0)
		assertEquals(2,expectedProfit.runnersIfWin.size)
		assertEquals(0,expectedProfit.runnersIfWin(11),0)
		assertEquals(0,expectedProfit.runnersIfWin(12),0)
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testCalculateNoProbabilities {
		val bets = List(Bet(100,123,10,2,BACK,1,11))
		val probabilities:Map[Long,Double] = Map()
		ExpectedProfitCalculator.calculate(bets, probabilities)
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testCalculateWrongProbabilities {
		val bets = List(Bet(100,123,10,2,BACK,1,11))
		val probabilities:Map[Long,Double] = Map(12l -> 2.1)
		ExpectedProfitCalculator.calculate(bets, probabilities)
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testCalculateBetsOnDifferentMarkets {
		val bets = List(Bet(100,123,10,2,BACK,1,11),Bet(101,123,10,2,BACK,2,11))
		val probabilities:Map[Long,Double] = Map(11l -> 2.1)
		ExpectedProfitCalculator.calculate(bets, probabilities)

	}

	@Test def testCalculateOneBackBetProbabilityNotChanged {
		val bets = List(Bet(100,123,10,3,BACK,1,11))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/3d,12l->1.5d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit.marketExpectedProfit,0)

		assertEquals(2,expectedProfit.runnersIfWin.size)
		assertEquals(20,expectedProfit.runnersIfWin(11),0)
		assertEquals(-10,expectedProfit.runnersIfWin(12),0)
	}

	@Test def testCalculateOneBackBetProbabilityChanged1 {
		val bets = List(Bet(100,123,10,3,BACK,1,11))

		val probabilities:Map[Long,Double] = Map(11l -> 1d/4d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(-2.5,expectedProfit.marketExpectedProfit,0.001)

		assertEquals(1,expectedProfit.runnersIfWin.size)
		assertEquals(20,expectedProfit.runnersIfWin(11),0)
	}
	@Test def testCalculateOneBackBetProbabilityChanged2 {
		val bets = List(Bet(100,123,10,3,BACK,1,11))

		val probabilities:Map[Long,Double] = Map(11l -> 1d/1.5)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(10,expectedProfit.marketExpectedProfit,0.001)

		assertEquals(1,expectedProfit.runnersIfWin.size)
		assertEquals(20,expectedProfit.runnersIfWin(11),0)
	}

	@Test def testCalculateOneLayBetProbabilityNotChanged {
		val bets = List(Bet(100,123,10,2,LAY,1,11))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/2d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit.marketExpectedProfit,0)

		assertEquals(1,expectedProfit.runnersIfWin.size)
		assertEquals(-10,expectedProfit.runnersIfWin(11),0)
	}

	@Test def testCalculateOneLayBetProbabilityChanged1 {
		val bets = List(Bet(100,123,10,2,LAY,1,11))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/3d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(3.333,expectedProfit.marketExpectedProfit,0.001)

		assertEquals(1,expectedProfit.runnersIfWin.size)
		assertEquals(-10,expectedProfit.runnersIfWin(11),0)
	}

	@Test def testCalculateOneLayBetProbabilityChanged2 {
		val bets = List(Bet(100,123,10,2.5,LAY,1,11))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/1.5,12l ->3)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(-6.666,expectedProfit.marketExpectedProfit,0.001)

		assertEquals(2,expectedProfit.runnersIfWin.size)
		assertEquals(-15,expectedProfit.runnersIfWin(11),0)
		assertEquals(10,expectedProfit.runnersIfWin(12),0)
	}

	@Test def testCalculateTwoBackBetsOnTwoRunnersBetProbabilityNotChanged {
		val bets = List(Bet(100,123,10,1.5,BACK,1,11),Bet(101,123,10,3,BACK,1,12))
		val probabilities:Map[Long,Double] = Map(11l -> 1/1.5,12l -> 1d/3d,13l->0d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit.marketExpectedProfit,0)

		assertEquals(3,expectedProfit.runnersIfWin.size)
		assertEquals(-5,expectedProfit.runnersIfWin(11),0)
		assertEquals(10,expectedProfit.runnersIfWin(12),0)
		assertEquals(-20,expectedProfit.runnersIfWin(13),0)
	}

	@Test def testCalculateTwoBackBetsOnTwoRunnersBetProbabilityChanged {
		val bets = List(Bet(100,123,10,1.5,BACK,1,11),Bet(101,123,10,3,BACK,1,12))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/3d,12l -> 1/1.5)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(5,expectedProfit.marketExpectedProfit,0)

		assertEquals(2,expectedProfit.runnersIfWin.size)
		assertEquals(-5,expectedProfit.runnersIfWin(11),0)
		assertEquals(10,expectedProfit.runnersIfWin(12),0)
	}

	@Test def testCalculateTwoLayBetsOnTwoRunnersBetProbabilityNotChanged {
		val bets = List(Bet(100,123,10,1.5,LAY,1,11),Bet(101,123,10,3,LAY,1,12))
		val probabilities:Map[Long,Double] = Map(11l -> 1/1.5,12l -> 1d/3d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit.marketExpectedProfit,0)

		assertEquals(2,expectedProfit.runnersIfWin.size)
		assertEquals(5,expectedProfit.runnersIfWin(11),0)
		assertEquals(-10,expectedProfit.runnersIfWin(12),0)
	}

	@Test def testCalculateTwoLayBetsOnTwoRunnersBetProbabilityChanged {
		val bets = List(Bet(100,123,10,1.5,LAY,1,11),Bet(100,123,10,3,LAY,1,12))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/3d,12l -> 1/1.5)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(-5,expectedProfit.marketExpectedProfit,0)

		assertEquals(2,expectedProfit.runnersIfWin.size)
		assertEquals(5,expectedProfit.runnersIfWin(11),0)
		assertEquals(-10,expectedProfit.runnersIfWin(12),0)
	}

	@Test def testCalculateTwoBackBetsAndTwoLayBetsOnThreeRunnersBetProbabilityNotChanged {
		val bets = List(Bet(100,123,10,2,BACK,1,11),Bet(101,123,10,3,BACK,1,12),Bet(100,123,10,6,BACK,1,13),Bet(102,123,10,2,LAY,1,11),Bet(100,123,10,3,LAY,1,12),Bet(103,123,10,6,LAY,1,13))
		val probabilities:Map[Long,Double] = Map(11l -> 1/2,12l -> 1/3, 13l -> 1/6)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit.marketExpectedProfit,0)

		assertEquals(3,expectedProfit.runnersIfWin.size)
		assertEquals(0,expectedProfit.runnersIfWin(11),0)
		assertEquals(0,expectedProfit.runnersIfWin(12),0)
		assertEquals(0,expectedProfit.runnersIfWin(13),0)
	}

	@Test def testCalculateTwoBackBetsAndTwoLayBetsOnTwoRunnersBetProbabilityChanged {
		val bets = List(Bet(100,123,10,2,BACK,1,11),Bet(100,123,10,3,BACK,1,12),Bet(101,123,10,6,BACK,1,13),Bet(102,123,10,2,LAY,1,11),Bet(103,123,10,3,LAY,1,12),Bet(104,123,10,6,LAY,1,13))
		val probabilities:Map[Long,Double] = Map(11l -> 1/6,12l -> 1/3, 13l -> 1/2)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit.marketExpectedProfit,0.001)

		assertEquals(3,expectedProfit.runnersIfWin.size)
		assertEquals(0,expectedProfit.runnersIfWin(11),0)
		assertEquals(0,expectedProfit.runnersIfWin(12),0)
		assertEquals(0,expectedProfit.runnersIfWin(13),0)
	}

	@Test def testCalculateALotOfBetsHedgesToZeroProbabilitiesChanged {
		val bets = List(
				Bet(100,123,10,2,BACK,1,11),
				Bet(101,123,10,3,BACK,1,12),
				Bet(102,123,10,6,BACK,1,13),
				Bet(103,123,10,2,LAY,1,11),
				Bet(104,123,10,3,LAY,1,12),
				Bet(105,123,10,6,LAY,1,13),
				Bet(106,123,13,4,BACK,1,11),
				Bet(107,123,14,5,BACK,1,12),
				Bet(108,123,15,6,BACK,1,13),
				Bet(109,123,13,4,LAY,1,11),
				Bet(110,123,14,5,LAY,1,12),
				Bet(111,123,15,6,LAY,1,13)		
		)
		val probabilities:Map[Long,Double] = Map(11l -> 1/6,12l -> 1/3, 13l -> 1/2)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit.marketExpectedProfit,0.001)

		assertEquals(3,expectedProfit.runnersIfWin.size)
		assertEquals(0,expectedProfit.runnersIfWin(11),0)
		assertEquals(0,expectedProfit.runnersIfWin(12),0)
		assertEquals(0,expectedProfit.runnersIfWin(13),0)
	}

	/**Tests for avgPrice */
	@Test def testAvgPriceNoBets {
		val bets = Nil
		assertTrue(ExpectedProfitCalculator.avgPrice(bets).isNaN)
	}

	@Test def testAvgPrice {
		val bets = Bet(100,123,2,2,BACK,1,11) ::Bet(101,123,3,3,BACK,1,12) :: Nil
		assertEquals(2.6,ExpectedProfitCalculator.avgPrice(bets),0)
	}

}