package soft3410;

import java.util.Collection;
import java.util.Random;
import java.util.Stack;

import contention.abstractions.CompositionalIntSet;
import contention.abstractions.CompositionalIterator;


/**
 * A sequential skiplist-based implementation of the integer set
 */
public class SkiplistIntSet
        extends contention.abstractions.AbstractCompositionalIntSet {

    /** The maximum number of levels */
    final private int maxLevel;
    /** The first element of the list */
    final public Node head;
    /** The last element of the list */
    final public Node tail;
    /** The thread-private PRNG, used for fil(), not for height/level determination. */
    final private static ThreadLocal<Random> s_random = new ThreadLocal<Random>() {
        @Override
        protected synchronized Random initialValue() {
            return new Random();
        }
    };
    private static transient int randomSeed = new Random().nextInt() | 0x0100;


    public SkiplistIntSet() {
        this(31);
    }

    public SkiplistIntSet(final int maxLevel) {
        this.maxLevel = maxLevel;
        this.head = new Node(maxLevel, Integer.MIN_VALUE);
        this.tail = new Node(maxLevel, Integer.MAX_VALUE);
        for (int i = 0; i <= maxLevel; i++) {
            head.setNext(i, tail);
        }
    }

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

    public void fill(final int range, final long size) {
        while (this.size() < size) {
            this.addInt(s_random.get().nextInt(range));
        }
    }

    private int randomLevel() {
        return Math.min((maxLevel - 1), (randomLeveler()));
    }

    @Override
    public boolean containsInt(final int value) {
        boolean result;

        Node node = head;

        for (int i = maxLevel; i >= 0; i--) {
            if (node.getNext(i) == null) return false;
            Node next = node.getNext(i);
            while (next.getValue() < value) {
                if (next == null) return false;
                node = next;
                if (node.getNext(i) == null) return false;
                next = node.getNext(i);
            }
        }
        if (node.getNext(0) == null) return false;
        node = node.getNext(0);

        result = (node.getValue() == value);

        return result;
    }

    @Override
    public boolean addInt(final int value) {

//        Node[] update = new Node [maxLevel + 1];
//        Node node = head;
//        for (int i = maxLevel; i >= 0; i--) {
//            while ((node.getNext(i) != null) && (node.getNext(i).getValue() < value)) node = node.getNext(i);
//            update[i] = node;
//        }
//        node = node.getNext(0);
//
//        if (node.getValue() != value || node == null) {
//            int level = randomLevel();
//            Node newNode = new Node(level, value);
//            for (int i = 0; i < level; i++) {
//                newNode.setNext(i, update[i].getNext(i));
//                update[i].setNext(i, newNode);
//            }
//            return true;
//        }
//        return false;


//        System.out.print("a");
        Node[] update = new Node[maxLevel + 1];
        Node curr = head;   //current node we at
//        for (int i = maxLevel; i >= 0; i--) {
//            if (curr.getNext(i) == null) return false;
//            Node next = curr.getNext(i);    //node next to current
//            while (next.getValue() < value) {
//                if (next == null) return false;
//                curr = next;    //the next value is less than what we adding, then go next
//                if (curr.getNext(i) == null) return false;
//                next = curr.getNext(i); //set next to current's new next
//            }
//            if (curr == null) return false;
//            update[i] = curr;   //well we reached a node that has a value higher, next's value would be higher so current is still under
//        }
        for (int i = maxLevel; i>= 0; i--) {
            while (curr.getNext(i) != null && curr.getNext(i).getValue() < value) {
                curr = curr.getNext(i);
            }
            update[i] = curr;
        }


//        if (curr.getNext(0) == null) return false;
        curr = curr.getNext(0); //current is now the higher value node
        if (curr.getValue() == value || curr == null) { //if the higher value node equals to the value we tryna add then no
            return false;
        } else {
//            int level = (int) Math.floor(Math.random()*maxIndex); //might need to minus 1
            int level = randomLevel();
            curr = new Node(level, value);  //making a new node
            for (int i = 0; i <= level; i++) {
                if (update[i].getNext(i) == null && update[0].getNext(0) != null) return false;
                curr.setNext(i, update[i].getNext(i));  //yeah this makes sense
                if (curr == null && update[0] != null) return false;
                update[i].setNext(i, curr); //yeah this does too
            }
            return true;
        }
    }

    @Override
    public boolean removeInt(int value) {
//        System.out.print("r");
        boolean result;

        Node[] update = new Node[maxLevel + 1];
        Node node = head;

        for (int i = maxLevel; i >= 0; i--) {
            if (node.getNext(i) == null) return false;
            Node next = node.getNext(i);
            while (next.getValue() < value) {
                if (next == null) return false;
                node = next;
                if (node.getNext(i) == null) return false;
                next = node.getNext(i);
            }
            if (node == null) return false;
            update[i] = node;
        }
        if (node.getNext(0) == null) return false;
        node = node.getNext(0);

        if (node == null) return false;
        if (node.getValue() != value) {
            result = false;
        } else {
            int maxLevel = node.getLevel();
            for (int i = maxLevel; i >= 0; i--) {
                if (node.getNext(i) == null) return false;
                update[i].setNext(i, node.getNext(i));
            }
            result = true;
        }

        return result;
    }

    @Override
    public boolean addAll(Collection<Integer> c) {
        boolean result = true;
        for (int x : c) result &= this.addInt(x);
        return result;
    }

    @Override
    public boolean removeAll(Collection<Integer> c) {
        boolean result = true;
        for (int x : c) result &= this.removeInt(x);
        return result;
    }

    @Override
    public int size() {
        int s = 0;
        Node node = head.getNext(0).getNext(0);

        while (node != null) {
            node = node.getNext(0);
            s++;
        }
        return s;
    }

    @Override
    public String toString() {
        String str = new String();
        Node curr = head;
        int i, j;
        final int[] arr = new int[maxLevel+1];

        for (i=0; i<= maxLevel; i++) arr[i] = 0;

        do {
            str += curr.toString();
            arr[curr.getLevel()]++;
            curr = curr.getNext(0);
        } while (curr != null);
        for (j=0; j < maxLevel; j++)
            str += arr[j] + " nodes of level " + j;
        return str;
    }


    public class Node {

        final private int value;
        final private Node[] next;

        public Node(final int level, final int value) {
            this.value = value;
            next = new Node[level + 1];
        }

        public int getValue() {
            return value;
        }

        public int getLevel() {
            return next.length - 1;
        }

        public void setNext(final int level, final Node succ) {
            next[level] = succ;
        }

        public Node getNext(final int level) {
            return next[level];
        }

        @Override
        public String toString() {
            String result = "";
            result += "<l=" + getLevel() + ",v=" + value + ">:";
            for (int i = 0; i <= getLevel(); i++) {
                result += " @[" + i + "]=";
                if (next[i] != null) {
                    result += next[i].getValue();
                } else {
                    result += "null";
                }
            }
            return result;
        }
    }

    public class SLIterator implements CompositionalIterator<Integer> {
        Node next = head;
        Stack<Node> stack = new Stack<Node>();

        SLIterator() {
            while (next != null) {
                stack.push(next.next[0]);
            }
        }

        public boolean hasNext() {
            return next != null;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public Integer next() {
            Node node = next;
            next = stack.pop();
            return node.getValue();
        }
    }

    /**
     * This is called after the JVM warmup phase to make sure the data structure is well initalized,
     * and after each iteration to clear the structure.
     */
    public void clear() {
        for (int i = 0; i <= this.maxLevel; i++) {
            this.head.setNext(i, this.tail);
        }
        return;
    }

    @Override
    public Object getInt(int value) {
        Node node = head;

        for (int i = maxLevel; i >= 0; i--) {
            Node next = node.getNext(i);
            while (next.getValue() < value) {
                node = next;
                next = node.getNext(i);
            }
        }
        node = node.getNext(0);

        if (node.getValue() == value) return node;
        return null;
    }

    @Override
    public Object putIfAbsent(int x, int y) {
        if (!containsInt(x)) removeInt(y);
        return null;
    }
}