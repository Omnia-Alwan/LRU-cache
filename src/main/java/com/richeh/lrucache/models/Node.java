package com.richeh.lrucache.models;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Node {
    //generic key
    private String key;
    //generic value
    private Object value;
    //ref next
    private Node next;
    //ref prev
    private Node prev;

    public Node(String key, Object value){
        this.key= key;
        this.value= value;
    }
}
