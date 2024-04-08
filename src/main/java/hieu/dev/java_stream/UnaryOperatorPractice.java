package hieu.dev.java_stream;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class UnaryOperatorPractice {
    public static void main(String[] args) {
        IntStream numbers = IntStream.rangeClosed(1, 100);
        System.out.println(numbers.filter(n -> n % 2 == 0).count());


        Stream<int[]> pythagoreanTriples = IntStream.rangeClosed(1, 100).boxed()
                .flatMap(
                        a -> IntStream.rangeClosed(1, 100)
                                .filter(b -> Math.sqrt(a * a + b * b) % 1 == 0)
                                .mapToObj(b -> new int[]{a, b, (int) Math.sqrt(a * a + b * b)})
                );

        pythagoreanTriples.limit(5).forEach(vertexes -> {
            System.out.printf("%d, %d, %d%n", vertexes[0], vertexes[1], vertexes[2]);
        });

        Stream.iterate(List.of(0, 1), l -> List.of(l.get(1), l.get(0) + l.get(1)))
                .map(l -> l.get(0))
                .takeWhile(e -> e < 11)
                .forEach(System.out::println);

        Stream.generate(new Fibonacci()).limit(10).forEach(System.out::println);
    }

    public static class Fibonacci implements Supplier<Integer> {
        private int x1 = 0;
        private int x2 = 1;
        @Override
        public Integer get() {
            int temp1 = x1, temp2 = x2;
            x1 = temp2; x2 = temp1 + temp2;
            return temp1;
        }
    }
}
