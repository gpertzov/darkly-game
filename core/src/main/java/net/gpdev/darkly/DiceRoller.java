package net.gpdev.darkly;

import com.badlogic.gdx.math.RandomXS128;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class DiceRoller {
    final private Random random;


    public DiceRoller(final long seed) {
        random = new RandomXS128(seed);
    }

    List<Integer> roll(final int numDice, final int numSides) {
        return IntStream
                .generate(() -> random.nextInt(numSides) + 1)
                .limit(numDice)
                .boxed().collect(toList());
    }

    int rollSum(final int numDice, final int numSides) {
        return roll(numDice, numSides).stream().mapToInt(Integer::intValue).sum();
    }
}
