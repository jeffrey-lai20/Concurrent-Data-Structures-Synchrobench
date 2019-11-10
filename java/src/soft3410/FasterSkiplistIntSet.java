/*
 *  Based on example code from:
 *  "The Art of Multiprocessor Programming"
 *  M. Herlihy, N. SHavit
 *  chapter 14.3, 2008,
 *  Synchrobench's source code, v1.1.0-alpha
 *  "LazySkipList.java"
 *  https://github.com/gramoli/synchrobench/blob/master/java/src/skiplists/lockbased/LazySkipList.java,
 *  and
 *  "RandomLevelGenerator.java"
 *  https://github.com/gramoli/synchrobench/blob/master/java/src/skiplists/RandomLevelGenerator.java
 *  by Vincent Gramoli.
 */

package soft3410;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.Math;

/**
 * A fine-grained optimistic locking skip-list implementation of an int set.
 */
public final class FasterSkiplistIntSet
        extends contention.abstractions.AbstractCompositionalIntSet {

    /** The maximum number of levels */
    final private int maxLevel;
    /** The first element of the list */
    final private Node head;
    /** The last element of the list */
    final private Node tail;

    /** The thread-private PRNG, used for fil(), not for height/level determination. */
    final private static ThreadLocal<Random> s_random = new ThreadLocal<Random>() {
        @Override
        protected synchronized Random initialValue() {
            return new Random();
        }
    };


    private static transient int randomSeed = new Random().nextInt() | 0x0100;

    /**
     * Random level int generator.
     * @return
     */
    public static int randomLeveler() {
        int x = randomSeed;
        x ^= x << 13;
        x ^= x >>17;
        randomSeed = x ^= x <<5;
        if ((x & 0x80000001) != 0)
            return 0;
        int level = 1;
        while (((x >>>= 1) & 1) != 0)
            ++level;
        return level;
    }

    /**
     * Random level int generator in range of maximum level.
     * @return
     */
    private int randomLevel() {
        return Math.min((maxLevel - 1), (randomLeveler()));
    }

    public FasterSkiplistIntSet() {
        this.maxLevel = 31;
        this.head = new Node(Integer.MIN_VALUE, maxLevel);
        this.tail = new Node(Integer.MAX_VALUE, maxLevel);
        for (int i = 0; i <= maxLevel; i++) {
            head.next[i] = tail;
        }
    }

    /**
     * Traverses the bottom level of the skiplist, counting all nodes.
     * Returns the number of unique valued nodes in the skip-list.
     * @return
     */
    @Override
    public int size() {
        int size = 0;
        Node node = head.next[0].next[0];

        while (node != null) {
            node = node.next[0];
            size++;
        }
        return size;
    }

    /**
     * Clears the skip-list, removing all elements by setting
     * the head node to point to the tail node.
     */
    @Override
    public void clear() {
        for (int i = 0; i <= this.maxLevel; i++) {
            this.head.next[i] = this.tail;
        }
        return;
    }

    /**
     * Calls the find method to check if the value exists in the skip-list.
     * Checks to see if the node is fully linked, assuring it isn't being removed.
     * @param value
     * @return
     */
    @Override
    public boolean containsInt(final int value) {
        Node[] preds = (Node[]) new Node[maxLevel + 1];
        Node[] succs = (Node[]) new Node[maxLevel + 1];
        int levelFound = find(value, preds, succs);
        return (levelFound != -1 && succs[levelFound].fullyLinked);
    }

    /**
     * Goes through the levels and linked list to find the value.
     * Returns -1 if the value is not found, otherwise returns
     * the level it exists up to.
     * @param value
     * @param preds
     * @param succs
     * @return
     */
    private int find(final int value, Node[] preds, Node[] succs) {
        int key = value;
        int levelFound = -1;
        Node pred = head;

        for (int level = maxLevel; level >= 0; level--) {
            Node curr = pred.next[level];

            while (key > curr.key) {
                pred = curr;
                curr = pred.next[level];
            }

            if (levelFound == -1 && key == curr.key) {
                levelFound = level;
            }
            preds[level] = pred;
            succs[level] = curr;
        }
        return levelFound;
    }

    /**
     * Goes through the level's linked list to find the predecessor node.
     * Returns the found predecessor.
     * @param value
     * @param level
     * @return
     */
    public Node findPredecessor(int value, int level) {
        int key = value;
        Node pred = head;
        while(pred.next[level].key < value) {
            pred = pred.next[level];
        }
        return pred;
    }

    /**
     * For each level, checks if the predecessor of the successors is still the same.
     * Returns false if predecessors/successors have changed.
     * @param level
     * @param predecessors
     * @param successors
     * @return
     */
    private boolean validate(int level, Node[] predecessors, Node[] successors) {
        int topLevel = level;
            for (int i = 0; i < topLevel; i++) {
                Node current = findPredecessor(successors[i].key, i);
                if (current.key == predecessors[i].key) {
                    if (predecessors[i].next[i].key != successors[i].key) return false;
                }
            }
            return true;
    }

    /**
     * Optimistic locking of addInt. Finds nodes without locking, then lock nodes,
     * and finally check that everything is okay.
     * @param value
     * @return
     */
    @Override
    public boolean addInt(final int value) {
        int topLevel = randomLevel();
        Node[] predecessors = (Node[]) new Node[maxLevel + 1];  //Sets of predecessors for different levels
        Node[] successors = (Node[]) new Node[maxLevel + 1];    //Sets of successors for different levels

        int levelFound = find(value, predecessors, successors);
        //Level found isn't -1, which means it already exists
        if (levelFound != -1) {
                    return false;
            }
            synchronized (predecessors) {
                synchronized (successors) {
                    if (validate(topLevel, predecessors, successors)) {
                        //Goes through each level and sets the appropriate successor
                        Node newNode = new Node(value, topLevel);
                        for (int level = 0; level <= topLevel; level++) {
                            newNode.next[level] = successors[level];
                        }
                        //Goes through each level and sets the appropriate next node for the predecessors
                        for (int level = 0; level <= topLevel; level++) {
                            predecessors[level].next[level] = newNode;
                        }
                        //After done, set fully linked as true
                        newNode.fullyLinked = true;
                        return true;
                    }
                    return false;
                }
            }
    }

    /**
     * Checks if the value exists, and then removes it by setting its predecessors'
     * next node to the node to be removed's next node.
     * @param value
     * @return
     */
    @Override
    public boolean removeInt(final int value) {
        Node victim = null; //Victim to remove probs
        int topLevel = -1;  //Max level that victim exists at
        Node[] predecessors = (Node[]) new Node[maxLevel + 1];    //Victim's predecessors
        Node[] successors = (Node[]) new Node[maxLevel + 1];   //Victim's successors
        int levelFound = find(value, predecessors, successors); //Initialize predecessors and successors

        //levelFound is -1 which means that the value isn't found.
        if (levelFound == -1) {
            return false;
        }
        //If the level is actually found then the victim is the successor
        victim = successors[levelFound];
        topLevel = victim.topLevel;
        if (victim.fullyLinked && victim.topLevel == levelFound) {  //If the node is fully linked (preds and succs for all levels connected) and level is the same as found
                    synchronized (predecessors) {
                        synchronized (successors) {
                            if (validate(levelFound, predecessors, successors)) {
                                //Unlink
                                for (int level = topLevel; level >= 0; level--) {
                                    predecessors[level].next[level] = victim.next[level];   //set the predecessors of the victim to the victim's successors
                                }
                                return true;
                            }
                        }
                    }
        }
        return false;
    }

    /**
     * Class Node used for the link list.
     */
    private static final class Node {
        final Lock lock = new ReentrantLock();
        final int key;
        final Node[] next;
        volatile boolean fullyLinked = false;
        private int topLevel;

        public Node(final int value, int height) {
            key = value;
            next = new Node[height + 1];
            topLevel = height;
        }
    }
}