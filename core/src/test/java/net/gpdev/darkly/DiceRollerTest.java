package net.gpdev.darkly;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DiceRollerTest {

    private static final int REPETITIONS = 100000;
    private DiceRoller diceRoller;

    @org.junit.Before
    public void setUp() throws Exception {
        diceRoller = new DiceRoller(0);
    }

    @org.junit.After
    public void tearDown() throws Exception {
        diceRoller = null;
    }

    @Test
    public void testRoll1d20() {
        for (int i = 0; i < REPETITIONS; i++) {
            final List<Integer> results = diceRoller.roll(1, 20);
            assertEquals(1, results.size());
            final Integer roll = results.get(0);
            assertTrue(roll >= 1);
            assertTrue(roll <= 20);
        }
    }

    @Test
    public void testRoll3d6() {
        for (int i = 0; i < REPETITIONS; i++) {
            final List<Integer> results = diceRoller.roll(3, 6);
            assertEquals(3, results.size());
           results.forEach(roll -> {
               assertTrue(roll >= 1);
               assertTrue(roll <= 6);
           });
        }
    }

}