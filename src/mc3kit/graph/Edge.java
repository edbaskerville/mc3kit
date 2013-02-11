package mc3kit.graph;

import java.util.Observable;

public class Edge extends Observable {
  Node tail;
  Node head;
  String name;
  Graph graph;
  
  public Edge(Node tail, Node head) {
    this.tail = tail;
    this.head = head;
  }
  
  public Edge(String name, Node tail, Node head) {
    this(tail, head);
    this.name = name;
  }
  
  public Node getTail() {
    return tail;
  }
  
  public Node getHead() {
    return head;
  }
}
