package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._
import HillClimbing._

/** This trait can be mixed into HillClimbing implementation to provide local optimisation after a new trader with positive profit is bred.
 * 
 * Use case: Random hill climbing algorithm could be mixed with this trait to provide local optimisation for all randomly found traders with positive profit.
 * 
 * @author korzekwad
 *
 */
trait LocalOptimisation[T <: ITrader] extends HillClimbing[T] {

  /**Optimises trader solution every time new trader with positive profit is bred. 
   * This is an abstract function and must be implemented by a concrete implementation of HillClimbing algorithm.
   * 
   * @param solution
   * @return Optimised solution.
   */
  def optimise(solution: Solution[T]): Solution[T]

  /**Calls super.breed and then optimises returned trader solution if its expected profit is positive.
   * 
   * @param trader The current best solution (a parent used to born a new child)
   * @return Optimised trader
   * 
   */
  abstract override def breed(trader: Solution[T]): Solution[T] = {
    val child = super.breed(trader)
    val optimisedChild = if (child.expectedProfit > 0) optimise(child) else child
    optimisedChild
  }
}