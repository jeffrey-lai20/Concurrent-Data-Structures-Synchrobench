/*
 * Algorithm:
 *   Fine-grained locking skip list.
 *   "A Simple Optimistic Skiplist Algorithm"
 *   M. Herlihy, Y. Lev, V. Luchangco, N. Shavit
 *   p.124-138, SIROCCO 2007
 *
 * Code:
 *  Based on example code from:
 *  "The Art of Multiprocessor Programming"
 *  M. Herlihy, N. SHavit
 *  chapter 14.3, 2008
 *
 */

package soft3410;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.lang.Math;

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

    private int randomLevel() {
        return Math.min((maxLevel - 1), (randomLeveler()));
    }


    public FasterSkiplistIntSet() {
        this(31);
    }

    public FasterSkiplistIntSet(final int maxLevel) {
        this.head = new Node(Integer.MIN_VALUE, maxLevel);
        this.tail = new Node(Integer.MAX_VALUE, maxLevel);
        this.maxLevel = maxLevel;
        for (int i = 0; i <= maxLevel; i++) {
            head.next[i] = tail;
        }
    }

    @Override
    public int size() {
        int size = 0;
        Node node = head.next[0].next[0];   //Exclude count for head and tail.

        while (node != null) {
            node = node.next[0];
            size++;
        }
        return size;
    }

    @Override
    public void clear() {
        for (int i = 0; i <= this.maxLevel; i++) {
            this.head.next[i] = this.tail;
        }
        return;
    }

    @Override
    public boolean containsInt(final int value) {
        Node[] preds = (Node[]) new Node[maxLevel + 1];
        Node[] succs = (Node[]) new Node[maxLevel + 1];
        int levelFound = find(value, preds, succs);
        return (levelFound != -1 && succs[levelFound].fullyLinked);
    }

    /* The preds[] and succs[] arrays are filled from the maximum level to 0 with the predecessor and successor references for the given key. */
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

    public Node findPredecessor(int value, int level) {
        int key = value;
        Node pred = head;
        while(pred.next[level].key < value) {
            pred = pred.next[level];
        }
        return pred;
    }

    private boolean validate(int level, Node[] predecessors, Node[] successors) {
        int topLevel = level;
            for (int i = 0; i < topLevel; i++) {
                Node current = findPredecessor(successors[i].key, i);
                if (current.key == predecessors[i].key) {
                    return predecessors[i].next[i].key == successors[i].key;
                }
            }
            return false;
    }

    /**
     * Optimistic locking of addInt. Find nodes without locking, then lock nodes,
     * and finally check that everything is okay.
     * @param value
     * @return
     */
    @Override
    public boolean addInt(final int value) {
//        int topLevel = (int) Math.floor(Math.random()*maxLevel); //Might need to minus 1, but sets the random highest level for the int
        int topLevel = randomLevel();
        Node[] predecessors = (Node[]) new Node[maxLevel + 1];  //Sets of predecessors for different levels
        Node[] successors = (Node[]) new Node[maxLevel + 1];    //Sets of successors for different levels

        int levelFound = find(value, predecessors, successors);
        //If it already exists then leave? yeah leave
        //No don't leave, that sets predecessors and successors. Only returns -1 if nothing is filled? I think
        //Ohh returns -1 if nothing is filled or if the value doesn't exist.
        if (levelFound != -1) {
                    return false;
            }
//        while (true) {
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
//                    continue;
                    return false;
                }
            }
//        }
    }

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
//            if (victim.fullyLinked) {
//                synchronized (victim) {
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
//                }
//            }
        }
        return false;
    }

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


        public void lock() {
            lock.lock();
        }

        public void unlock() {
            lock.unlock();
        }
    }

}