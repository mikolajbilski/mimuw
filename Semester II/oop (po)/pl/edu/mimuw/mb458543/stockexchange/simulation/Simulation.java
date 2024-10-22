package pl.edu.mimuw.mb458543.stockexchange.simulation;

import pl.edu.mimuw.mb458543.stockexchange.investors.Investor;
import pl.edu.mimuw.mb458543.stockexchange.investors.Portfolio;
import pl.edu.mimuw.mb458543.stockexchange.investors.RandomInvestor;
import pl.edu.mimuw.mb458543.stockexchange.investors.SMAInvestor;
import pl.edu.mimuw.mb458543.stockexchange.stocks.Stock;
import pl.edu.mimuw.mb458543.stockexchange.stockexchange.StockExchange;
import pl.edu.mimuw.mb458543.stockexchange.utils.RandomnessProvider;

import java.io.*;
import java.util.*;

public class Simulation {
    private StockExchange stockExchange;
    private final int simulationTime;
    private List<Investor> investors;

    private String getNextDataString(Scanner sc) throws EOFException {
        String line;
        while(sc.hasNext()) {
            line = sc.nextLine();
            if(line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }
            return line;
        }
        sc.close();
        throw new EOFException("Nieoczekiwany koniec pliku!");
    }

    private boolean loadSimulationData(String filename) {
        // Open data file
        Scanner sc;
        try {
            sc = new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            System.out.println("Nie znaleziono pliku " + filename);
            return false;
        }

        // Read investor count
        String investorTypes;
        try {
             investorTypes = getNextDataString(sc);
        } catch (EOFException e) {
            return false;
        }

        int randomInvestors = 0;
        int smaInvestors = 0;

        for(char c : investorTypes.toCharArray()) {
            switch(c) {
                case ' ':
                    break;
                case 'R':
                    ++randomInvestors;
                    break;
                case 'S':
                    ++smaInvestors;
                    break;
                default:
                    System.out.println("Nieoczekiwany symbol podczas wczytywania danych o inwestorach: " + c);
                    sc.close();
                    return false;
            }
        }

        // Read stock info
        String stockInfo;
        try {
            stockInfo = getNextDataString(sc);
        } catch (EOFException e) {
            return false;
        }

        String[] stocks = stockInfo.split("\\s+");
        Map<Stock, Integer> stockTypes = new HashMap<>();
        for(String stock : stocks) {
            String stockName;
            int stockPrice;
            try {
                stockName = stock.substring(0, stock.indexOf(":"));
                if(stockName.isEmpty() || stockName.length() > 5) {
                    throw new IllegalArgumentException();
                }
                for(char c : stockName.toCharArray()) {
                    if(c < 'A' || c > 'Z') {
                        throw new IllegalArgumentException();
                    }
                }
                stockPrice = Integer.parseInt(stock.substring(stock.indexOf(":") + 1));
            } catch (Exception e) {
                System.out.println("Niepoprawne dane akcji: " + stockInfo);
                sc.close();
                return false;
            }
            Stock s = new Stock(stockName);
            if(stockTypes.put(s, stockPrice) != null) {
                System.out.println("Duplikat w nazwach akcji: " + stockInfo);
                sc.close();
                return false;
            }

        }

        // Create stock exchange
        stockExchange = new StockExchange(stockTypes);

        // Read starting portfolio
        String portfolioInfo;
        try {
            portfolioInfo = getNextDataString(sc);
        } catch (EOFException e) {
            return false;
        }

        int startingMoney;
        String[] portfolioStrings = portfolioInfo.split("\\s+");
        try {
            startingMoney = Integer.parseInt(portfolioStrings[0]);
            if(startingMoney < 0) {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            System.out.println("Niepoprawne początkowe środki: " + portfolioStrings[0]);
            sc.close();
            return false;
        }
        if(portfolioStrings.length - 1 != stockTypes.size()) {
            System.out.println("Niepoprawne dane portfolio");
            sc.close();
            return false;
        }

        Map<Stock, Integer> portfolioAmounts = new HashMap<>();
        for(int i = 1; i < portfolioStrings.length; i++) {
            String portfolioItem = portfolioStrings[i];
            Stock s;
            int startingAmount;
            try {
                String portfolioStockName = portfolioItem.substring(0, portfolioItem.indexOf(":"));
                s = new Stock(portfolioStockName);
                if(!stockTypes.containsKey(s)) {
                    throw new IllegalArgumentException();
                }
                startingAmount = Integer.parseInt(portfolioItem.substring(portfolioItem.indexOf(":") + 1));
                if(portfolioAmounts.put(s, startingAmount) != null) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception e) {
                System.out.println("Niepoprawne dane portfolio: " + portfolioItem);
                sc.close();
                return false;
            }
        }

        // Create investors with specified portfolio
        investors = new ArrayList<>(randomInvestors + smaInvestors);
        for(int i = 0; i < randomInvestors; i++) {
            investors.add(new RandomInvestor(stockExchange, new Portfolio(portfolioAmounts), startingMoney));
        }
        for(int i = 0; i < smaInvestors; i++) {
            investors.add(new SMAInvestor(stockExchange, new Portfolio(portfolioAmounts), startingMoney));
        }

        sc.close();
        return true;
    }

    public Simulation(String filename, int simulationTime) {
        boolean readSuccess = loadSimulationData(filename);
        if(!readSuccess) {
            throw new IllegalArgumentException("Podczas tworzenia sytuacji wystąpił błąd!");
        }
        if(simulationTime < 0) {
            throw new IllegalArgumentException("Simulation time must be non-negative: " + simulationTime);
        }
        this.simulationTime = simulationTime;
    }

    public void simulate() {
        for(int i = 0; i < simulationTime; ++i) {
            RandomnessProvider.shuffle(investors);
            for(Investor investor : investors) {
                investor.makeDecision();
            }
            stockExchange.processTransactions();
        }
    }

    public String getResults() {
        StringBuilder sb = new StringBuilder();
        for(Investor investor : investors) {
            sb.append(investor).append("\n");
        }
        return sb.toString();
    }
}
