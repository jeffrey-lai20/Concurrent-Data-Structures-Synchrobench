package soft3410;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A coarse-graines locking linked list implementation of a set.
 */
public class ReadWriteLinkedListIntSet
    extends contention.abstractions.AbstractCompositionalIntSet {
  final private Node head;
  final private Node tail;
  ReentrantReadWriteLock readWriteLock;

  public ReadWriteLinkedListIntSet() {
    head = new Node(Integer.MIN_VALUE);
    tail = new Node(Integer.MAX_VALUE);
    head.setNext(tail);
    readWriteLock = new ReentrantReadWriteLock();
  }

  /**
   * Add a new int to the set.
   *
   * @param value  The new int to be added
   * @return false if the int already exists in the set
   */
  public boolean addInt(int value) {
    readWriteLock.writeLock().lock();
    Node predecessor = findPredecessor(value);
    Node current = predecessor.getNext();

    if (current.value == value) {
      readWriteLock.writeLock().unlock();
      return false;
    }
    Node node = new Node(value);
    node.setNext(current);
    predecessor.setNext(node);
    readWriteLock.writeLock().unlock();
    return true;
  }

  /**
   * Remove an int from the set.
   *
   * @param value  The int to be removed
   * @return false if the int did not exist in the set
   */
  public boolean removeInt(int value) {
    readWriteLock.writeLock().lock();
    Node predecessor = findPredecessor(value);
    Node current = predecessor.getNext();

    if (current.value != value) {
      readWriteLock.writeLock().unlock();
      return false;
    }
    predecessor.setNext(current.getNext());
    readWriteLock.writeLock().unlock();
    return true;
  }

  /**
   * Check if an int is a member of the set.
   *
   * @param value  The int to be checked
   * @return true if value exists in the set
   */
  public boolean containsInt(int value) {
    readWriteLock.readLock().lock();
    Node current = findPredecessor(value).getNext();
    readWriteLock.readLock().unlock();
    return (current.value == value);
  }

  public int size() {
    readWriteLock.readLock().lock();
    int size = 0;
    Node current = head.getNext();
    while (current.getNext() != null) {
      current = current.getNext();
      size++;
    }
    readWriteLock.readLock().unlock();
    return size;
  }

  /**
   * Find and return the predecessor to the node that would contain the given value.
   * @param value The value to search for.
   * @return the Node containing the largest value smaller than the search value
   */
  private Node findPredecessor(int value) {
    readWriteLock.readLock().lock();
    Node current = head;
    while (true) {
      // Found a match in the next node, so return its predecessor
      if (current.getNext().value >= value) {
        readWriteLock.readLock().unlock();
        return current;
      }
      current = current.getNext();
    }
  }

  /**
   * Empty the set.
   */
  public void clear() {
    readWriteLock.writeLock().lock();
    head.setNext(tail);
    readWriteLock.writeLock().unlock();
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
