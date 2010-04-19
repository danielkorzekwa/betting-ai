package dk.bettingai.marketsimulator


/** Main class for the market simulator.
 * 
 * @author korzekwad
 *
 */
object SimulatorApp  {
	
  def main(args:Array[String]) {
  println("""Simulation is started.
Simulation progress: 1% 2% 3% 4% 5% 6%
.......................................................
.................................................................................................................
..................................100%
Simulation is finished in 0 sec.

Expected profit report for trader com.dk.bettingai.trader.SimpleTraderImpl:

Man Utd vs Arsenal: Match Odds expProfit=3 expAggrProfit=3  mBets=1 uBets=1
-------------------------------------------------------------------------------------
TotalExpectedProfit=3 TotalMatchedBets=1 TotalUnmachedBets=0
""")
    
  }
}
