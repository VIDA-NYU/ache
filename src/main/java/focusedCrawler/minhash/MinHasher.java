package focusedCrawler.minhash;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MinHasher {
    
    public final int nextPrime = 2147483587;
    public final int maxValue = nextPrime - 1;
    public int[] coeffA;
    public int[] coeffB;
    public int numOfHashes;

    public MinHasher(int numOfHashes) {
        this.numOfHashes = numOfHashes;
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
        Random random = new Random();
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