import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class TempReading {
  public static void main(String[] args) {
    // Intializing threads
    tempModule[] threads = new tempModule[8];
    for (int i = 0; i < 8; i++) {
      threads[i] = new tempModule();
      threads[i].start();
    }
    // Adding temperatures to shared LinkedList
    Node tempListTail = new Node(1000);
    Node tempListHead = new Node(-1000, tempListTail);
    ArrayList<Integer> chainTrack = new ArrayList<Integer>();
    for (int min = 0; min < 60; min++) {
      for (int i = 0; i < 8; i++) {
        Random r = new Random();
        int randomTemp = r.nextInt(171) - 100;
        threads[i].addTemp2List(tempListHead, randomTemp);
      }
    }
    // Printing the temperatures
    Node tempNode = tempListHead;
    tempNode = tempNode.next.getReference();
    System.out.printf("5 lowest temps recorded this hour: [");
    for (int i = 0; i < 4; i++) {
      System.out.printf("%d, ", tempNode.item);
      tempNode = tempNode.next.getReference();
    }
    System.out.println(tempNode.item + "]");
    Queue<Integer> highTemp = new LinkedList<>();
    while (tempNode.next != null) {
      if (highTemp.size() > 4) {
        highTemp.remove();
        highTemp.add(tempNode.item);
      }
      else {
        highTemp.add(tempNode.item);
      }
      tempNode = tempNode.next.getReference();
    }
    System.out.printf("5 highest temps recorded this hour: ");
    System.out.println(highTemp);
  }
}

class tempModule extends Thread {
  public void addTemp2List(Node head, int temp) {
    head.add(temp, head);
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
