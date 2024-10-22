package pl.edu.mimuw.mb458543.stockexchange.transactions;

import pl.edu.mimuw.mb458543.stockexchange.investors.Investor;
import pl.edu.mimuw.mb458543.stockexchange.stocks.Stock;

public abstract class TransactionOrder {
    private final Investor investor;
    private final TransactionOrderType type;
    private final Stock stock;
    private int maxQuota;
    private final int priceLimit;

    public TransactionOrder(Investor investor, TransactionOrderType type, Stock stock, int maxQuota, int priceLimit) {
        this.investor = investor;
        this.type = type;
        this.stock = stock;
        this.maxQuota = maxQuota;
        this.priceLimit = priceLimit;
    }

    // Is transaction expired at the end of turnNumber turn?
    public abstract boolean isExpired(int turnNumber);

    public void reduceQuota(int tradedQuota) {
        if(maxQuota < tradedQuota || tradedQuota < 0) {
            throw new IllegalArgumentException("Trying to reduce transaction quota of" + maxQuota + " by " + tradedQuota);
        }
        maxQuota -= tradedQuota;
    }

    public Investor getInvestor() {
        return investor;
    }

    public TransactionOrderType getType() {
        return type;
    }

    public Stock getStock() {
        return stock;
    }

    public int getMaxQuota() {
        return maxQuota;
    }

    public int getPriceLimit() {
        return priceLimit;
    }
}
