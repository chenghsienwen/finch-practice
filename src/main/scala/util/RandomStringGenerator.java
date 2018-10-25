package util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomStringGenerator {

    private static final String alphaNumber = "0123456789abcdefghijklmnopqrstuvwxyz";

    private final Random randomAlgorithm;

    private final char[] outputSymbols;

    private final char[] buffer;

    public RandomStringGenerator(int length) {
        if (length < 1) throw new IllegalArgumentException();
        this.randomAlgorithm = new SecureRandom();
        this.outputSymbols = alphaNumber.toCharArray();
        this.buffer = new char[length];
    }

    public String nextString() {
        for (int idx = 0; idx < buffer.length; ++idx)
            buffer[idx] = outputSymbols[randomAlgorithm.nextInt(outputSymbols.length)];
        return new String(buffer);
    }

}