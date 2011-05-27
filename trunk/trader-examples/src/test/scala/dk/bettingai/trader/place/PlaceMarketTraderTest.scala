package dk.bettingai.trader.place

import org.junit._
import Assert._

/**
 * Run trader implementation.
 *
 * @author korzekwad
 *
 */
class PlaceMarketTraderTest {

  @Test
  def test {
    val traderClass = classOf[PlaceMarketTrader]
    val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName, "marketDataDir=./src/test/resources/one_hr_win_place_10mins_before_inplay", "traderImpl=" + traderClass.getName)
    dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)
  }
}