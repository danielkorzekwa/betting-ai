***********************************************************************************
*Betting AI Copyright 2010 Daniel Korzekwa(http://danmachine.com)                 *      
*Project homepage: http://code.google.com/p/betting-ai/                           *
*Licenced under Apache License 2.0(http://www.apache.org/licenses/LICENSE-2.0)    *
***********************************************************************************

Betting AI version 1.3 (RELEASED on 04 March 2011)

Integrate market simulator with complex event processing (Esper) to calculate some metrics, e.g. derivatives or to recognize patterns
Proof of concept. Create and test trading strategies adopting meta-heuristics (http://www.goodreads.com/book/show/9734814-essentials-of-metaheuristics)
Create trading-optimiser component that allows for optimising trading strategies using co-evolution hill climbing gradient algorithm.
Improve performance of Betting Exchange Engine.
Improve performance of market simulation (utilise multiple cpu cores).
Improve performance of risk calculator.
Add Wealth (Kelly Criterion http://en.wikipedia.org/wiki/Kelly_criterion)
Enable market simulations for multiple traders, e.g. two traders can be analysed by competing against each other on a replayed betfair market.

-----------------------------------------------------------------------------------
Betting AI version 1.2 (RELEASED on 24 November 2010)

Goal 1 - Run market simulation for simple AI trader and real market data.
Goal 2 - Improve performance of market simulation.
Goal 3 - Code review of releases 1.0,1.1,1.2, refactoring.
Goal 4 - Add more market/bets data to runner context, e.g. traded volume, runner
prices,etc.

Goal 5 - Generate html reports for market simulation with some useful graphs.

-----------------------------------------------------------------------------------
Betting AI version 1.1 (RELEASED on 23 July 2010)

Goal - Run market simulation for simple trader implementation (place bet if price>2) and real market data.

-----------------------------------------------------------------------------------
Betting AI version 1.0 ( RELEASED on 16 June 2010)

Goal - Proof of concept. Run market simulation for very simple market data that contains a few events to create market and place/cancel bets. Simulation will be executed for simple trader implementation that places a bet if price is bigger than 2.
Scope - http://code.google.com/p/betting-ai/issues/list?can=1&q=milestone%3DRelease1.0&colspec=ID+Type+Status+Priority+Complexity+Milestone+Owner+Component+Summary&cells=tiles

-----------------------------------------------------------------------------------
ROADMAP - http://code.google.com/p/betting-ai/wiki/Roadmap

-----------------------------------------------------------------------------------
FAQ

1)How to start market simulator from command line?
simulator marketDataDir=./marketdata traderImpl=dk.bettingai.marketsimulator.trader.SimpleTrader

2)How to colelct market data?
Go to http://code.google.com/p/betting-exchange-event-collector

3) How to run trading optimisation from unit tests?
Look at those two unit tests:
- /betting-ai/trading-optimiser/src/test/scala/dk/bettingai/tradingoptimiser/CoevolutionHillClimbingTest.scala
- /trader-examples/src/test/scala/dk/bettingai/trader/pricepriceslope/PricePriceSlopeTraderRandomTest.scala
