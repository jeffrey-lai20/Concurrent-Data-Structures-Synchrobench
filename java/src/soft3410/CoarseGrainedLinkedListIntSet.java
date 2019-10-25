package soft3410;

/**
 * A coarse-grained locking linked list implementation of a set.
 */
public class CoarseGrainedLinkedListIntSet
    extends contention.abstractions.AbstractCompositionalIntSet {
  final private Node head;
  final private Node tail;

  public CoarseGrainedLinkedListIntSet() {
    head = new Node(Integer.MIN_VALUE);
    tail = new Node(Integer.MAX_VALUE);
    head.setNext(tail);
  }

  /**
   * Add a new int to the set.
   *
   * @param value  The new int to be added
   * @return false if the int already exists in the set
   */
  public synchronized boolean addInt(int value) {
    Node predecessor = findPredecessor(value);
    Node current = predecessor.getNext();

    if (current.value == value) {
      return false;
    }
    Node node = new Node(value);
    node.setNext(current);
    predecessor.setNext(node);
    return true;
  }

  /**
   * Remove an int from the set.
   *
   * @param value  The int to be removed
   * @return false if the int did not exist in the set
   */
  public synchronized boolean removeInt(int value) {
    Node predecessor = findPredecessor(value);
    Node current = predecessor.getNext();

    if (current.value != value) {
      return false;
    }
    predecessor.setNext(current.getNext());
    return true;
  }

  /**
   * Check if an int is a member of the set.
   *
   * @param value  The int to be checked
   * @return true if value exists in the set
   */
  public synchronized boolean containsInt(int value) {
    Node current = findPredecessor(value).getNext();
    return (current.value == value);
  }

  public synchronized int size() {
    int size = 0;
    Node current = head.getNext();
    while (current.getNext() != null) {
      current = current.getNext();
      size++;
    }
    return size;
  }

  /**
   * Find and return the predecessor to the node that would contain the given value.
   * @param value The value to search for.
   * @return the Node containing the largest value smaller than the search value
   */
  private synchronized Node findPredecessor(int value) {
    Node current = head;
    while (true) {
      // Found a match in the next node, so return its predecessor
      if (current.getNext().value >= value) {
        return current;
      }
      current = current.getNext();
    }
  }

  /**
   * Empty the set.
   */
  public synchronized void clear() {
    head.setNext(tail);
  }

  class Node {

    final int value;
    private Node next;

    Node(int value, Node next) {
      this.value = value;
      this.next = next;
    }

    Node(int value) {
      this(value, null);
    }

    void setNext(Node next) {
      this.next = next;
    }

    Node getNext() {
      return next;
    }
  }

}
