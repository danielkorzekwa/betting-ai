package dk.bettingai

import org.junit._
import Assert._

class BetOracleTest {

  @Test def testOneInput {
    val betOracle = new BetOracle(1)
	  
	val inputs = Array[Double](0)
	val idealOutput = 1
    var iter=0
	do {
	  println("iter=" + iter + " input=" + inputs.toList.mkString(",") + " output=" + betOracle.compute(inputs) + " ideal=" + idealOutput + " error=" + betOracle.getError())
	  betOracle.train(inputs, idealOutput)
	  iter+=1
	}
	while(iter<1000 && betOracle.getError>0.01)
    	
    assertEquals(idealOutput,betOracle.compute(inputs),0)
    assertTrue("Error should be:  0 < e <= 0.01",betOracle.getError()>0 && betOracle.getError()<=0.01)
  }
   
  @Test def testTwoInputs {
    val betOracle = new BetOracle(2)
	  
	val xorInput = new Array[Array[Double]](4)
    val xorIdeal = Array[Double](-1,1,1,-1)
	   
    xorInput(0) = Array(0,0)
    xorInput(1) = Array(1,0)
	xorInput(2) = Array(0,1)
	xorInput(3) = Array(1,1)
	
    var iter=0
	do {
	  computeAndLearn(betOracle, xorInput, xorIdeal)
      iter+=1
	}
	while(iter<2000 && betOracle.getError>0.01) 
	
    /**Assert if BetOracle returns correct values*/
	assertBetOracle(betOracle, xorInput, xorIdeal)
    assertTrue("Error should be:  0 < e <= 0.01",betOracle.getError()>0 && betOracle.getError()<=0.01)
  }
   
  @Test def testThreeInputs {
    val betOracle = new BetOracle(3)
	  
	val xorInput = new Array[Array[Double]](8)
    val xorIdeal = Array[Double](1,-1,1,-1,1,1,-1,1)
	   
    xorInput(0) = Array(0,0,0)
    xorInput(1) = Array(0,0,1)
	xorInput(2) = Array(0,1,0)
	xorInput(3) = Array(0,1,1)
	xorInput(4) = Array(1,0,0)
    xorInput(5) = Array(1,0,1)
	xorInput(6) = Array(1,1,0)
	xorInput(7) = Array(1,1,1)
	
    var iter=0
	do {
	  computeAndLearn(betOracle, xorInput, xorIdeal)
      iter+=1
	}
	while(iter<2000 && betOracle.getError>0.01) 
	
    /**Assert if BetOracle returns correct values*/
	assertBetOracle(betOracle, xorInput, xorIdeal)
    assertTrue("Error should be:  0 < e <= 0.01",betOracle.getError()>0 && betOracle.getError()<=0.01)
  }
  
  private def computeAndLearn(betOracle:BetOracle,xorInput:Array[Array[Double]],xorIdeal:Array[Double]) {
    for (i <- 0 until xorInput.length) {
      println("input=" + xorInput(i).mkString(",") + " output=" + betOracle.compute(xorInput(i)) + " ideal=" + xorIdeal(i) + " error=" + betOracle.getError()) 
	  betOracle.train(xorInput(i),xorIdeal(i))
    }	
  }
   /**Assert if BetOracle returns correct values*/
  private def assertBetOracle(betOracle:BetOracle,xorInput:Array[Array[Double]],xorIdeal:Array[Double]) {
    for (i <- 0 until xorInput.length) {
	  assertEquals(xorIdeal(i),betOracle.compute(xorInput(i)),0)
    }
  }
}
