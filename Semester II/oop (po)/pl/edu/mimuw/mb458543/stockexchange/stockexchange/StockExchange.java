package pl.edu.mimuw.mb458543.stockexchange.stockexchange;

import pl.edu.mimuw.mb458543.stockexchange.investors.Investor;
import pl.edu.mimuw.mb458543.stockexchange.stocks.Stock;
import pl.edu.mimuw.mb458543.stockexchange.transactions.TransactionOrder;

import java.util.Map;

public class StockExchange {
    private Map<Stock, Integer> stockPrices;
    private final TransactionSystem transactionSystem;
    private int turnNumber;
    private final int maxPriceDifference;

    public StockExchange(Map<Stock, Integer> stocks) {
        this.stockPrices = stocks;
        this.transactionSystem = new TransactionSystem(stocks);
        maxPriceDifference = 10;
    }

    public void processTransactions() {
        //System.out.println("Turn " + turnNumber + ":");
        stockPrices = transactionSystem.processTransactions(turnNumber++);
    }

    public void placeTransactionOrder(TransactionOrder order) {
        // Validate transaction
        if(order.getPriceLimit() < 1 || order.getMaxQuota() < 1) {
            return;
        }
        Stock stock = order.getStock();
        if(order.getPriceLimit() < stockPrices.get(stock) - maxPriceDifference || order.getPriceLimit() > stockPrices.get(stock) + maxPriceDifference) {
            return;
        }
        Investor investor = order.getInvestor();
        if(!investor.canHandleTransaction(order.getType(), stock, order.getPriceLimit(), order.getMaxQuota())) {
            return;
        }

        transactionSystem.addTransaction(order, turnNumber);
    }

    public int getStockPrice(Stock stock) {
        return stockPrices.get(stock);
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public int getMaxPriceDifference() {
        return maxPriceDifference;
    }
}
