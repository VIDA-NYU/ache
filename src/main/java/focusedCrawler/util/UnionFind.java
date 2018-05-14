package focusedCrawler.util;

import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class UnionFind {
    /**
     * parent[i] points to parent of element i or to self.
     */
    private IntArrayList parent;

    /**
     * rank[i] holds the rank (cardinality) of root element i.
     */
    private IntArrayList rank;

    /**
     * The number of disjoint sets
     */
    private int num;

    /**
     * Create n disjoint sets containing a single element numbered from 0 to n - 1.
     * 
     * @param n
     */
    public UnionFind(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Expected n > 0");
        }
        parent = new IntArrayList(n);
        parent.size(n);
        rank = new IntArrayList(n);
        rank.size(n);
        for (int i = 0; i < n; ++i) {
            parent.set(i,  i); // root of self
            rank.set(i, 1); // contains only self
        }
        num = n;
    }

    /**
     * Find representative element (i.e root of tree) for element i
     * 
     * @param i
     * @return
     */
    public int find(int i) {
        if (i < 0 || i > parent.size())
            throw new NoSuchElementException("Invalid element: i=" + i);

        return root(i);
    }

    /**
     * Merge set containing u with the one containing u.
     * 
     * @param u
     * @param v
     * @return the representative of union
     */
    public int union(int u, int v) {
        // Grow arrays internal storage is smaller than u or v
        int max = Math.max(u, v) + 1;
        if(parent.size() < max) {
            growArrays(max);
        }
        
        // Replace elements by representatives
        u = find(u);
        v = find(v);

        if (u == v)
            return u; // no-op

        // Make smaller tree u point to v
        if (rank.getInt(v) < rank.getInt(u)) {
            int t = v;
            v = u;
            u = t; // swap u, v
        }

        parent.set(u, v);
        rank.set(v, rank.getInt(v) + rank.getInt(u));
        rank.set(u, -1);

        num--;

        return v;
    }

    private void growArrays(int max) {
        int currentSize = parent.size();
        parent.size(max);
        rank.size(max);
        for(int i = currentSize; i < max; i++) {
            parent.set(i,  i); // root of self
            rank.set(i, 1); // contains only self
        }
        num += max - currentSize;
    }

    public int numberOfSets() {
        return num;
    }

    /**
     * Find representative (root) of element u
     */
    private int root(int u) {
        while (parent.getInt(u) != u)
            u = parent.get(u);
        return u;
    }

    /**
     * Find root of element u, while compressing path of visited nodes.
     * <p>
     * This is an optimized version of {@link UnionFind#root(int)} which modifies the internal tree
     * as it traverses it (moving from u to root).
     */
    @SuppressWarnings("unused")
    private int root1(int u) {
        int p = parent.get(u);
        if (p == u)
            return u;

        // So, u is a non-root node with parent p
        do {
            int p1 = parent.get(p);
            if (p == p1) {
                // The root is found at p
                u = p;
                break;
            } else {
                // Must move 1 level up
                parent.set(u, p1); // compress path for u (minus 1)
                u = p;
                p = p1;
            }
        } while (true);

        return u;
    }

    /**
     * Get rank (i.e. cardinality) of the set containing element u
     * 
     * @param u
     * @return
     */
    public int rank(int u) {
        u = root(u);
        return rank.getInt(u);
    }

    public Int2ObjectMap<IntList> listSets() {
        Int2ObjectMap<IntList> sets = new Int2ObjectOpenHashMap<IntList>();
        for (int i = 0; i < parent.size(); i++) {
            int p = find(i);
            IntList list = sets.get(p);
            if (list == null) {
                list = new IntArrayList();
                sets.put(p, list);
            }
            list.add(i);
        }
        return sets;
    }

}
