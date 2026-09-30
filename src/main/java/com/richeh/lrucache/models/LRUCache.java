package com.richeh.lrucache.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Setter
@Getter
@Component
public class LRUCache {
    private DoublyLinkedList linkedList = new DoublyLinkedList();
    private int capacity;
    private HashMap<String, Node> hashMap = new HashMap<>();

    public LRUCache(){
        this.capacity = 4;
    }
}
