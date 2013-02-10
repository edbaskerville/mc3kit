package mc3kit.graph;

public class Edge {
  Node tail;
  Node head;
  int id;
  String name;
  Graph graph;
  
  public Edge(Node tail, Node head) {
    this.tail = tail;
    this.head = head;
    id = -1;
  }
  
  public Edge(String name, Node tail, Node head) {
    this(tail, head);
    this.name = name;
  }
}
