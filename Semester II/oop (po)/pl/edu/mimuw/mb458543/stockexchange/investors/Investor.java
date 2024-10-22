package pl.edu.mimuw.mb458543.stockexchange.investors;

import pl.edu.mimuw.mb458543.stockexchange.stockexchange.StockExchange;
import pl.edu.mimuw.mb458543.stockexchange.stocks.Stock;
import pl.edu.mimuw.mb458543.stockexchange.transactions.TransactionOrderType;

public abstract class Investor {
    protected final StockExchange stockExchange;
    protected final Portfolio portfolio;
    protected int money;
    protected String name;

    public Investor(StockExchange stockExchange, Portfolio portfolio, int money) {
        this.stockExchange = stockExchange;
        this.portfolio = portfolio;
        this.money = money;
        name = "Investor";
    }

    public boolean canHandleTransaction(TransactionOrderType type, Stock stock, int price, int amount) {
        if (type == TransactionOrderType.BUY) {
            return money >= price * amount;
        } else {
            return getStockAmount(stock) >= amount;
        }
    }

    public void trade(TransactionOrderType type, Stock stock, int price, int amount) {
        int stockAmountDifference = amount * (type == TransactionOrderType.BUY ? 1 : -1);
        int moneyDifference = -price * stockAmountDifference;
        portfolio.changeStockAmount(stock, stockAmountDifference);
        money += moneyDifference;
    }

    // Make investment decision
    // It is investor's responsibility to place order at the stock exchange
    public abstract void makeDecision();

    public int getStockAmount(Stock stock) {
        return portfolio.getStockAmount(stock);
    }

    private int estimatedNetWorth() {
        int netWorth = money;
        for(Stock stock : portfolio.getStocks()) {
            netWorth += getStockAmount(stock) * stockExchange.getStockPrice(stock);
        }
        return netWorth;
    }

    @Override
    public String toString() {
        return name + ": " + money + " " + portfolio + ", estimated net worth: " + estimatedNetWorth();
    }
}
