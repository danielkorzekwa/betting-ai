Betting AI version 1.0 ( RELEASED on 16 June 2010)

  * Goal - Proof of concept. Run market simulation for very simple market data that contains a few events to create market and place/cancel bets. Simulation will be executed for simple trader implementation that places a bet if price is bigger than 2.

  * Scope - http://code.google.com/p/betting-ai/issues/list?can=1&q=milestone%3DRelease1.0&colspec=ID+Type+Status+Priority+Complexity+Milestone+Owner+Component+Summary&cells=tiles


---


Betting AI version 1.1 (RELEASED on 23 July 2010)

  * Goal - Run market simulation for simple trader implementation (place bet if price>2) and real market data.


---


Betting AI version 1.2 (RELEASED on 24 November 2010)

  * Goal 1 - Run market simulation for simple AI trader and real market data.
  * Goal 2 - Improve performance of market simulation.
  * Goal 3 - Code review of releases 1.0,1.1,1.2, refactoring.
  * Goal 4 - Add more market/bets data to runner context, e.g. traded volume, runner
prices,etc.
  * Goal 5 - Generate html reports for market simulation with some useful graphs.


---


Betting AI version 1.3 (RELEASED on 4 March 2011)
  * Integrate market simulator with complex event processing (Esper) to calculate some metrics, e.g. derivatives or to recognize patterns
  * Proof of concept. Create and test trading strategies adopting meta-heuristics (http://www.goodreads.com/book/show/9734814-essentials-of-metaheuristics)
  * Create trading-optimiser component that allows for optimising trading strategies using co-evolution hill climbing gradient algorithm.
  * Improve performance of Betting Exchange Engine.
  * Improve performance of market simulation (utilise multiple cpu cores).
  * Improve performance of risk calculator.
  * Add Wealth (Kelly Criterion http://en.wikipedia.org/wiki/Kelly_criterion)
  * Enable market simulations for multiple traders, e.g. two traders can be analysed by competing against each other on a replayed betfair market.


---


Betting AI version 1.4 (release planned on 30 July 2011)
  * More metaheuristic optimisation, evolution algorithms, genetic programming, cooperative evolution, graph based trading optimisation. [Issue 99](http://code.google.com/p/betting-ai/issues/detail?id=99)
  * Improve performance CPU/mem of market simulation. [Issue 98](http://code.google.com/p/betting-ai/issues/detail?id=98), [Issue 97](http://code.google.com/p/betting-ai/issues/detail?id=97)
  * Refactoring of Betting Exchange - Introduce abstract types, e.g. replace BetTypeEnum (BACK, LAY) with specialised classes BackBet and LayBet [Issue 87](http://code.google.com/p/betting-ai/issues/detail?id=87)
  * Simplify using of Complex Event Processing inside of Traders, e.g. add some dsl functions for delta or slope. [Issue 83](http://code.google.com/p/betting-ai/issues/detail?id=83)
  * Standalone application for running trading strategy on a real betting exchange. Input arguments: Trader Implementation, betfair market id, market polling interval in seconds.[Issue 100](http://code.google.com/p/betting-ai/issues/detail?id=100)
  * Proof of concept. Create and test trading strategies adopting neural networks. (http://www.goodreads.com/book/show/391010.Introduction_to_Neural_Networks_with_Java) [Issue 64](http://code.google.com/p/betting-ai/issues/detail?id=64)
  * Compare efficiency of trading strategies between meta-heuristics and simple neural network (back propagation)

---


Future

  * Visualisation of trader optimisation.
  * Web interface to Market Simulator.
  * Collaborative trading in a cloud, many traders can collaboratively   trade.
