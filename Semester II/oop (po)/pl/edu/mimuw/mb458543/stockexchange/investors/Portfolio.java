package pl.edu.mimuw.mb458543.stockexchange.investors;

import pl.edu.mimuw.mb458543.stockexchange.stocks.Stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Portfolio {
    private final HashMap<Stock, Integer> stocks;

    public Portfolio(Map<Stock, Integer> stocks) {
        this.stocks = new HashMap<>(stocks);
    }

    public List<Stock> getStocks() {
        return new ArrayList<>(stocks.keySet());
    }

    public void changeStockAmount(Stock stock, int change) {
        stocks.compute(stock, (k, currentAmount) -> (currentAmount == null) ? change : currentAmount + change);
    }

    public int getStockAmount(Stock stock) {
        return stocks.get(stock);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Stock, Integer> entry : stocks.entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
        }
        return sb.toString();
    }
}
