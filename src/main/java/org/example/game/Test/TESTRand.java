package org.example.game.Test; // Keep package or change to a more appropriate one (e.g., com.yourcompany.util)

import java.util.concurrent.ThreadLocalRandom; // Often preferred for better performance in concurrent apps

public class TESTRand {

    // Define a constant for the upper bound (exclusive) for clarity
    private static final int UPPER_BOUND = 1000; // Generates numbers from 0 to 999

    // Define how many numbers to generate
    private static final int NUMBERS_TO_GENERATE = 10;

    public static void main(String[] args) {
        // Use ThreadLocalRandom for potentially better performance and less contention
        // Or stick with 'new Random()' if preferred for simplicity in single-threaded apps
        // Random random = new Random(); // Simple alternative

        System.out.printf("Generating %d random numbers (0-%d), formatted to 3 digits:\n",
                NUMBERS_TO_GENERATE, UPPER_BOUND - 1);

        for (int i = 0; i < NUMBERS_TO_GENERATE; i++) {
            // Generate a random integer between 0 (inclusive) and UPPER_BOUND (exclusive)
            // int number = random.nextInt(UPPER_BOUND); // If using 'new Random()'
            int number = ThreadLocalRandom.current().nextInt(UPPER_BOUND); // Preferred way

            // Use String.format for efficient and correct zero-padding
            // %03d means:
            // % - Start of format specifier
            // 0 - Pad with leading zeros
            // 3 - Minimum width of 3 characters
            // d - Format as a decimal integer
            String formattedNumber = String.format("%03d", number);

            // Print the formatted number
            System.out.println(formattedNumber);
        }
        System.out.println("Generation complete.");
    }
}