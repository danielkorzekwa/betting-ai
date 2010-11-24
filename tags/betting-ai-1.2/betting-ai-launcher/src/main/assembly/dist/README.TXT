***********************************************************************************
*Betting AI Copyright 2010 Daniel Korzekwa(http://danmachine.com)                 *      
*Project homepage: http://code.google.com/p/betting-ai/                           *
*Licenced under Apache License 2.0(http://www.apache.org/licenses/LICENSE-2.0)    *
***********************************************************************************

Betting AI ver. 1.2, November 2010 (http://code.google.com/p/betting-ai/issues/list?cursor=55&q=milestone%3DRelease1.2&updated=55&ts=1290596093&can=1)
- Market simulation for many markets.
- Generate html reports for market simulation with some useful graphs.
- Improved performance of market simulation.
- Extending TraderContext with
	- fillBet
	- cancelBet
	- placeHedgeBets
    - tradedVolume
    - risk metrics
- Adding some trader examples, e.g. how to calculate derivatives with Esper complex event processing

Betting AI ver. 1.1
- Market Data Collector was added - allows for collecting real market data from betfair betting exchange.
------------------------------------------------------------------------------------
FAQ

1)How to start market simulator?
simulator marketDataDir=./marketdata traderImpl=dk.bettingai.marketsimulator.trader.SimpleTrader

2)How to start market data collector?
marketdatacollector marketDataDir=./marketdata bfUser=john bfPassword=green bfProductId=82 collectionInterval=5 discoveryInterval=300 startInMinutesFrom=-60 startInMinutesTo=300