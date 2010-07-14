package dk.bettingai.marketsimulator.risk

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.api.IBet.BetTypeEnum._
class ExpectedProfitCalculatorTest {

	@Test def testCalculateNoBets {
		assertEquals(0,ExpectedProfitCalculator.calculate(List(), Map()),0)
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testCalculateNoProbabilities {
		val bets = List(Bet(100,123,10,2,BACK,1,11))
		val probabilities:Map[Long,Double] = Map()
		ExpectedProfitCalculator.calculate(bets, probabilities)
	}
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
		val bets = List(Bet(100,123,10,2,BACK,1,11))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/2d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit,0)
	}

	@Test def testCalculateOneBackBetProbabilityChanged1 {
		val bets = List(Bet(100,123,10,2,BACK,1,11))
		
		val probabilities:Map[Long,Double] = Map(11l -> 1d/3d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(-3.333,expectedProfit,0.001)
	}
	@Test def testCalculateOneBackBetProbabilityChanged2 {
		val bets = List(Bet(100,123,10,2,BACK,1,11))
		
		val probabilities:Map[Long,Double] = Map(11l -> 1d/1.5)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(3.333,expectedProfit,0.001)
	}

	@Test def testCalculateOneLayBetProbabilityNotChanged {
		val bets = List(Bet(100,123,10,2,LAY,1,11))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/2d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit,0)
	}

	@Test def testCalculateOneLayBetProbabilityChanged1 {
		val bets = List(Bet(100,123,10,2,LAY,1,11))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/3d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(3.333,expectedProfit,0.001)
	}
	
		@Test def testCalculateOneLayBetProbabilityChanged2 {
		val bets = List(Bet(100,123,10,2,LAY,1,11))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/1.5)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(-3.333,expectedProfit,0.001)
	}

	@Test def testCalculateTwoBackBetsOnTwoRunnersBetProbabilityNotChanged {
		val bets = List(Bet(100,123,10,1.5,BACK,1,11),Bet(101,123,10,3,BACK,1,12))
		val probabilities:Map[Long,Double] = Map(11l -> 1/1.5,12l -> 1d/3d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit,0)
	}

	@Test def testCalculateTwoBackBetsOnTwoRunnersBetProbabilityChanged {
	val bets = List(Bet(100,123,10,1.5,BACK,1,11),Bet(101,123,10,3,BACK,1,12))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/3d,12l -> 1/1.5)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(5,expectedProfit,0)
	}

	@Test def testCalculateTwoLayBetsOnTwoRunnersBetProbabilityNotChanged {
		val bets = List(Bet(100,123,10,1.5,LAY,1,11),Bet(101,123,10,3,LAY,1,12))
		val probabilities:Map[Long,Double] = Map(11l -> 1/1.5,12l -> 1d/3d)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit,0)
	}

	@Test def testCalculateTwoLayBetsOnTwoRunnersBetProbabilityChanged {
		val bets = List(Bet(100,123,10,1.5,LAY,1,11),Bet(100,123,10,3,LAY,1,12))
		val probabilities:Map[Long,Double] = Map(11l -> 1d/3d,12l -> 1/1.5)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(-5,expectedProfit,0)
	}

	@Test def testCalculateTwoBackBetsAndTwoLayBetsOnThreeRunnersBetProbabilityNotChanged {
		val bets = List(Bet(100,123,10,2,BACK,1,11),Bet(101,123,10,3,BACK,1,12),Bet(100,123,10,6,BACK,1,13),Bet(102,123,10,2,LAY,1,11),Bet(100,123,10,3,LAY,1,12),Bet(103,123,10,6,LAY,1,13))
		val probabilities:Map[Long,Double] = Map(11l -> 1/2,12l -> 1/3, 13l -> 1/6)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit,0)
	}

	@Test def testCalculateTwoBackBetsAndTwoLayBetsOnTwoRunnersBetProbabilityChanged {
			val bets = List(Bet(100,123,10,2,BACK,1,11),Bet(100,123,10,3,BACK,1,12),Bet(101,123,10,6,BACK,1,13),Bet(102,123,10,2,LAY,1,11),Bet(103,123,10,3,LAY,1,12),Bet(104,123,10,6,LAY,1,13))
		val probabilities:Map[Long,Double] = Map(11l -> 1/6,12l -> 1/3, 13l -> 1/2)
		val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities)
		assertEquals(0,expectedProfit,0.001)
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
		assertEquals(0,expectedProfit,0.001)
	}

}