package pl.edu.mimuw.mb458543.stockexchange.stocks;

public record Stock(String name) {

    public Stock {
        if (name.isEmpty() || name.length() > 5) {
            throw new IllegalArgumentException();
        }
        for (char c : name.toCharArray()) {
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException();
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
