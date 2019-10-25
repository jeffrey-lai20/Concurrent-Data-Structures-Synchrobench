package soft3410;
import java.util.*;

/**
 * A coarse-grained locking hash map implementation of a set.
 */
public class CoarseGrainedChainingHashTableIntSet
        extends contention.abstractions.AbstractCompositionalIntSet {

    private ArrayList<HashNode<Integer>> table;
    private static final int numBucket = 1024;
    private int size;

    /**
     * Constructor for the hash table.
     */
    public CoarseGrainedChainingHashTableIntSet() {
        this.size = 0;
        this.table = new ArrayList<HashNode<Integer>> (numBucket);
        for (int i = 0; i < numBucket; i++) {
            table.add(null);
        }
    }

    /**
     * Hash function to map a key to an index.
     * @param key
     * @return
     */
    public Integer getIndex (Integer key) {
        int hashCode = (int) key.hashCode();
        int index = hashCode % numBucket;
        return index;
    }

    /**
     * Add a new int to the set.
     * @param x
     * @return
     */
    public synchronized boolean addInt(int x) {
        if((size/numBucket) >= 10) {
            return false;
        }
        HashNode<Integer> prev = null;
        HashNode<Integer> node = table.get(getIndex(x));
        while (node != null && node.getKey() != x) {
            prev = node;
            node = node.next;
        }
        if (node == null) {
            node = new HashNode<Integer> (x);
            if (prev == null) {
                table.set(getIndex(x), node);
            } else {
                prev.setNext(node);
            }
            size++;
            return true;
        } else {
            node.setValue(x);
        }
        return false;
    }

    /**
     * Remove an int from the set.
     * @param x
     * @return
     */
    public synchronized boolean removeInt(int x) {
        HashNode<Integer> prev = null;
        HashNode<Integer> node = table.get(getIndex(x));
        while (node != null && node.getKey() != x) {
            prev = node;
            node = node.getNext();
        }
        if (node == null) {
            return false;
        } else {
            if (prev == null) {
                table.set(getIndex(x), node.getNext());
            } else {
                prev.setNext(node.getNext());
            }
            size--;
            return true;
        }
    }

    /**
     * Check if an int is a member of the set.
     * @param x
     * @return
     */
    public synchronized boolean containsInt(int x) {
        HashNode<Integer> node = table.get(getIndex(x));
        while(node != null) {
            if (node.getKey() == x) {
                return true;
            }
            node = node.getNext();
        }
        return false;

    }

    /**
     * Returns the size of the set.
     * @return
     */
    public int size() {
        int sizeA = 0;
        for (int i = 0; i < numBucket; i++) {
            HashNode<Integer> current = table.get(i);
            if (current != null) {
                while (current != null) {
                    current = current.getNext();
                    sizeA++;
                }
            }
        }
        return sizeA;
    }

    /**
     *  Empty the set.
     */
    public synchronized void clear() {
        this.size = 0;
        this.table = new ArrayList<HashNode<Integer>> (numBucket);
        for (int i = 0; i < numBucket; i++) {
            table.add(null);
        }
    }
}

