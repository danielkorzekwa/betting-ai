***********************************************************************************
*Betting AI Copyright 2010 Daniel Korzekwa(http://danmachine.com)                 *      
*Project homepage: http://code.google.com/p/betting-ai/                           *
*Licenced under Apache License 2.0(http://www.apache.org/licenses/LICENSE-2.0)    *
***********************************************************************************

Betting AI ver. 1.2
- Market simulator for many markets.

Betting AI ver. 1.1

What's new?
- Market Data Collector.
------------------------------------------------------------------------------------
FAQ

1)How to start market simulator?
simulator marketDataDir=./marketdata traderImpl=dk.bettingai.marketsimulator.trader.SimpleTrader

2)How to start market data collector?
marketdatacollector marketDataDir=./marketdata bfUser=john bfPassword=green bfProductId=82 collectionInterval=5 discoveryInterval=300 startInMinutesFrom=-60 startInMinutesTo=300