import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class BagOfGifts {
  public static void main(String[] args) {
    ArrayList<Integer> giftBag = new ArrayList<Integer>();
    for (int i = 1; i < 500001; i++) {
      giftBag.add(i);
    }
    Collections.shuffle(giftBag);

    Servant[] threads = new Servant[4];
    for (int i = 0; i < 4; i++) {
      threads[i] = new Servant();
      threads[i].start();
    }

    Node giftListTail = new Node(999999);
    Node giftListHead = new Node(-1, giftListTail);
    ArrayList<Integer> chainTrack = new ArrayList<Integer>();
    int addOrRem = 2;
    while (!giftBag.isEmpty()) {
      if ((addOrRem % 2) == 0) {
        for (int i = 0; i < 4; i++) {
          if (giftBag.isEmpty()) continue;
          int giftID = giftBag.remove(0);
          chainTrack.add(giftID);
          threads[i].addGift2List(giftListHead, giftID);
        }
      }
      else {
        for (int i = 0; i < 4; i++) {
          if (chainTrack.isEmpty()) continue;
          int giftID = chainTrack.remove(0);
          threads[i].WriteThankYou(giftListHead, giftID);
        }
      }
      addOrRem++;
    }

    while (!chainTrack.isEmpty()) {
      for (int i = 0; i < 4; i++) {
        if (chainTrack.isEmpty()) continue;
        int giftID = chainTrack.remove(0);
        threads[i].WriteThankYou(giftListHead, giftID);
      }
    }
    /*
    Node tempNode = giftListHead;
    while (tempNode.next != null) {
      System.out.println(tempNode.item);
      tempNode = tempNode.next.getReference();
    }*/

  }
}

class Servant extends Thread {
  public void addGift2List(Node head, int giftID) {
    head.add(giftID, head);
  }
  public void WriteThankYou(Node head, int giftID) {
    if (head.remove(giftID, head)) {
      System.out.println("Thank you note for gift #" + giftID);
    }
  }
}

// Code based on slide 230 ch.9
class Window {
  public Node pred;
  public Node curr;
  public Window(Node pred, Node curr) {
    this.pred = pred;
    this.curr = curr;
  }
}

// Code based on slides 230, 235, 242, 248 ch.9
class Node {
  int item;
  AtomicMarkableReference<Node> next;

  public Node(int item) {
    this.item = item;
    this.next = null;
  }
  public Node(int item, Node next) {
    this.item = item;
    AtomicMarkableReference<Node> atomicNext = new AtomicMarkableReference<>(next, false);
    this.next = atomicNext;
  }


  public boolean remove(int item, Node head) {
    boolean snip;
    while (true) {
      Window window = find(item, head);
      Node pred = window.pred;
      Node curr = window.curr;
      if (curr.item != item) {
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
  public boolean add(int item, Node head) {
    boolean splice;
    while (true) {
      Window window = find(item, head);
      Node pred = window.pred;
      Node curr = window.curr;
      if (curr.item == item) {
        return false;
      } else {
        Node newNode = new Node(item);
        newNode.next = new AtomicMarkableReference<Node>(curr, false);
        if (pred.next.compareAndSet(curr, newNode, false, false)) {
          return true;
        }
        return false;
      }
    }
  }
  public static Window find(int item, Node head) {
    Node pred = null;
    Node curr = null;
    Node succ = null;
    boolean[] marked = {false};
    boolean snip;
    retry: while (true) {
      pred = head;
      if (pred.next == null) {
        return new Window(pred, curr);
      }
      curr = pred.next.getReference();
      while (true) {
        if (curr.next != null) {
          succ = curr.next.get(marked);
        }
        while (marked[0]) {
          snip = pred.next.compareAndSet(curr, succ, false, false);
          if (!snip) continue retry;
          curr = succ;
          succ = curr.next.get(marked);
        }
        if (curr.item >= item) {
          return new Window(pred, curr);
        }
        pred = curr;
        curr = succ;
      }
    }
  }
}
