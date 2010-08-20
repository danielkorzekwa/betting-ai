package dk.bettingai.marketsimulator.betex

import org.junit._
import Assert._
import PriceUtil.PriceRoundEnum._

class PriceUtilTest {

	@Test def testValidate {

		val validate = PriceUtil.validate(PriceUtil.getPriceRanges) _

		assertEquals(1.64,validate(1.6317991631799162, ROUND_UP), 0);
		assertEquals(1.63, validate(1.6317991631799162, ROUND_DOWN), 0);

		assertEquals(1.88, validate(1.88, ROUND_UP), 0);
		assertEquals(1.88, validate(1.88, ROUND_DOWN), 0);

		assertEquals(4.9,validate(4.8 + 0.01,ROUND_UP),0)
		assertEquals(1.01, validate(0.5, ROUND_UP), 0);
		assertEquals(1.01, validate(0.5, ROUND_DOWN), 0);

		assertEquals(1000, validate(2000, ROUND_UP), 0);
		assertEquals(1000, validate(2000, ROUND_DOWN), 0);

		assertEquals(1.01, validate(1.01, ROUND_UP), 0);
		assertEquals(1.01, validate(1.01, ROUND_DOWN), 0);

		assertEquals(1000, validate(1000, ROUND_UP), 0);
		assertEquals(1000, validate(1000, ROUND_DOWN), 0);

		assertEquals(2.0, validate(2.0, ROUND_UP), 0);
		assertEquals(2.0, validate(2.0, ROUND_DOWN), 0);

		assertEquals(2.02, validate(2.01, ROUND_UP), 0);
		assertEquals(2.0, validate(2.01, ROUND_DOWN), 0);

		assertEquals(44, validate(43, ROUND_UP), 0);
		assertEquals(44, validate(44, ROUND_UP), 0);
		assertEquals(46, validate(45, ROUND_UP), 0);

		assertEquals(860, validate(856, ROUND_UP), 0);

		assertEquals(1000, validate(999.99, ROUND_UP), 0);
		assertEquals(990, validate(999.99, ROUND_DOWN), 0);
	}

}