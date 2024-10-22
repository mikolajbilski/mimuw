package pl.edu.mimuw.mb458543.stockexchange.stockexchange;

import pl.edu.mimuw.mb458543.stockexchange.stocks.Stock;
import pl.edu.mimuw.mb458543.stockexchange.transactions.TransactionOrder;

import java.util.*;

public class TransactionSystem {
    private final Map<Stock, TransactionQueue> stockQueues;

    private int currentTurnOrder;
    private int lastOrderTurn;

    public TransactionSystem(Map<Stock, Integer> stocks) {
        currentTurnOrder = 0;
        lastOrderTurn = 0;
        stockQueues = new HashMap<>();
        stocks.forEach((stock, price) -> stockQueues.put(stock, new TransactionQueue(stock, price)));
    }


    public Map<Stock, Integer> processTransactions(int turnNumber) {
        Map<Stock, Integer> newPrices = new HashMap<>();
        // Process all possible transactions
        for(TransactionQueue queue : stockQueues.values()) {
            int newPrice = queue.processTransactions(turnNumber);
            newPrices.put(queue.getStock(), newPrice);
        }
        return newPrices;
    }

    public void addTransaction(TransactionOrder transactionOrder, int turnNumber) {
        if(turnNumber != lastOrderTurn) {
            lastOrderTurn = turnNumber;
            currentTurnOrder = 0;
        }
        Stock stock = transactionOrder.getStock();
        stockQueues.get(stock).addTransaction(transactionOrder, turnNumber, currentTurnOrder++);
    }
}
