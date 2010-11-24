package dk.bettingai.marketcollector


import org.junit._
import Assert._

class UserInputParserTest {

	@Test(expected=classOf[IllegalArgumentException])
	def testWrongNumberOfInputParameters { UserInputParser.parse(Array("incorrect parameters")) }

	@Test
	def testCorrectInputParameters { 
		val inputArgs = Array("marketDataDir=./target/marketeventsdata","bfUser=john","bfPassword=mule","bfProductId=82","collectionInterval=4","discoveryInterval=60","startInMinutesFrom=-60","startInMinutesTo=12")
		val argsMap = UserInputParser.parse(inputArgs) 

		assertEquals("./target/marketeventsdata",argsMap("marketDataDir"))
		assertEquals("john",argsMap("bfUser"))
		assertEquals("mule",argsMap("bfPassword"))
		assertEquals("82",argsMap("bfProductId"))
		assertEquals("4",argsMap("collectionInterval"))
		assertEquals("60",argsMap("discoveryInterval"))
		assertEquals("-60",argsMap("startInMinutesFrom"))
		assertEquals("12",argsMap("startInMinutesTo"))
	}

}