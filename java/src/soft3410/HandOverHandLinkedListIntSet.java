package soft3410;

import java.util.concurrent.locks.*;
/**
 * A hand over hand locking linked list implementation of a set.
 */
public class HandOverHandLinkedListIntSet
    extends contention.abstractions.AbstractCompositionalIntSet {
  final private Node head;
  final private Node tail;

  public HandOverHandLinkedListIntSet() {
    head = new Node(Integer.MIN_VALUE);
    tail = new Node(Integer.MAX_VALUE);
    head.setNext(tail);
  }

  /**
   * Add a new int to the set.
   * 
   * @param item  The new int to be added
   * @return false if the int already exists in the set
   */
  public boolean addInt(int item) {
    head.lock();
    Node predecessor = head;
    head.getNext().lock();
    Node current = head.getNext();
    while (current.value < item) {
      predecessor.unlock();
      predecessor = current;
      current.getNext().lock();
      current = predecessor.getNext();
    }

    if (current.value == item) {
      predecessor.unlock();
      current.unlock();
      return false;
    }
    Node node = new Node(item);
    node.setNext(current);
    predecessor.setNext(node);
    predecessor.unlock();
    current.unlock();
    return true;
  }

  /**
   * Remove an int from the set.
   * 
   * @param item  The int to be removed
   * @return false if the int did not exist in the set
   */
  public boolean removeInt(int item) {
    head.lock();
    Node predecessor = head;
    head.getNext().lock();
    Node current = head.getNext();
    while (current.value < item) {
      predecessor.unlock();
      predecessor = current;
      current.getNext().lock();
      current = predecessor.getNext();
    }

    if (current.value != item) {
      predecessor.unlock();
      current.unlock();
      return false;
    }
    predecessor.setNext(current.getNext());
    predecessor.unlock();
    current.unlock();
    return true;
  }

  /**
   * Check if an int is a member of the set.
   * 
   * @param item  The int to be checked
   * @return true if item exists in the set
   */
  public boolean containsInt(int item) {
    head.lock();
    Node predecessor = head;
    head.getNext().lock();
    Node current = head.getNext();
    while (current.value < item) {
      predecessor.unlock();
      predecessor = current;
      current.getNext().lock();
      current = predecessor.getNext();
    }
    predecessor.unlock();
    current.unlock();
    return (current.value == item);
  }

  public int size() {
    int size = 0;
    head.lock();
    Node predecessor = head;
    head.getNext().lock();
    Node current = head.getNext();
    while (current.getNext() != null) {
      predecessor.unlock();
      predecessor = current;
      current.getNext().lock();
      current = predecessor.getNext();
      size++;
    }
    predecessor.unlock();
    return size;
  }

  /**
   * Empty the set.
   */
  public void clear() {
    head.lock();
    head.setNext(tail);
    head.unlock();
  }

  class Node {

    final int value;
    private Node next;
    private Lock lock;

    Node(int value, Node next) {
      this.value = value;
      this.next = next;
      this.lock = new ReentrantLock();
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

    void lock() {
        lock.lock();
    }

    void unlock() {
        lock.unlock();
    }

  }

}

