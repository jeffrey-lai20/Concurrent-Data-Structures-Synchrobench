/*
 *  Based on example code from:
 *  "The Art of Multiprocessor Programming"
 *  M. Herlihy, N. SHavit
 *  chapter 14.3, 2008,
 *  Synchrobench's source code, v1.1.0-alpha
 *  "SequentialSkipListIntSet.java"
 *  https://github.com/gramoli/synchrobench/blob/master/java/src/skiplists/sequential/SequentialSkipListIntSet.java,
 *  and
 *  "RandomLevelGenerator.java"
 *  https://github.com/gramoli/synchrobench/blob/master/java/src/skiplists/RandomLevelGenerator.java
 *  by Vincent Gramoli.
 */

package soft3410;

import java.util.Collection;
import java.util.Random;
import java.util.Stack;
import java.lang.Math;

/**
 * A sequential skip-list implementation of an int set.
 */

public class SkiplistIntSet
        extends contention.abstractions.AbstractCompositionalIntSet {

    /** The maximum number of levels */
    final private int maxIndex;
    /** The first element of the list */
    final public Node head;
    /** The last element of the list */
    final public Node tail;
    private static transient int randomSeed = new Random().nextInt() | 0x0100;

    public SkiplistIntSet() {
        this.maxIndex = 100;
        this.head = new Node(maxIndex, Integer.MIN_VALUE);
        this.tail = new Node(maxIndex, Integer.MAX_VALUE);
        for (int i = 0; i <= maxIndex; i++) {
            head.setNext(i, tail);
        }
    }

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
        return Math.min((maxIndex - 1), (randomLeveler()));
    }

    /**
     * Finds the position the new node is to be added, and checks if the value already exists.
     * For each level, relinks the predecessors and successors of the linked list to the new valued node.
     * @param value
     * @return
     */
    public boolean addInt(int value) {
        Node[] update = new Node[maxIndex + 1];
        Node curr = head;
        for (int i = maxIndex; i >= 0; i--) {
            Node next = curr.getNext(i);
            while (next != null && next.getValue() < value) {
                curr = next;
                next = curr.getNext(i);
            }
            update[i] = curr;
        }
        curr = curr.getNext(0);
        if (curr.getValue() == value) {
            return false;
        } else {
            int level = randomLevel();
            curr = new Node(level, value);
            for (int i = 0; i <= level; i++) {
                curr.setNext(i, update[i].getNext(i));
                update[i].setNext(i, curr);
            }
            return true;
        }
    }

    /**
     * Finds the appropriate node and checks if the value to be removed exists.
     * If so, delinks the node and links the predecessors the the successors.
     * @param value
     * @return
     */
    public boolean removeInt(int value) {
        Node[] update = new Node[maxIndex + 1];
        Node curr = head;
        for (int i = maxIndex; i >= 0; i--) {
            Node next = curr.getNext(i);
            while (next.getValue() < value && next != null) {
                curr = next;
                next = curr.getNext(i);
            }
            update[i] = curr;
        }
        curr = curr.getNext(0);
        if (curr.getValue() != value) {
            return false;
        } else {
            int maxIndex = curr.getIndex();
            for (int i = 0; i <= maxIndex; i++) {
                update[i].setNext(i, curr.getNext(i));
            }
            return true;
        }
    }

    /**
     * Goes through the levels and linked lists to check if the value exists in the skip-list.
     * @param value
     * @return
     */
    public boolean containsInt(int value) {
        Node curr = head;
        for (int i = maxIndex; i >= 0; i--) {
            Node next = curr.getNext(i);
            while(next != null && next.getValue() < value) {
                curr = next;
                next = curr.getNext(i);
            }
        }
        curr = curr.getNext(0);
        return (curr.getValue() == value);
    }

    /**
     * Traverses the bottom level of the skiplist, counting all nodes.
     * Returns the number of unique valued nodes in the skip-list.
     * @return
     */
    public synchronized int size() {
        int i = 0;
        Node curr = head.getNext(0).getNext(0);
        while (curr != null) {
            curr = curr.getNext(0);
            i++;
        }
        return i;
    }

    /**
     * Clears the skip-list, removing all elements by setting
     * the head node to point to the tail node.
     */
    public void clear() {
        for (int i = 0; i <= this.maxIndex; i++) {
            this.head.setNext(i, this.tail);
        }
    }

    /**
     * Class Node used for the link list.
     */
    public class Node {
        final private int value;
        final private Node[] next;

        public Node(final int index, final int value) {
            this.value = value;
            next = new Node[index+1];
        }

        /**
         * Returns the node's value.
         * @return
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Returns the node's index.
         * @return
         */
        public int getIndex() {
            return next.length - 1;
        }

        /**
         * Sets the node's successor.
         * @param index
         * @param successor
         */
        public void setNext(final int index, final Node successor) {
            next[index] = successor;
        }

        /**
         * Returns the node's successor.
         * @param index
         * @return
         */
        public Node getNext(final int index) {
            return next[index];
        }
    }
}