package dk.bettingai.marketsimulator.risk

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.api.IBet.BetTypeEnum._
import scala.collection._
import dk.bettingai.marketsimulator.betex.api._
import scala.collection._

class ExpectedProfitCalculatorTest {

  /**Test scenarios for market expected profit.*/

  @Test
  def testCalculateNoBets {
    val bets = List[IBet]()
    val probs = Map(11l -> 1.5, 12l -> 3d)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probs, commission)
    assertEquals(0, expectedProfit.marketExpectedProfit, 0)
    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(0, expectedProfit.runnersIfWin(11), 0)
    assertEquals(0, expectedProfit.runnersIfWin(12), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probs, commission), expectedProfit)

  }

  @Test(expected = classOf[IllegalArgumentException])
  def testCalculateNoProbabilities {
    val bets = List(Bet(100, 123, 10, 2, BACK, 1, 11))
    val probabilities: Map[Long, Double] = Map()
    val commission = 0d
    ExpectedProfitCalculator.calculate(bets, probabilities, commission)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testCalculateWrongProbabilities {
    val bets = List(Bet(100, 123, 10, 2, BACK, 1, 11))
    val probabilities: Map[Long, Double] = Map(12l -> 2.1)
    ExpectedProfitCalculator.calculate(bets, probabilities, 0)
  }

  @Test(expected = classOf[IllegalArgumentException])
  def testCalculateBetsOnDifferentMarkets {
    val bets = List(Bet(100, 123, 10, 2, BACK, 1, 11), Bet(101, 123, 10, 2, BACK, 2, 11))
    val probabilities: Map[Long, Double] = Map(11l -> 2.1)
    ExpectedProfitCalculator.calculate(bets, probabilities, 0)

  }

  @Test
  def testCalculateOneBackBetProbabilityNotChanged {
    val bets = List(Bet(100, 123, 10, 3, BACK, 1, 11))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 3d, 12l -> 1 / 1.5d)
    val commission = 0d
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(0, expectedProfit.marketExpectedProfit, 0)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(20, expectedProfit.runnersIfWin(11), 0)
    assertEquals(-10, expectedProfit.runnersIfWin(12), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testCalculateOneBackBetProbabilityChanged1 {
    val bets = List(Bet(100, 123, 10, 3, BACK, 1, 11))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 4d, 12l -> 3d / 4d)
    val commission = 0d
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(-2.5, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(20, expectedProfit.runnersIfWin(11), 0)
    assertEquals(-10, expectedProfit.runnersIfWin(12), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }
  @Test
  def testCalculateOneBackBetProbabilityChanged2 {
    val bets = List(Bet(100, 123, 10, 3, BACK, 1, 11))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 1.5, 12l -> 1d / 3)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(10, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(20, expectedProfit.runnersIfWin(11), 0)
    assertEquals(-10, expectedProfit.runnersIfWin(12), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testCalculateOneLayBetProbabilityNotChanged {
    val bets = List(Bet(100, 123, 10, 2, LAY, 1, 11))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 2d, 12l -> 1d / 2d)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(0, expectedProfit.marketExpectedProfit, 0)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(-10, expectedProfit.runnersIfWin(11), 0)
    assertEquals(10, expectedProfit.runnersIfWin(12), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testCalculateOneLayBetProbabilityChanged1 {
    val bets = List(Bet(100, 123, 10, 2, LAY, 1, 11))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 3d, 12l -> 2d / 3d)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(3.333, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(-10, expectedProfit.runnersIfWin(11), 0)
    assertEquals(10, expectedProfit.runnersIfWin(12), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testCalculateOneLayBetProbabilityChanged2 {
    val bets = List(Bet(100, 123, 10, 2.5, LAY, 1, 11))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 1.5, 12l -> 1d / 3d)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(-6.666, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(-15, expectedProfit.runnersIfWin(11), 0)
    assertEquals(10, expectedProfit.runnersIfWin(12), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testCalculateTwoBackBetsOnTwoRunnersBetProbabilityNotChanged {
    val bets = List(Bet(100, 123, 10, 1.5, BACK, 1, 11), Bet(101, 123, 10, 3, BACK, 1, 12))
    val probabilities: Map[Long, Double] = Map(11l -> 1 / 1.5, 12l -> 1d / 3d, 13l -> 0d)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(0, expectedProfit.marketExpectedProfit, 0)

    assertEquals(3, expectedProfit.runnersIfWin.size)
    assertEquals(-5, expectedProfit.runnersIfWin(11), 0)
    assertEquals(10, expectedProfit.runnersIfWin(12), 0)
    assertEquals(-20, expectedProfit.runnersIfWin(13), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testCalculateTwoBackBetsOnTwoRunnersBetProbabilityChanged {
    val bets = List(Bet(100, 123, 10, 1.5, BACK, 1, 11), Bet(101, 123, 10, 3, BACK, 1, 12))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 3d, 12l -> 1 / 1.5)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(5, expectedProfit.marketExpectedProfit, 0)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(-5, expectedProfit.runnersIfWin(11), 0)
    assertEquals(10, expectedProfit.runnersIfWin(12), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testCalculateTwoLayBetsOnTwoRunnersBetProbabilityNotChanged {
    val bets = List(Bet(100, 123, 10, 1.5, LAY, 1, 11), Bet(101, 123, 10, 3, LAY, 1, 12))
    val probabilities: Map[Long, Double] = Map(11l -> 1 / 1.5, 12l -> 1d / 3d)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(0, expectedProfit.marketExpectedProfit, 0)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(5, expectedProfit.runnersIfWin(11), 0)
    assertEquals(-10, expectedProfit.runnersIfWin(12), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testCalculateTwoLayBetsOnTwoRunnersBetProbabilityChanged {
    val bets = List(Bet(100, 123, 10, 1.5, LAY, 1, 11), Bet(100, 123, 10, 3, LAY, 1, 12))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 3d, 12l -> 1 / 1.5)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(-5, expectedProfit.marketExpectedProfit, 0)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(5, expectedProfit.runnersIfWin(11), 0)
    assertEquals(-10, expectedProfit.runnersIfWin(12), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testCalculateTwoBackBetsAndTwoLayBetsOnThreeRunnersBetProbabilityNotChanged {
    val bets = List(Bet(100, 123, 10, 2, BACK, 1, 11), Bet(101, 123, 10, 3, BACK, 1, 12), Bet(100, 123, 10, 6, BACK, 1, 13), Bet(102, 123, 10, 2, LAY, 1, 11), Bet(100, 123, 10, 3, LAY, 1, 12), Bet(103, 123, 10, 6, LAY, 1, 13))
    val probabilities: Map[Long, Double] = Map(11l -> 1 / 2, 12l -> 1 / 3, 13l -> 1 / 6)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(0, expectedProfit.marketExpectedProfit, 0)

    assertEquals(3, expectedProfit.runnersIfWin.size)
    assertEquals(0, expectedProfit.runnersIfWin(11), 0)
    assertEquals(0, expectedProfit.runnersIfWin(12), 0)
    assertEquals(0, expectedProfit.runnersIfWin(13), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testCalculateTwoBackBetsAndTwoLayBetsOnTwoRunnersBetProbabilityChanged {
    val bets = List(Bet(100, 123, 10, 2, BACK, 1, 11), Bet(100, 123, 10, 3, BACK, 1, 12), Bet(101, 123, 10, 6, BACK, 1, 13), Bet(102, 123, 10, 2, LAY, 1, 11), Bet(103, 123, 10, 3, LAY, 1, 12), Bet(104, 123, 10, 6, LAY, 1, 13))
    val probabilities: Map[Long, Double] = Map(11l -> 1 / 6, 12l -> 1 / 3, 13l -> 1 / 2)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(0, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(3, expectedProfit.runnersIfWin.size)
    assertEquals(0, expectedProfit.runnersIfWin(11), 0)
    assertEquals(0, expectedProfit.runnersIfWin(12), 0)
    assertEquals(0, expectedProfit.runnersIfWin(13), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testCalculateALotOfBetsHedgesToZeroProbabilitiesChanged {
    val bets = List(
      Bet(100, 123, 10, 2, BACK, 1, 11),
      Bet(101, 123, 10, 3, BACK, 1, 12),
      Bet(102, 123, 10, 6, BACK, 1, 13),
      Bet(103, 123, 10, 2, LAY, 1, 11),
      Bet(104, 123, 10, 3, LAY, 1, 12),
      Bet(105, 123, 10, 6, LAY, 1, 13),
      Bet(106, 123, 13, 4, BACK, 1, 11),
      Bet(107, 123, 14, 5, BACK, 1, 12),
      Bet(108, 123, 15, 6, BACK, 1, 13),
      Bet(109, 123, 13, 4, LAY, 1, 11),
      Bet(110, 123, 14, 5, LAY, 1, 12),
      Bet(111, 123, 15, 6, LAY, 1, 13))
    val probabilities: Map[Long, Double] = Map(11l -> 1 / 6, 12l -> 1 / 3, 13l -> 1 / 2)
    val commission = 0
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(0, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(3, expectedProfit.runnersIfWin.size)
    assertEquals(0, expectedProfit.runnersIfWin(11), 0)
    assertEquals(0, expectedProfit.runnersIfWin(12), 0)
    assertEquals(0, expectedProfit.runnersIfWin(13), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  /**Test scenarios for wealth.*/
  @Test
  def testWealthOneBackBetProbabilityNotChanged {
    val bets = List(Bet(100, 123, 10, 3, BACK, 1, 11))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 3d, 12l -> 1 / 1.5d)
    val commission = 0d
    val expectedProfit = ExpectedProfitCalculator.wealth(bets, probabilities, commission, 100)
    assertEquals(-0.942, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(20, expectedProfit.runnersIfWin(11), 0)
    assertEquals(-10, expectedProfit.runnersIfWin(12), 0)

    /**Verify with wealth return by ExpectedProfitEngine.*/
    assertEquals(refWealth(bets, probabilities, commission, 100), expectedProfit)

    assertEquals(Double.NaN, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 9).marketExpectedProfit, 0.001)
    assertEquals(-10, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 10).marketExpectedProfit, 0.001)
    assertEquals(-0.099, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000).marketExpectedProfit, 0.001)
  }

  @Test
  def testWealthOneLayBetProbabilityNotChanged {
    val bets = List(Bet(100, 123, 10, 3, LAY, 1, 11))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 3d, 12l -> 1 / 1.5d)
    val commission = 0d
    val expectedProfit = ExpectedProfitCalculator.wealth(bets, probabilities, commission, 100)
    assertEquals(-1.078, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(-20, expectedProfit.runnersIfWin(11), 0)
    assertEquals(10, expectedProfit.runnersIfWin(12), 0)

    /**Verify with wealth return by ExpectedProfitEngine.*/
    assertEquals(refWealth(bets, probabilities, commission, 100), expectedProfit)

    assertEquals(Double.NaN, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 19).marketExpectedProfit, 0.001)
    assertEquals(-20, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 20).marketExpectedProfit, 0.001)
    assertEquals(-0.1, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000).marketExpectedProfit, 0.001)
  }

  @Test
  def testWealthOneBackBetProbabilityChanged1 {
    val bets = List(Bet(100, 123, 10, 5, BACK, 1, 13))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 2, 12l -> 1d / 3, 13l -> 1d / 6)
    val commission = 0d
    val expectedProfit = ExpectedProfitCalculator.wealth(bets, probabilities, commission, 1000)
    assertEquals(-1.836, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(3, expectedProfit.runnersIfWin.size)
    assertEquals(-10, expectedProfit.runnersIfWin(11), 0)
    assertEquals(-10, expectedProfit.runnersIfWin(12), 0)
    assertEquals(40, expectedProfit.runnersIfWin(13), 0)

    /**Verify with wealth return by ExpectedProfitEngine.*/
    assertEquals(refWealth(bets, probabilities, commission, 1000), expectedProfit)

    assertEquals(-10, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 10).marketExpectedProfit, 0.001)
    assertEquals(-2.456, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 200).marketExpectedProfit, 0.001)
    assertEquals(-2.079, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 400).marketExpectedProfit, 0.001)
    assertEquals(-2.000, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 500).marketExpectedProfit, 0.001)
    assertEquals(-1.878, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 800).marketExpectedProfit, 0.001)
    assertEquals(-1.836, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000).marketExpectedProfit, 0.001)
    assertEquals(-1.666, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000000).marketExpectedProfit, 0.001)
  }

  @Test
  def testWealthOneLayBetProbabilityChanged1 {
    val bets = List(Bet(100, 123, 10, 5, LAY, 1, 13))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 2, 12l -> 1d / 3, 13l -> 1d / 6)
    val commission = 0d
    val expectedProfit = ExpectedProfitCalculator.wealth(bets, probabilities, commission, 1000)
    assertEquals(1.489, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(3, expectedProfit.runnersIfWin.size)
    assertEquals(10, expectedProfit.runnersIfWin(11), 0)
    assertEquals(10, expectedProfit.runnersIfWin(12), 0)
    assertEquals(-40, expectedProfit.runnersIfWin(13), 0)

    /**Verify with wealth return by ExpectedProfitEngine.*/
    assertEquals(refWealth(bets, probabilities, commission, 1000), expectedProfit)

    assertEquals(-40, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 40).marketExpectedProfit, 0.001)
    assertEquals(1.304, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 500).marketExpectedProfit, 0.001)
    assertEquals(1.578, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 2000).marketExpectedProfit, 0.001)
    assertEquals(1.649, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 10000).marketExpectedProfit, 0.001)
    assertEquals(1.664, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 100000).marketExpectedProfit, 0.001)
    assertEquals(1.666, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000000).marketExpectedProfit, 0.001)
  }

  @Test
  def testWealthOneBackBetProbabilityChanged2 {
    val bets = List(Bet(100, 123, 100, 5, BACK, 1, 13))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 2, 12l -> 1d / 3, 13l -> 1d / 6)
    val commission = 0d
    val expectedProfit = ExpectedProfitCalculator.wealth(bets, probabilities, commission, 1000)
    assertEquals(-31.223, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(3, expectedProfit.runnersIfWin.size)
    assertEquals(-100, expectedProfit.runnersIfWin(11), 0)
    assertEquals(-100, expectedProfit.runnersIfWin(12), 0)
    assertEquals(400, expectedProfit.runnersIfWin(13), 0)

    /**Verify with wealth return by ExpectedProfitEngine.*/
    assertEquals(refWealth(bets, probabilities, commission, 1000), expectedProfit)

    assertEquals(-100, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 100).marketExpectedProfit, 0.001)
    assertEquals(-65.199, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 200).marketExpectedProfit, 0.001)
    assertEquals(-46.722, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 400).marketExpectedProfit, 0.001)
    assertEquals(-42.114, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 500).marketExpectedProfit, 0.001)
    assertEquals(-34.206, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 800).marketExpectedProfit, 0.001)
    assertEquals(-31.223, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000).marketExpectedProfit, 0.001)
    assertEquals(-24.562, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 2000).marketExpectedProfit, 0.001)
    assertEquals(-18.368, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 10000).marketExpectedProfit, 0.001)
    assertEquals(-16.839, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 100000).marketExpectedProfit, 0.001)
    assertEquals(-16.684, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000000).marketExpectedProfit, 0.001)
  }

  @Test
  def testWealthOneLayBetProbabilityChanged2 {
    val bets = List(Bet(100, 123, 100, 5, LAY, 1, 13))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 2, 12l -> 1d / 3, 13l -> 1d / 6)
    val commission = 0d
    val expectedProfit = ExpectedProfitCalculator.wealth(bets, probabilities, commission, 1000)
    assertEquals(-5.696, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(3, expectedProfit.runnersIfWin.size)
    assertEquals(100, expectedProfit.runnersIfWin(11), 0)
    assertEquals(100, expectedProfit.runnersIfWin(12), 0)
    assertEquals(-400, expectedProfit.runnersIfWin(13), 0)

    /**Verify with wealth return by ExpectedProfitEngine.*/
    assertEquals(refWealth(bets, probabilities, commission, 1000), expectedProfit)

    assertEquals(-400, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 400).marketExpectedProfit, 0.001)
    assertEquals(-54.898, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 500).marketExpectedProfit, 0.001)
    assertEquals(-13.777, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 800).marketExpectedProfit, 0.001)
    assertEquals(-5.696, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000).marketExpectedProfit, 0.001)
    assertEquals(6.947, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 2000).marketExpectedProfit, 0.001)
    assertEquals(14.893, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 10000).marketExpectedProfit, 0.001)
    assertEquals(16.492, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 100000).marketExpectedProfit, 0.001)
    assertEquals(16.649, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000000).marketExpectedProfit, 0.001)
  }

  @Test
  def testWealthOneBackBetProbabilityChanged3 {
    val bets = List(Bet(100, 123, 1000, 5, BACK, 1, 13))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 2, 12l -> 1d / 3, 13l -> 1d / 6)
    val commission = 0d
    val expectedProfit = ExpectedProfitCalculator.wealth(bets, probabilities, commission, 10000)
    assertEquals(-312.238, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(3, expectedProfit.runnersIfWin.size)
    assertEquals(-1000, expectedProfit.runnersIfWin(11), 0)
    assertEquals(-1000, expectedProfit.runnersIfWin(12), 0)
    assertEquals(4000, expectedProfit.runnersIfWin(13), 0)

    /**Verify with wealth return by ExpectedProfitEngine.*/
    assertEquals(refWealth(bets, probabilities, commission, 10000), expectedProfit)

    assertEquals(-1000, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000).marketExpectedProfit, 0.001)
    assertEquals(-651.993, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 2000).marketExpectedProfit, 0.001)
    assertEquals(-467.224, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 4000).marketExpectedProfit, 0.001)
    assertEquals(-421.143, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 5000).marketExpectedProfit, 0.001)
    assertEquals(-342.06, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 8000).marketExpectedProfit, 0.001)
    assertEquals(-312.238, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 10000).marketExpectedProfit, 0.001)
    assertEquals(-245.628, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 20000).marketExpectedProfit, 0.001)
    assertEquals(-183.680, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 100000).marketExpectedProfit, 0.001)
    assertEquals(-168.399, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000000).marketExpectedProfit, 0.001)
    assertEquals(-166.840, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 10000000).marketExpectedProfit, 0.001)
  }

  @Test
  def testWealthOneLayBetProbabilityChanged3 {
    val bets = List(Bet(100, 123, 1000, 5, LAY, 1, 13))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 2, 12l -> 1d / 3, 13l -> 1d / 6)
    val commission = 0d
    val expectedProfit = ExpectedProfitCalculator.wealth(bets, probabilities, commission, 10000)
    assertEquals(-56.961, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(3, expectedProfit.runnersIfWin.size)
    assertEquals(1000, expectedProfit.runnersIfWin(11), 0)
    assertEquals(1000, expectedProfit.runnersIfWin(12), 0)
    assertEquals(-4000, expectedProfit.runnersIfWin(13), 0)

    /**Verify with wealth return by ExpectedProfitEngine.*/
    assertEquals(refWealth(bets, probabilities, commission, 10000), expectedProfit)

    assertEquals(-4000, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 4000).marketExpectedProfit, 0.001)
    assertEquals(-548.981, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 5000).marketExpectedProfit, 0.001)
    assertEquals(-137.775, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 8000).marketExpectedProfit, 0.001)
    assertEquals(-56.961, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 10000).marketExpectedProfit, 0.001)
    assertEquals(69.477, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 20000).marketExpectedProfit, 0.001)
    assertEquals(148.938, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 100000).marketExpectedProfit, 0.001)
    assertEquals(164.926, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 1000000).marketExpectedProfit, 0.001)
    assertEquals(166.493, ExpectedProfitCalculator.wealth(bets, probabilities, 0, 10000000).marketExpectedProfit, 0.001)
  }

  /**Tests against commission.*/
  @Test
  def testCalculateOneBackBetProbabilityNotChangedWithCommission {
    val bets = List(Bet(100, 123, 100, 2, BACK, 1, 11))
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 2d, 12l -> 1 / 2d)
    val commission = 0.03
    val expectedProfit = ExpectedProfitCalculator.calculate(bets, probabilities, commission)
    assertEquals(-1.5, expectedProfit.marketExpectedProfit, 0)

    assertEquals(2, expectedProfit.runnersIfWin.size)
    assertEquals(97.0, expectedProfit.runnersIfWin(11), 0)
    assertEquals(-100, expectedProfit.runnersIfWin(12), 0)

    /**Verify with market expected profit return by ExpectedProfitEngine.*/
    assertEquals(refExpectedProfit(bets, probabilities, commission), expectedProfit)
  }

  @Test
  def testWealthOneBackBetProbabilityChanged1CommissionChanged {
    val bets = List(Bet(100, 123, 10, 5, BACK, 1, 13))
    val commission = 0.05
    val probabilities: Map[Long, Double] = Map(11l -> 1d / 2, 12l -> 1d / 3, 13l -> 1d / 6)
    val expectedProfit = ExpectedProfitCalculator.wealth(bets, probabilities, commission, 1000)
    assertEquals(-2.156, expectedProfit.marketExpectedProfit, 0.001)

    assertEquals(3, expectedProfit.runnersIfWin.size)
    assertEquals(-10, expectedProfit.runnersIfWin(11), 0)
    assertEquals(-10, expectedProfit.runnersIfWin(12), 0)
    assertEquals(38, expectedProfit.runnersIfWin(13), 0)

    /**Verify with wealth return by ExpectedProfitEngine.*/
    assertEquals(refWealth(bets, probabilities, commission, 1000), expectedProfit)

    assertEquals(-10, ExpectedProfitCalculator.wealth(bets, probabilities, commission, 10).marketExpectedProfit, 0.001)
    assertEquals(-2.731, ExpectedProfitCalculator.wealth(bets, probabilities, commission, 200).marketExpectedProfit, 0.001)
    assertEquals(-2.381, ExpectedProfitCalculator.wealth(bets, probabilities, commission, 400).marketExpectedProfit, 0.001)
    assertEquals(-2.308, ExpectedProfitCalculator.wealth(bets, probabilities, commission, 500).marketExpectedProfit, 0.001)
    assertEquals(-2.195, ExpectedProfitCalculator.wealth(bets, probabilities, commission, 800).marketExpectedProfit, 0.001)
    assertEquals(-2.156, ExpectedProfitCalculator.wealth(bets, probabilities, commission, 1000).marketExpectedProfit, 0.001)
    assertEquals(-2, ExpectedProfitCalculator.wealth(bets, probabilities, commission, 1000000).marketExpectedProfit, 0.001)
  }
  /**Calculates market expected profit using ExpectedProfitEngine.*/
  private def refExpectedProfit(bets: List[IBet], probabilities: Map[Long, Double], commission: Double): MarketExpectedProfit = {
    val engine = ExpectedProfitEngine()
    bets.foreach(b => engine.addBet(b.betSize, b.betPrice, b.betType, b.runnerId))
    engine.calculateExpectedProfit(probabilities, commission)
  }

  /**Calculates wealth using ExpectedProfitEngine.*/
  private def refWealth(bets: List[IBet], probabilities: Map[Long, Double], commission: Double, bank: Double): MarketExpectedProfit = {
    val engine = ExpectedProfitEngine()
    bets.foreach(b => engine.addBet(b.betSize, b.betPrice, b.betType, b.runnerId))
    engine.calculateWealth(probabilities, commission, bank)
  }
}