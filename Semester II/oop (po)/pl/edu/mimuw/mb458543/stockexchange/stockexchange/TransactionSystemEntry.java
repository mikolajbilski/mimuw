package pl.edu.mimuw.mb458543.stockexchange.stockexchange;

import pl.edu.mimuw.mb458543.stockexchange.transactions.TransactionOrder;
import pl.edu.mimuw.mb458543.stockexchange.transactions.TransactionOrderType;

import java.util.Objects;

public class TransactionSystemEntry implements Comparable<TransactionSystemEntry> {
    private final TransactionOrder transactionOrder;
    private final Timestamp timestamp;

    public TransactionSystemEntry(TransactionOrder transactionOrder, int turnNumber, int index) {
        this.transactionOrder = transactionOrder;
        this.timestamp = new Timestamp(turnNumber, index);
    }

    public TransactionOrder getTransactionOrder() {
        return transactionOrder;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    // Allows sorting by desired order in transaction queue
    @Override
    public int compareTo(TransactionSystemEntry o) {
        int compare = (transactionOrder.getType() == TransactionOrderType.BUY) ? -1 : 1;
        compare *= transactionOrder.getPriceLimit() - o.getTransactionOrder().getPriceLimit();
        if(compare != 0) return compare;
        return timestamp.compareTo(o.timestamp);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionSystemEntry that)) return false;
        return transactionOrder.equals(that.transactionOrder) && Objects.equals(transactionOrder, that.transactionOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionOrder, transactionOrder);
    }
}
