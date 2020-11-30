package achecrawler.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Preconditions;

/**
 * This class creates a sample from an unbounded stream of items.
 *
 */
public class Sampler<T> {
    
    private final List<T> reservoir;
    private final int numSamples;

    private final Random random;
    private int numItemsSeen = 0;

    /**
     * Create a new sampler with a certain reservoir size using a supplied random number generator.
     *
     * @param numSamples Maximum number of samples to retain in the reservoir. Must be non-negative.
     * @param random Instance of the random number generator to use for sampling
     */
    public Sampler(int numSamples, Random random) {
        Preconditions.checkArgument(numSamples > 0, "numSamples should be positive");
        Preconditions.checkNotNull(random);
        this.numSamples = numSamples;
        this.random = random;
        this.reservoir = new ArrayList<T>(numSamples);
    }

    public Sampler(List<T> initialSample) {
        this(initialSample, new Random());
    }

    public Sampler(List<T> initialSample, Random random) {
        Preconditions.checkNotNull(initialSample);
        Preconditions.checkArgument(initialSample.size() > 0, "initial samle can't empty");
        this.numSamples = initialSample.size();
        this.random = random;
        this.reservoir = new ArrayList<T>(initialSample);
    }

    /**
     * Create a new sampler with a certain reservoir size using the default random number generator.
     *
     * @param numSamples Maximum number of samples to retain in the reservoir. Must be non-negative.
     */
    public Sampler(int numSamples) {
        this(numSamples, new Random());
    }

    /**
     * Sample an item and store in the reservoir if needed.
     *
     * @param item The item to sample - may not be null.
     */
    public void sample(T item) {
        Preconditions.checkNotNull(item);
        if (reservoir.size() < numSamples) {
            // reservoir not yet full, just append
            reservoir.add(item);
        } else {
            // find a sample to replace
            int randomIndex = random.nextInt(numItemsSeen + 1);
            if (randomIndex < numSamples) {
                reservoir.set(randomIndex, item);
            }
        }
        numItemsSeen++;
    }

    /**
     * Get samples collected in the reservoir.
     *
     * @return A sequence of the samples. No guarantee is provided on the order of the samples.
     */
    public Iterable<T> getSamples() {
        return reservoir;
    }
    
    public int reservoirSize() {
        return reservoir.size();
    }
}
