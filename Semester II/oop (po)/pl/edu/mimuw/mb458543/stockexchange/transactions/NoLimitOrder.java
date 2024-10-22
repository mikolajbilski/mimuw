package pl.edu.mimuw.mb458543.stockexchange.transactions;

import pl.edu.mimuw.mb458543.stockexchange.investors.Investor;
import pl.edu.mimuw.mb458543.stockexchange.stocks.Stock;

public class NoLimitOrder extends TransactionOrder {

    public NoLimitOrder(Investor investor, TransactionOrderType type, Stock stock, int maxQuota, int priceLimit) {
        super(investor, type, stock, maxQuota, priceLimit);
    }

    @Override
    public boolean isExpired(int turnNumber) {
        return false;
    }
}