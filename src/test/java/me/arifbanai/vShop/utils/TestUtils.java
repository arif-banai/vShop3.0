package me.arifbanai.vShop.utils;

import java.util.List;
import java.util.Random;

/**
 * Utility functions used for testing purposes
 */
public class TestUtils {

    /**
     * Generate a random int using Math.random() in some range
     * @param min Minimum (inclusive) of the random number
     * @param max Maximum (exclusive) of the random number
     * @return A random int in the range (min <= int < max)
     */
    public static int getRandomInt(int min, int max) {
        if (min >= max)
            throw new IllegalArgumentException("Max must be greater than min");

        return (int)(Math.random() * ((max - min))) + min;
    }

    /**
     * Swap first (subsetSize) elements in the list (input) with some random element
     *
     * Operates in O(subSize) time
     * @param input The list of elements
     * @param subsetSize the size of the resulting subset
     * @param <T> The type of the elements in the List
     * @return a random subset of the list
     */
    public static <T> List<T> getRandomSubList(List<T> input, int subsetSize) {

        if(subsetSize > input.size()) {
            throw new IllegalArgumentException("subsetSize must be less than or equal to input.size()");
        }

        Random r = new Random();
        int inputSize = input.size();
        for (int i = 0; i < subsetSize; i++)
        {
            int indexToSwap = i + r.nextInt(inputSize - i);
            T temp = input.get(i);
            input.set(i, input.get(indexToSwap));
            input.set(indexToSwap, temp);
        }

        return input.subList(0, subsetSize);
    }

}
