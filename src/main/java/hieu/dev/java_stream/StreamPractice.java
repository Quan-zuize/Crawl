package hieu.dev.java_stream;

import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class StreamPractice {
    private static List<Transaction> getTransactions() {
        Trader raoul = new Trader("Raoul", "Cambridge");
        Trader mario = new Trader("Mario", "Milan");
        Trader alan = new Trader("Alan", "Cambridge");
        Trader brian = new Trader("Brian", "Cambridge");
        return List.of(
                new Transaction(brian, 2011, 300),
                new Transaction(raoul, 2012, 1000),
                new Transaction(raoul, 2011, 400),
                new Transaction(mario, 2012, 710),
                new Transaction(mario, 2012, 700),
                new Transaction(alan, 2012, 950)
        );
    }

    public static void main(String[] args) {
        List<Transaction> transactions = getTransactions();

        List<Transaction> sortedTransactionsIn2011 = transactions.stream()
                .filter(transaction -> Objects.equals(transaction.year(), 2011))
                .sorted(Comparator.comparing(Transaction::value))
                .toList();
        List<String> distinctTraderCities = transactions.stream()
                .map(Transaction::trader)
                .map(Trader::getCity)
                .distinct()
                .toList();
        List<Trader> sortedCambridgeTraders = transactions.stream()
                .map(Transaction::trader)
                .filter(trader -> Objects.equals(trader.getCity(), "Cambridge"))
                .distinct()
                .sorted(Comparator.comparing(Trader::getName))
                .toList();
        String sortedTraderNames = transactions.stream()
                .map(Transaction::trader)
                .map(Trader::getName)
                .distinct()
                .sorted()
                .reduce((name, name2) -> name + " " + name2).orElse("");
        boolean anyMilanTrader = transactions.stream()
                .map(Transaction::trader)
                .anyMatch(trader -> Objects.equals(trader.getCity(), "Milan"));

        Runnable printTransactionValues = () -> {
            transactions.stream()
                    .map(Transaction::value)
                    .map(value -> value + " ")
                    .forEach(System.out::print);
            System.out.println();
        };


        Supplier<Optional<Integer>> findHighestTransactionValue = () ->
                transactions.stream()
                        .map(Transaction::value)
//                        .max(Integer::compare);
                        .reduce(Integer::max);

        Supplier<Optional<Transaction>> findTransactionWithSmallestValue = () ->
                transactions.stream()
                        .min(Comparator.comparing(Transaction::value));


        System.out.println("1. sortedTransactionsIn2011: " + sortedTransactionsIn2011);
        System.out.println("2. distinctTraderCities: " + distinctTraderCities);
        System.out.println("3. sortedCambridgeTraders: " + sortedCambridgeTraders);
        System.out.println("4. sortedTraderNames: " + sortedTraderNames);
        System.out.println("5. anyMilanTrader: " + anyMilanTrader);
        System.out.print("6. printTransactionValues: ");
        printTransactionValues.run();
        System.out.println("7. findHighestTransactionValue: " + findHighestTransactionValue.get().get());
        System.out.println("8. findTransactionWithSmallestValue: " + findTransactionWithSmallestValue.get().get());
    }

    @Getter
    public static class Trader {
        private final String name;
        private final String city;

        public Trader(String n, String c) {
            this.name = n;
            this.city = c;
        }

        public String toString() {
            return "Trader:" + this.name + " in " + this.city;
        }
    }

    public record Transaction(Trader trader, int year, int value) {

        public String toString() {
            return "{" + this.trader + ", " +
                    "year: " + this.year + ", " +
                    "value:" + this.value + "}";
        }
    }
}
