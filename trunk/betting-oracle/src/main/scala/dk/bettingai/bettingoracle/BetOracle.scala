package dk.bettingai.bettingoracle

import org.encog.neural.networks._
import org.encog.neural.networks.layers._
import org.encog.neural.data.basic._
import org.encog.neural.data._
import org.encog.neural.networks.training.propagation.resilient._
import org.encog.neural.networks.logic._

/**BetOracle takes decision whether or not the bet should be placed. 
 * This decision is based on a set of binary inputs, e.g. current price, totalToBack, totalToLay, etc. 
 * Each input value has to be normalised as a value between 0(inclusive) and 1(inclusive). 
 * BetOracle uses a feed forward neural and resilient propagation training for learning process. 
 * It returns one integer value: 1 or -1. 1 - back bet should be placed, 0 lay bet should be placed.
 * 
 * @author korzekwad
 * @param inputs Number of binary inputs that are provided to the BetOracle for bet placement decision.
 */

class BetOracle(inputs:Int) {

	private val network = new BasicNetwork(new FeedforwardLogic())
	network.addLayer(new BasicLayer(inputs))
	network.addLayer(new BasicLayer(inputs*2))
	network.addLayer(new BasicLayer(1))
	network.getStructure().finalizeStructure()
	network.reset()

	private val train = new ResilientPropagation(network,new BasicNeuralDataSet())
	private var dataSet = new BasicNeuralDataSet()
	
  /** Calculates bet placement decision. Either back or lay bet should be placed.
   * 
   * @param inputs Values of the binary inputs that the bet placement decision is taken for.
   * @return 1 or 1. 1 - back bet should be placed, -1 lay bet should be placed.
   */
  def compute(inputs: Array[Double]):Double = {	
    val output:NeuralData = network.compute(new BasicNeuralData(inputs))
    
    if(output.getData(0)>0) 1 else -1
  }
	
	
  /** Train the BetOracle what was the correct bet placement decision for the given set of inputs.
   *
   * @param inputs Values of binary inputs that the bet placement decision was taken for.
   * @param output Correct bet placement decision that should be taken for that bet. It should take values 1 or 0.
   *        1 - placing back bet was the correct decision, -1 - placing lay bet was the correct decision.
   */
  def train(inputs: Array[Double], output: Double) {
	dataSet.add(new BasicNeuralData(inputs), new BasicNeuralData(Array(output)))
    train.setTraining(dataSet);
    train.iteration() 
  }
  
  /** 
   * @return Get the current error percent from the training. Between 0 and 1.
   */
  def getError():Double = {
	 train.getError()
  }

}
