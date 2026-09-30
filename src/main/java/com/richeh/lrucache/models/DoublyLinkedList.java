package com.richeh.lrucache.models;

import lombok.Getter;
import org.springframework.stereotype.Component;



@Getter
@Component
public class DoublyLinkedList {
    //head
    private final Node head;
    //tail
    private final Node tail;

    public DoublyLinkedList(){
        head = new Node("", new Object());
        tail = new Node("", new Object());
        head.setNext(tail);
        tail.setPrev(head);
    }

    public void addFirst(Node node) {
        node.setNext(head.getNext());
        node.setPrev(head);
        head.getNext().setPrev(node);
        head.setNext(node);
    }

    public void remove(Node node) {
        //nodex -> node ->nodey
        Node nodey = node.getNext();
        Node nodex = node.getPrev();
        //nodex -> nodey
        nodex.setNext(nodey);
        //nodex <- nodey
        nodey.setPrev(nodex);
        //remove node from list
        node.setPrev(null);
        node.setNext(null);
    }

    public void moveToFront(Node node) {
        remove(node);
        addFirst(node);
    }

    public Node removeLast() {
        //nodex -> node -> tail
        Node node = tail.getPrev();
        if(node == head )
            return null;
        remove(node);
        return node;
    }

    public void displayAllNodes(){
        //head -> node1 .... -> tail
        int c = 1;
        Node curr = head;
        System.out.println("Node head");
        curr = curr.getNext();
        if(curr == tail){
            System.out.println("abort! List is EMPTY");
        }
        while(curr != tail ){
            System.out.println("Node "+c+", Key is: "+curr.getKey()+" , Value is: "+curr.getValue());
            c++;
            curr = curr.getNext();
        }
    }

}
