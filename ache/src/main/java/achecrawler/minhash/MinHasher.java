package achecrawler.minhash;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/*
 * Generates a family of hash functions that can be used for locality-sensitive hashing (LSH).
 */
public class MinHasher {

    public final int nextPrime = 2147483587;
    public final int maxValue = nextPrime - 1;
    public int[] coeffA;
    public int[] coeffB;
    public int numOfHashes;
    private int seed;

    /**
     * Creates a family of universal hash functions. Uses a fixed seed number (chosen randomly) to
     * generate the hash functions.
     * 
     * @param numOfHashes The number of hash functions to generate.
     */
    public MinHasher(int numOfHashes) {
        this(numOfHashes, 1947);
    }

    /**
     * Creates a family of universal hash functions.
     * 
     * @param numOfHashes The number of hash functions to generate.
     * @param seed The seed number used to generate the hash functions.
     */
    public MinHasher(int numOfHashes, int seed) {
        this.numOfHashes = numOfHashes;
        this.seed = seed;
        this.coeffA = pickRandCoefficients(numOfHashes);
        this.coeffB = pickRandCoefficients(numOfHashes);
    }

    public int[] minHashSignature(Set<Integer> hashedShingles) {
        int[] signatures = new int[numOfHashes];
        for (int i = 0; i < numOfHashes; i++) {
            int min = nextPrime + 1;
            for (int shingle : hashedShingles) {
                shingle = shingle % maxValue;
                int h = (coeffA[i] * shingle + coeffB[i]) % nextPrime;
                if (h < min) {
                    min = h;
                }
            }
            signatures[i] = min;
        }
        return signatures;
    }

    private int[] pickRandCoefficients(int k) {
        int[] rands = new int[k];
        HashSet<Integer> seen = new HashSet<Integer>(k);
        Random random = new Random(seed);
        int i = 0;
        while (k > 0) {
            int randIndex = random.nextInt(maxValue);
            while (seen.contains(randIndex)) {
                randIndex = random.nextInt(maxValue);
            }
            rands[i] = randIndex;
            seen.add(randIndex);
            k = k - 1;
            i++;
        }
        return rands;
    }

}
