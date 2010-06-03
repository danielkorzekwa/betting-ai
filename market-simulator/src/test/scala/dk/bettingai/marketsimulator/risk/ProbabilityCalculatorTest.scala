package dk.bettingai.marketsimulator.risk

import org.junit._
import Assert._

class ProbabilityCalculatorTest {

	@Test def calculateNoPrices {
		assertEquals(0,ProbabilityCalculator.calculate(Map(),1).size)
	}

	@Test(expected=classOf[IllegalArgumentException]) def calculateNumOfWinnersLessThan1 {
		ProbabilityCalculator.calculate(Map(),0)
	}

	/**
	 * Winner market scenarios.
	 * */

	@Test def calculateWinnerMarketNotSettled {
		val marketPrices = Map(1l -> (1.9,2.5),2l -> (2.9,3.5),3l -> (5.9,6.5) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(0.5,probs(1), 0.001)
		assertEquals(0.333,probs(2), 0.001)
		assertEquals(0.166,probs(3), 0.001)
	}

	@Test def calculateWinnerMarketNotSettledToBackAndToLayPricesAreTheSame {
		val marketPrices = Map(1l -> (2d,2d),2l -> (2d,2d),3l -> (2d,2d) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(0.333,probs(1), 0.001)
		assertEquals(0.333,probs(2), 0.001)
		assertEquals(0.333,probs(3), 0.001)
	}

	@Test def calculateWinnerMarketFullySettled {
		val marketPrices = Map(1l -> (1d,1d),2l -> (Double.PositiveInfinity,Double.PositiveInfinity),3l -> (Double.PositiveInfinity,Double.PositiveInfinity) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(1,probs(1), 0)
		assertEquals(0,probs(2), 0)
		assertEquals(0,probs(3), 0)
	}
	@Test def calculateWinnerMarketFullySettledDeadHeat {
		val marketPrices = Map(1l -> (1d,1d),2l -> (1d,1d),3l -> (Double.PositiveInfinity,Double.PositiveInfinity) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(0.5,probs(1), 0)
		assertEquals(0.5,probs(2), 0)
		assertEquals(0,probs(3), 0)
	}

	/**The probability calculator may return negative probabilities for irrational prices. 
	 * It's a client responsibility to pass rational prices to the calculator and deal negative probabilities.*/
	@Test def calculateWinnerMarketPartiallySettledOneWinner {	
		val marketPrices = Map(1l -> (1.9,2.5),2l -> (1d,1d),3l -> (5.9,6.5) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(-0.0928,probs(1), 0.001)
		assertEquals(1,probs(2), 0.001)
		assertEquals(0.0928,probs(3), 0.001)
	}

	/**The probability calculator may return negative probabilities for irrational prices. 
	 * It's a client responsibility to pass rational prices to the calculator and deal negative probabilities.*/
	@Test def calculateWinnerMarketPartiallySettledTwoWinners {	
		val marketPrices = Map(1l -> (1d,1d),2l -> (1d,1d),3l -> (5.9,6.5) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(1,probs(1), 0.001)
		assertEquals(1,probs(2), 0.001)
		assertEquals(-0.999,probs(3), 0.001)
	}
	@Test def calculateWinnerMarketPartiallySettledOneLoser {	
		val marketPrices = Map(1l -> (1.9d,2.5d),2l -> (Double.PositiveInfinity,Double.PositiveInfinity),3l -> (5.9d,6.5d) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(0.796,probs(1), 0.001)
		assertEquals(0,probs(2), 0.001)
		assertEquals(0.203,probs(3), 0.001)
	}
	
	/**The probability calculator may return NaN probabilities for irrational prices. 
	 * It's a client responsibility to pass rational prices to the calculator and deal negative probabilities.*/
	@Test def calculateWinnerMarketNoWinnersAllLosers {
		val marketPrices = Map(1l -> (Double.PositiveInfinity,Double.PositiveInfinity),2l -> (Double.PositiveInfinity,Double.PositiveInfinity),3l -> (Double.PositiveInfinity,Double.PositiveInfinity) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(Double.NaN,probs(1), 0.001)
		assertEquals(Double.NaN,probs(2), 0.001)
		assertEquals(Double.NaN,probs(3), 0.001)
	}

	@Test def calculateWinnerMarketAllWinnersNoLosers {
		val marketPrices = Map(1l -> (1d,1d),2l -> (1d,1d),3l -> (1d,1d) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(0.333,probs(1), 0.001)
		assertEquals(0.333,probs(2), 0.001)
		assertEquals(0.333,probs(3), 0.001)
	}
	@Test def calculateWinnerMarketPriceToBackIsNaN {
		val marketPrices = Map(1l -> (Double.NaN,2.1),2l -> (2.9,3.5),3l -> (5.9,6.5) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(0.549,probs(1), 0.001)
		assertEquals(0.294,probs(2), 0.001)
		assertEquals(0.156,probs(3), 0.001)
	}
	@Test def calculateWinnerMarketPriceToLayIsNaN {
			val marketPrices = Map(1l -> (1.9,Double.NaN),2l -> (2.9,3.5),3l -> (5.9,6.5) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(0.505,probs(1), 0.001)
		assertEquals(0.331,probs(2), 0.001)
		assertEquals(0.162,probs(3), 0.001)
	}
	@Test def calculateWinnerMarketToLayProbBiggerThanToBackProb {
			val marketPrices = Map(1l -> (2.6,2.5),2l -> (3.6,3.5),3l -> (6.6,6.5) )
		val probs = ProbabilityCalculator.calculate(marketPrices,1)

		assertEquals(3, probs.size)
		assertEquals(0.472,probs(1), 0.001)
		assertEquals(0.341,probs(2), 0.001)
		assertEquals(0.186,probs(3), 0.001)
	}

	/**
	 * Place market scenarios.
	 * */
	@Test @Ignore def calculatePlaceMarketNotSettled {fail("Not implemented")}
	@Test @Ignore def calculatePlaceMarketNotSettledToBackAndToLayPricesAreTheSame {fail("Not implemented")}
	@Test @Ignore def calculatePlaceMarketFullySettled {fail("Not implemented")}
	@Test @Ignore def calculatePlaceMarketFullySettledDeadHeat {fail("Not implemented")}
	@Test @Ignore def calculatePlaceMarketPartiallySettledOneLoser {fail("Not implemented")}
	@Test @Ignore def calculatePlaceMarketPartiallySettledOneWinner {fail("Not implemented")}
	@Test @Ignore def calculatePlaceMarketPartiallySettledTwoWinners {fail("Not implemented")}

	@Test @Ignore def calculatePlaceMarketNoWinnersAllLosers {fail("Not implemented")}
	@Test @Ignore def calculatePlaceMarketAllWinnersNoLosers {fail("Not implemented")}
	@Test @Ignore def calculatePlaceMarketPriceToBackIsNaN {fail("Not implemented")}
	@Test @Ignore def calculatePlaceMarketPriceToLayIsNaN {fail("Not implemented")}
	@Test @Ignore def calculatePlaceMarketToLayProbBiggerThanToBackProb {fail("Not implemented")}

}