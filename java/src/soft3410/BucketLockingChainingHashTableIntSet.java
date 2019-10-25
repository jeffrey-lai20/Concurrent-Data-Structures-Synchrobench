package soft3410;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A fine-grained bucket locking hash map implementation of a set.
 */
public class BucketLockingChainingHashTableIntSet
        extends contention.abstractions.AbstractCompositionalIntSet {

    private ArrayList<HashNode<Integer>> table;
    private static final int numBucket = 1024;
    private int size;
    ReentrantLock bucketLock[];

    /**
     * Constructor for the hash table.
     */
    public BucketLockingChainingHashTableIntSet() {
        this.size = 0;
        this.table = new ArrayList<HashNode<Integer>> (numBucket);
        this.bucketLock = new ReentrantLock[numBucket];
        for (int i = 0; i < numBucket; i++) {
            table.add(null);
            bucketLock[i] = new ReentrantLock();
        }
    }

    /**
     * Hash function to map a key to an index.
     * @param key
     * @return
     */
    public int getIndex (Integer key) {
        int hashCode = (int) key.hashCode();
        int index = hashCode % numBucket;
        return index;
    }

    /**
     * Add a new int to the set.
     * @param x
     * @return
     */
    public boolean addInt(int x) {
        bucketLock[getIndex(x)].lock();

        if((size/numBucket) >= 10) {
            bucketLock[getIndex(x)].unlock();
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
            bucketLock[getIndex(x)].unlock();
            return true;
        } else {
            node.setValue(x);
        }
        bucketLock[getIndex(x)].unlock();
        return false;
    }

    /**
     * Remove an int from the set.
     * @param x
     * @return
     */
    public boolean removeInt(int x) {
        bucketLock[getIndex(x)].lock();
        HashNode<Integer> prev = null;
        HashNode<Integer> node = table.get(getIndex(x));

        while (node != null && node.getKey() != x) {
            prev = node;
            node = node.getNext();
        }

        if (node == null) {
            bucketLock[getIndex(x)].unlock();
            return false;
        } else {
            if (prev == null) {
                table.set(getIndex(x), node.getNext());
            } else {
                prev.setNext(node.getNext());
            }
            size--;
            bucketLock[getIndex(x)].unlock();
            return true;
        }
    }

    /**
     * Check if an int is a member of the set.
     * @param x
     * @return
     */
    public boolean containsInt(int x) {
        bucketLock[getIndex(x)].lock();
        HashNode<Integer> node = table.get(getIndex(x));
        while(node != null) {
            if (node.getKey() == x) {
                bucketLock[getIndex(x)].unlock();
                return true;
            }
            node = node.getNext();
        }
        bucketLock[getIndex(x)].unlock();
        return false;
    }

    /**
     * Returns the size of the set.
     * @return
     */
    public synchronized int size() {
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
     * Empty the set.
     */
    public synchronized void clear() {
        this.size = 0;
        this.table = new ArrayList<HashNode<Integer>> (numBucket);
        this.bucketLock = new ReentrantLock[numBucket];
        for (int i = 0; i < numBucket; i++) {
            table.add(null);
            bucketLock[i] = new ReentrantLock();
        }
    }
}

/**
 * Class for the linked list hash node.
 * @param <Integer>
 */
class HashNode<Integer> {
    Integer key;
    Integer value;
    HashNode<Integer> next;

    /**
     * Constructor for the hash node.
     * @param keyValue
     */
    HashNode(Integer keyValue) {
        this.key = keyValue;
        this.value = keyValue;
        next = null;
    }

    /**
     * Returns the node's key.
     * @return
     */
    Integer getKey() {
        return this.key;
    }

    /**
     * Returns the node's value.
     * @return
     */
    Integer getValue() {
        return this.value;
    }

    /**
     * Sets the node's value.
     * @param value
     */
    void setValue(Integer value) {
        this.value = value;
    }

    /**
     * Returns the next node in the linked list.
     * @return
     */
    HashNode getNext() {
        return this.next;
    }

    /**
     * Sets the next node in the linked list.
     * @param next
     */
    void setNext(HashNode<Integer> next) {
        this.next = next;
    }

}
