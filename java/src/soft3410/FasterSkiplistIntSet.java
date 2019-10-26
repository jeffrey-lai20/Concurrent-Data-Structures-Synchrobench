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
        Node node = head.next[0].next[0];

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
        return (levelFound != -1 && succs[levelFound].fullyLinked && !succs[levelFound].marked);
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

    /**
     * Optimistic locking of addInt. Find nodes without locking, then lock nodes,
     * and finally check that everything is okay.
     * @param value
     * @return
     */
    @Override
    public boolean addInt(final int value) {
        int topLevel = randomLevel();
        Node[] preds = (Node[]) new Node[maxLevel+ 1];
        Node[] succs = (Node[]) new Node[maxLevel+ 1];

        while (true) {
            /* Call find() to initialize preds and succs. */
            int levelFound = find(value, preds, succs);

            /* If an node is found that is unmarked then return false. */
//            if (levelFound != -1) {
//                Node nodeFound = succs[levelFound];
////                if (!nodeFound.marked) {
////                    /* Needs to wait for nodes to become fully linked. */
////                    while (!nodeFound.fullyLinked) {}
////                    return false;
////                }
////                /* If marked another thread is deleting it, so we retry. */
////                continue;
//            }

                //^^ That be a spin lock that says if it finds the level, and it isn't marked, wait in a while loop until it's fully linked.
            //  if it be fully linked, then start the whole while loop again.

            int highestLocked = -1;

            try {
                Node pred, succ;
//                boolean valid = true;

                /* Acquire locks. */
                for (int level = 0; (level <= topLevel); level++) {
                    pred = preds[level];
                    succ = succs[level];
                    pred.lock.lock();
                    highestLocked = level;
//                    valid = !pred.marked && !succ.marked && pred.next[level]==succ;
                }

                for (int level = 0; (level <= topLevel); level++) {
                    pred = preds[level];
                    succ = succs[level];
                    if (!containsInt(pred)) return false;
//                    pred.lock.lock();
//                    highestLocked = level;
//                    valid = !pred.marked && !succ.marked && pred.next[level]==succ;
                }


                /* Must have encountered effects of a conflicting method, so it releases (in the
                 * finally block) the locks it acquired and retries */
//                if (!valid) {
//                    continue;
//                }

                // Ayo this part below adds the new node, adding the next nodes for each successive level for next and prevs

                Node newNode = new Node(value, topLevel);
                for (int level = 0; level <= topLevel; level++) {
                    newNode.next[level] = succs[level];
                }
                for (int level = 0; level <= topLevel; level++) {
                    preds[level].next[level] = newNode;
                }
                newNode.fullyLinked = true; // successful and linearization point
                return true;

                //It be fully linked bc all levels are fully linked

            } finally {
                for (int level = 0; level <= highestLocked; level++) {
                    preds[level].unlock();
                }
            }

        }

    }

    @Override
    public boolean removeInt(final int value) {
        Node victim = null;
        boolean isMarked = false;
        int topLevel = -1;
        Node[] preds = (Node[]) new Node[maxLevel + 1];
        Node[] succs = (Node[]) new Node[maxLevel + 1];

        while (true) {
            /* Call find() to initialize preds and succs. */
            int levelFound = find(value, preds, succs);
            if (levelFound != -1) {
                victim = succs[levelFound];
            }

            /* Ready to delete if unmarked, fully linked, and at its top level. */
//            if (isMarked | (levelFound != -1 && (victim.fullyLinked && victim.topLevel == levelFound && !victim.marked))) {
//
//                /* Acquire locks in order to logically delete. */
//                if (!isMarked) {
//                    topLevel = victim.topLevel;
//                    victim.lock.lock();
//                    if (victim.marked) {
//                        victim.lock.unlock();
//                        return false;
//                    }
//                    victim.marked = true; // logical deletion
//                    isMarked = true;
//                }

                int highestLocked = -1;

                try {
                    Node pred, succ;
//                    boolean valid = true;

                    /* Acquire locks. */
                    for (int level = 0; valid && (level <= topLevel); level++) {
                        pred = preds[level];
                        pred.lock.lock();
                        highestLocked = level;
                        valid = !pred.marked && pred.next[level]==victim;
                    }

                    /* Acquire locks. */
                    for (int level = 0; valid && (level <= topLevel); level++) {
                        pred = preds[level];
                        if (!containsInt(pred)) return false;
//                        pred.lock.lock();
//                        highestLocked = level;
//                        valid = !pred.marked && pred.next[level]==victim;
                    }

                    /* Pred has changed and is no longer suitable, thus unlock and retries. */
//                    if (!valid) {
//                        continue;
//                    }

                    /* Unlink. */
                    for (int level = topLevel; level >= 0; level--) {
                        preds[level].next[level] = victim.next[level];
                    }
                    victim.lock.unlock();
                    return true;

                } finally {
                    for (int i = 0; i <= highestLocked; i++) {
                        preds[i].unlock();
                    }
                }
            } else {
                return false;
            }
        }


    }

    private static final class Node {
        final Lock lock = new ReentrantLock();
        final int key;
        final Node[] next;
        volatile boolean marked = false;
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