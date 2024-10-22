package pl.edu.mimuw.mb458543.stockexchange.stockexchange;

import pl.edu.mimuw.mb458543.stockexchange.investors.Investor;
import pl.edu.mimuw.mb458543.stockexchange.stocks.Stock;
import pl.edu.mimuw.mb458543.stockexchange.transactions.TransactionOrder;
import pl.edu.mimuw.mb458543.stockexchange.transactions.TransactionOrderType;

import java.util.SortedSet;
import java.util.TreeSet;

public class TransactionQueue {
    private final SortedSet<TransactionSystemEntry> buyOrders;
    private final SortedSet<TransactionSystemEntry> sellOrders;

    private final Stock stock;
    private int stockPrice;

    public TransactionQueue(Stock stock, int stockPrice) {
        buyOrders = new TreeSet<>();
        sellOrders = new TreeSet<>();
        this.stock = stock;
        this.stockPrice = stockPrice;
    }

    // Returns the price at which a given transaction should occur, or -1 if the transaction is impossible
    private static int getTransactionPrice(TransactionSystemEntry buyOrder, TransactionSystemEntry sellOrder) {
        int buyPrice = buyOrder.getTransactionOrder().getPriceLimit();
        int sellPrice = sellOrder.getTransactionOrder().getPriceLimit();
        if (buyPrice < sellPrice) {
            return -1;
        }
        return (buyOrder.getTimestamp().compareTo(sellOrder.getTimestamp()) > 0) ? sellPrice : buyPrice;
    }

    private static int getTransactionQuota(TransactionSystemEntry buyOrder, TransactionSystemEntry sellOrder) {
        return Math.min(buyOrder.getTransactionOrder().getMaxQuota(), sellOrder.getTransactionOrder().getMaxQuota());
    }

    public void removeExpiredTransactions(int turnNumber) {
        buyOrders.removeIf((t) -> t.getTransactionOrder().isExpired(turnNumber));
        sellOrders.removeIf((t) -> t.getTransactionOrder().isExpired(turnNumber));
    }

    public void addTransaction(TransactionOrder transactionOrder, int turnNumber, int currentTurnOrder) {
        if (transactionOrder.getType() == TransactionOrderType.BUY) {
            buyOrders.add(new TransactionSystemEntry(transactionOrder, turnNumber, currentTurnOrder));
        } else {
            sellOrders.add(new TransactionSystemEntry(transactionOrder, turnNumber, currentTurnOrder));
        }
    }

    // Returns the price of the last transaction of that stock. If no trades were made, returns the previous price
    public int processTransactions(int turnNumber) {
        //System.out.println("Processing " + buyOrders.size() + " buy orders and " + sellOrders.size() + " sell orders for stock " + stock + ".");
        // Process all possible transactions

        int newPrice = stockPrice;

        boolean done = false;
        while (!done) {
            if(sellOrders.isEmpty() || buyOrders.isEmpty()) {
                break;
            }
            TransactionSystemEntry sellOrder = sellOrders.first();
            TransactionSystemEntry buyOrder = buyOrders.first();
            int price = getTransactionPrice(buyOrder, sellOrder);
            if(price == -1) {
                done = true;
            } else {
                int transactionQuota = getTransactionQuota(buyOrder, sellOrder);
                Investor buyer = buyOrder.getTransactionOrder().getInvestor();
                Investor seller = sellOrder.getTransactionOrder().getInvestor();
                // Validate transaction
                if(!buyer.canHandleTransaction(TransactionOrderType.BUY, stock, price, transactionQuota)) {
                    buyOrders.removeFirst();
                    continue;
                }
                if(!seller.canHandleTransaction(TransactionOrderType.SELL, stock, price, transactionQuota)) {
                    sellOrders.removeFirst();
                    continue;
                }
                buyOrders.removeFirst();
                sellOrders.removeFirst();
                // Handle actual transaction
                newPrice = price;
                buyer.trade(TransactionOrderType.BUY, stock, price, transactionQuota);
                seller.trade(TransactionOrderType.SELL, stock, price, transactionQuota);
                // If there are any remaining stocks to buy/sell, create new transactions
                int remainingBuyAmount = buyOrder.getTransactionOrder().getMaxQuota() - transactionQuota;
                if(remainingBuyAmount > 0) {
                    buyOrder.getTransactionOrder().reduceQuota(transactionQuota);
                    buyOrders.add(buyOrder);
                }
                int remainingSellAmount = sellOrder.getTransactionOrder().getMaxQuota() - transactionQuota;
                if(remainingSellAmount > 0) {
                    sellOrder.getTransactionOrder().reduceQuota(transactionQuota);
                    sellOrders.add(sellOrder);
                }
            }
        }
        removeExpiredTransactions(turnNumber);
        stockPrice = newPrice;
        return newPrice;
    }

    public Stock getStock() {
        return stock;
    }
}
