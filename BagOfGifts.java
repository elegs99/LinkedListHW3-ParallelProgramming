import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class BagOfGifts {
  public static void main(String[] args) {
    Servant[] threads = new Servant[4];
    ArrayList<Integer> GiftBag = new ArrayList<Integer>();
    Node GiftListHead = new Node(0);
    for (int i = 1; i < 500001; i++) {
      GiftBag.add(i);
    }
    Collections.shuffle(GiftBag);

    for (int i = 0; i < 4; i++) {
      threads[i] = new Servant();
      threads[i].start();
    }
    for (int i = 0; i < 4; i++) {
      while (!GiftBag.isEmpty()) {
        threads[i].addGift2List(GiftListHead, GiftBag.remove(0));
      }
    }
  }
}

class Servant extends Thread {
  public void addGift2List(Node head, int giftID) {
    head.add(head, giftID);
    System.out.println("thank you note for " + giftID);
  }
}

class Window {
  public Node pred;
  public Node curr;
  public Window(Node pred, Node curr) {
    this.pred = pred;
    this.curr = curr;
  }
}

class Node {
  int key;
  AtomicMarkableReference<Node> next;

  public Node(int key) {
    this.key = key;
    this.next = null;
  }

  public boolean remove(Node head, int key) {
    boolean snip;
    while (true) {
      Window window = find(head, key);
      Node pred = window.pred;
      Node curr = window.curr;
      if (curr.key != key) {
        return false;
      } else {
        Node succ = curr.next.getReference();
        snip = curr.next.compareAndSet(succ, succ, false, true);
        if (!snip) continue;
        pred.next.compareAndSet(curr, succ, false, false);
        return true;
      }
    }
  }
  public boolean add(Node head, int key) {
    boolean splice;
    while (true) {
      Window window = find(head, key);
      Node pred = window.pred;
      Node curr = window.curr;
      if (curr.key == key) {
        return false;
      } else {
        Node newNode = new Node(key);
        newNode.next = new AtomicMarkableReference<Node>(curr, false);
        if (pred.next.compareAndSet(curr, newNode, false, false)) {
          return true;
        }
      }
    }
  }
  public static Window find(Node head, int key) {
    Node pred = null;
    Node curr = null;
    Node succ = null;
    boolean[] marked = {false};
    boolean snip;
    retry: while (true) {
      pred = head;
      curr = pred.next.getReference();
      while (true) {
        succ = curr.next.get(marked);
        while (marked[0]) {
          snip = pred.next.compareAndSet(curr, succ, false, false);
          if (!snip) continue retry;
          curr = succ;
          succ = curr.next.get(marked);
        }
        if (curr.key >= key) {
          return new Window(pred, curr);
        }
        pred = curr;
        curr = succ;
      }
    }
  }
}
