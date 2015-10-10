It's a temporary place for some ideas, they are moved then to [Issues](http://code.google.com/p/betting-ai/issues/list).

  1. MarketSimulator - consider leaving 1 cpu free, it's difficult to get to the box, which uses 100% of cpu.
  1. Optimise trader using kelly criterion (market level or global?).
  1. ITrader.init(ctx), ctx is used inside epn publisher, which is passed to ctx.registerEpn, it works only because trader context object is the same for the whole live cycle of a Trader, maybe it's better to create a new context every time trader.execute method is invoked, or change publisher(epn) to publisher(epn,ctx) to inject ctx to publisher?
  1. MarketService.placeBet. The placeBet method returns Bet object with userId = -1, either different object without userId should be returned, or calling bet.userId should throw UnsupportedOperationException.
  1. Bet.matchedDate was added and junit test is missing
  1. Review of MarketService
  1. Data collection - system clock is delayed
  1. MarketDataCollector crashed, analyse log.txt.backup file
  1. http://en.wikipedia.org/wiki/Automated_planning,
  1. http://en.wikipedia.org/wiki/STRIPS









