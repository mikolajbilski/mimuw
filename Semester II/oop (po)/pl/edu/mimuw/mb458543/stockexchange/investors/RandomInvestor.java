package pl.edu.mimuw.mb458543.stockexchange.investors;

import pl.edu.mimuw.mb458543.stockexchange.stockexchange.StockExchange;
import pl.edu.mimuw.mb458543.stockexchange.stocks.Stock;
import pl.edu.mimuw.mb458543.stockexchange.transactions.*;
import pl.edu.mimuw.mb458543.stockexchange.utils.RandomnessProvider;

public class RandomInvestor extends Investor {
    public RandomInvestor(StockExchange exchange, Portfolio portfolio, int money) {
        super(exchange, portfolio, money);
        name = "Random Investor";
    }

    @Override
    public void makeDecision() {
        int buyOrSell = RandomnessProvider.getRandomNumber(0, 1);
        TransactionOrderType type = buyOrSell == 1 ? TransactionOrderType.BUY : TransactionOrderType.SELL;
        Stock stock = RandomnessProvider.chooseRandom(portfolio.getStocks());
        if(getStockAmount(stock) == 0 && type == TransactionOrderType.SELL) {
            return;
        }
        int previousPrice = stockExchange.getStockPrice(stock);
        int maxDifference = stockExchange.getMaxPriceDifference();
        int price = RandomnessProvider.getRandomNumber(Math.max(previousPrice - maxDifference, 1), previousPrice + maxDifference);
        int maxAmount;
        if(type == TransactionOrderType.BUY) {
            maxAmount = money / price;
        } else {
            maxAmount = getStockAmount(stock);
        }
        int amount = RandomnessProvider.getRandomNumber(1, Math.max(maxAmount, 1));
        int transactionType = RandomnessProvider.getRandomNumber(0, 50);

        TransactionOrder transactionOrder = switch (transactionType) {
            case 0, 1 -> {
                int timeLimit = stockExchange.getTurnNumber() + RandomnessProvider.getRandomNumber(0, 10);
                yield new TimeLimitedOrder(this, type, stock, amount, price, timeLimit);
            }
            case 2 -> new NoLimitOrder(this, type, stock, amount, price);
            case 3, 4 -> new DoOrCancelOrder(this, type, stock, amount, price);
            default -> new InstantOrder(this, type, stock, amount, price);
        };

        stockExchange.placeTransactionOrder(transactionOrder);
    }
}
