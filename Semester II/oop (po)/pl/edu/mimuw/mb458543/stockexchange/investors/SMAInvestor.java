package pl.edu.mimuw.mb458543.stockexchange.investors;

import pl.edu.mimuw.mb458543.stockexchange.stockexchange.StockExchange;
import pl.edu.mimuw.mb458543.stockexchange.stocks.Stock;
import pl.edu.mimuw.mb458543.stockexchange.transactions.TimeLimitedOrder;
import pl.edu.mimuw.mb458543.stockexchange.transactions.TransactionOrder;
import pl.edu.mimuw.mb458543.stockexchange.transactions.TransactionOrderType;

import java.util.HashMap;
import java.util.Map;

public class SMAInvestor extends Investor {
    private final Map<Stock, SMASignalGenerator> smaSignalGenerators;

    public SMAInvestor(StockExchange exchange, Portfolio portfolio, int money) {
        super(exchange, portfolio, money);
        smaSignalGenerators = new HashMap<>();
        for(Stock stock : portfolio.getStocks()) {
            smaSignalGenerators.put(stock, new SMASignalGenerator(5, 10));
        }
        name = "SMA Investor";
    }

    @Override
    public void makeDecision() {
        double strongestSignal = 0;
        Stock bestStock = null;
        // Choose best stock to buy/sell
        for (Map.Entry<Stock, SMASignalGenerator> entry : smaSignalGenerators.entrySet()) {
            int newPrice = stockExchange.getStockPrice(entry.getKey());
            double smaSignal = entry.getValue().checkForSignal(newPrice);
            if(stockExchange.getTurnNumber() < 10) {
                continue;
            }
            // Do not decide to sell if you don't have any stock
            if(strongestSignal < 0 && getStockAmount(entry.getKey()) == 0) {
                continue;
            }
            if(Math.abs(smaSignal) > Math.abs(strongestSignal)) {
                bestStock = entry.getKey();
                strongestSignal = smaSignal;
            }
        }
        if(bestStock == null) {
            return;
        }

        TransactionOrderType buyOrSell = strongestSignal > 0 ? TransactionOrderType.BUY : TransactionOrderType.SELL;
        int price = stockExchange.getStockPrice(bestStock);
        int maxPossibleQuota = buyOrSell == TransactionOrderType.BUY ? money / price : getStockAmount(bestStock);
        double transactionFraction = Math.min((Math.abs(strongestSignal) - 0.5) * 100 + 0.5, 1);
        int actualQuota = Math.max(1, (int) (transactionFraction * maxPossibleQuota));
        int timeLimit = stockExchange.getTurnNumber() + 5;
        TransactionOrder order = new TimeLimitedOrder(this, buyOrSell, bestStock, actualQuota, price, timeLimit);
        stockExchange.placeTransactionOrder(order);
    }
}
