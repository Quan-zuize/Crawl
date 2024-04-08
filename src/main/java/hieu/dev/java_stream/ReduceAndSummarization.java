package hieu.dev.java_stream;

import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ReduceAndSummarization {
    private static final Faker faker = Faker.instance();
    private static final Timer timer = Timer.instance();
    public static void main(String[] args) {
        timer.start();
        List<Dish> dishes = generateData();
        List<DishSummary> dishSummaryList = dishes.stream().map(Dish::toDishSummary).toList();
        timer.stop("Data Generated");

        Integer count = dishes.size();
        Double sum = dishes.stream().mapToDouble(Dish::getCalories).sum();
        Double averageDishCalories = dishes.stream().collect(Collectors.averagingDouble(Dish::getCalories));
        Optional<Dish> minDishCaloriesOptional = dishes.stream().min(Comparator.comparing(Dish::getCalories));
        Optional<Dish> maxDishCaloriesOptional = dishes.stream().max(Comparator.comparing(Dish::getCalories));
        Double min = minDishCaloriesOptional.orElse(new Dish()).getCalories();
        Double max = maxDishCaloriesOptional.orElse(new Dish()).getCalories();

        DishSummary dishSummary = new DishSummary(
                count, sum, averageDishCalories, min, max
        );
        System.out.println(dishSummary);
        timer.stop("Data Summarization");

        timer.start();
        DishSummary reducedDish = dishSummaryList.stream()
                .reduce((dishSummary1, dishSummary2) -> {
                    DishSummary dishSummary3 = new DishSummary();
                    dishSummary3.setCount(dishSummary1.getCount() + dishSummary2.getCount());
                    dishSummary3.setSum(dishSummary1.getSum() + dishSummary2.getSum());
                    dishSummary3.setMin(Math.min(dishSummary1.getMin(), dishSummary2.getMin()));
                    dishSummary3.setMax(Math.max(dishSummary1.getMax(), dishSummary2.getMax()));
                    return dishSummary3;
                }).get();
        reducedDish.setAverageCalories(reducedDish.getSum() / reducedDish.getCount());
        System.out.println(reducedDish);
        timer.stop("Data reduction");

        String names = dishes.stream().map(Dish::getName).collect(Collectors.joining(", "));
//        System.out.println(names);
    }

    public static List<Dish> generateData() {
        List<Dish> dishes = new ArrayList<>(List.of(
                new Dish("pork", 800.0),
                new Dish("beef", 700.0),
                new Dish("salmon", 450.0)
        ));
        IntStream.rangeClosed(1, 100).forEach(value -> {
            dishes.add(new Dish(faker.name().firstName(), faker.number().randomDouble(2, 300, 1000)));
        });
        return dishes;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Dish implements Serializable{
        private String name;
        private Double calories;

        public DishSummary toDishSummary() {
            DishSummary dishSummary = new DishSummary();
            BeanUtils.copyProperties(this, dishSummary);
            dishSummary.setCount(1);
            dishSummary.setSum(this.calories);
            dishSummary.setMin(this.calories);
            dishSummary.setMax(this.calories);
            return dishSummary;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DishSummary extends Dish implements Serializable{
        private Integer count;
        private Double sum;
        private Double averageCalories;
        private Double min;
        private Double max;
    }

    public static class Timer {
        private long start0 = 0;
        public static Timer instance() {
            return new Timer();
        }
        public void start() {
            start0 = System.currentTimeMillis();
        }
        public void stop() {
            log.info("Time execute: {}", System.currentTimeMillis() - start0);
            start0 = System.currentTimeMillis();
        }
        public void stop(String tag) {
            log.info("[{}] Time execute: {}", tag, System.currentTimeMillis() - start0);
            start0 = System.currentTimeMillis();
        }
    }
}
