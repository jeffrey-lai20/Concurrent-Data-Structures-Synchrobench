/*
 *  Based on example code from:
 *  "The Art of Multiprocessor Programming"
 *  M. Herlihy, N. SHavit
 *  chapter 14.3, 2008,
 *  https://github.com/gramoli/synchrobench/blob/master/java/src/skiplists/sequential/SequentialSkipListIntSet.java,
 *  and
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

    final private int maxIndex;
    final public Node head;
    final public Node tail;

    public SkiplistIntSet() {
        this.maxIndex = 100;
        this.head = new Node(maxIndex, Integer.MIN_VALUE);
        this.tail = new Node(maxIndex, Integer.MAX_VALUE);
        for (int i = 0; i <= maxIndex; i++) {
            head.setNext(i, tail);
        }
    }

    public boolean addInt(int value) {

        Node[] update = new Node[maxIndex + 1];
        Node curr = head;
        for (int i = maxIndex; i >= 0; i--) {
            Node next = curr.getNext(i);
            while (next.getValue() < value) {
                curr = next;
                next = curr.getNext(i);
            }
            update[i] = curr;
        }
        curr = curr.getNext(0);
        if (curr.getValue() == value) {
            return false;
        } else {
            int level = (int) Math.floor(Math.random()*maxIndex); //might need to minus 1
            curr = new Node(level, value);
            for (int i = 0; i <= level; i++) {
                curr.setNext(i, update[i].getNext(i));
                update[i].setNext(i, curr);
            }
            return true;
        }
    }

    public boolean removeInt(int value) {

        Node[] update = new Node[maxIndex + 1];
        Node curr = head;
        for (int i = maxIndex; i >= 0; i--) {
            Node next = curr.getNext(i);
            while (next.getValue() < value) {
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

    public boolean containsInt(int value) {
        Node curr = head;
        for (int i = maxIndex; i >= 0; i--) {
            Node next = curr.getNext(i);
            while(next.getValue() < value) {
                curr = next;
                next = curr.getNext(i);
            }
        }
        curr = curr.getNext(0);
        return (curr.getValue() == value);
    }

    public int size() {
        int i = 0;
        Node curr = head.getNext(0).getNext(0);
        while (curr != null) {
            curr = curr.getNext(0);
            i++;
        }
        return i;
    }

    public void clear() {
        for (int i = 0; i <= this.maxIndex; i++) {
            this.head.setNext(i, this.tail);
        }
    }

    public class Node {

        final private int value;
        final private Node[] next;

        public Node(final int index, final int value) {
            this.value = value;
            next = new Node[index+1];
        }

        public int getValue() {
            return this.value;
        }

        public int getIndex() {
            return next.length - 1;
        }

        public void setNext(final int index, final Node successor) {
            next[index] = successor;
        }

        public Node getNext(final int index) {
            return next[index];
        }

        public String toString() {
            String out = "";
            out += "Index: " + getIndex() + " Value: " + value;
            for (int i = 0; i <= getIndex(); i++) {
                out += " @[" + i + "]=";
                if (next[i] != null) {
                    out += next[i].getValue();
                } else {
                    out += "null";
                }
            }
            return out;
        }
    }

}
