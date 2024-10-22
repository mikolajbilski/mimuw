package pl.edu.mimuw.mb458543.stockexchange.transactions;

import pl.edu.mimuw.mb458543.stockexchange.investors.Investor;
import pl.edu.mimuw.mb458543.stockexchange.stocks.Stock;

public class TimeLimitedOrder extends TransactionOrder {
    private final int timeLimit;

    public TimeLimitedOrder(Investor investor, TransactionOrderType type, Stock stock, int maxQuota, int priceLimit, int timeLimit) {
        super(investor, type, stock, maxQuota, priceLimit);
        this.timeLimit = timeLimit;
    }

    @Override
    public boolean isExpired(int turnNumber) {
        return timeLimit <= turnNumber;
    }
}
