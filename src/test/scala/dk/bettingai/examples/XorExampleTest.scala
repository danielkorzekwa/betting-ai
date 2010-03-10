package dk.bettingai.examples

import org.junit._
import Assert._
import org.encog.neural.networks._
import org.encog.neural.activation._
import org.encog.neural.networks.layers._
import org.encog.neural.data.basic._
import org.encog.neural.networks.training.propagation.resilient._
import org.encog.neural.data._
import java.util._


class XorExampleTest {

  private val xorInput = new Array[Array[Double]](4,2)
  private val xorIdeal = new Array[Array[Double]](4,2)
  private val network  = new BasicNetwork() 
  
  @Before def setUp = {	  
	xorInput(0)(0) = 0
    xorInput(0)(1) = 0
    xorInput(1)(0) = 1
	xorInput(1)(1) = 0
	xorInput(2)(0) = 0
	xorInput(2)(1) = 1
	xorInput(3)(0) = 1
	xorInput(3)(1) = 1
			
	xorIdeal(0)(0) = 0
	xorIdeal(1)(0) = 1
	xorIdeal(2)(0) = 1
	xorIdeal(3)(0) = 0

	network.addLayer(new BasicLayer(2))
	network.addLayer(new BasicLayer(4))
	network.addLayer(new BasicLayer(1))
    network.getStructure().finalizeStructure()
    network.reset()
  }
	
  @Test def test() = {
	val trainingSet:BasicNeuralDataSet = new BasicNeuralDataSet(xorInput,xorIdeal)
	val train = new ResilientPropagation(network,trainingSet)
	
	/**Train*/
	var iteration = 1
	do {
	  train.iteration()
	  println("iteration: " + iteration + " Error: " + train.getError)	
	  iteration+=1
	} while (train.getError>0.005)
		
	/**Execute*/
	trainingSet.getData().toArray().foreach(data => computeAndAssert(data.asInstanceOf[NeuralDataPair]))
  }
  
  def computeAndAssert(dataPair:NeuralDataPair)= {
    val result:NeuralData = network.compute(dataPair.getIdeal)
    println(dataPair.getInput().getData(0) + "," +dataPair.getInput().getData(0) + ", actual=" + + result.getData(0) + " ,ideal=" + dataPair.getIdeal().getData(0))
    assertTrue("Error is bigger than 0.01",(dataPair.getIdeal().getData(0)-result.getData(0)).abs<=0.02)
  }
}
