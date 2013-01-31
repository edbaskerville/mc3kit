package mc3kit.graph;

public class Edge {
  Node tail;
  Node head;
  int id;
  Graph graph;
  
  public Edge(Node tail, Node head) {
    this.tail = tail;
    this.head = head;
    id = -1;
  }
}
