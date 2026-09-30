package com.richeh.lrucache.service;

import com.richeh.lrucache.models.DoublyLinkedList;
import com.richeh.lrucache.models.LRUCache;
import com.richeh.lrucache.models.Node;
import org.springframework.stereotype.Service;

import java.util.HashMap;
@Service
public class LRUService {
    private final LRUCache lruCache = new LRUCache();

    public void setLruCacheCapacity(int capacity){
        lruCache.setCapacity(capacity);
    }
    //@Transactional
    public synchronized Node get(Object key) {
        key = key.toString();
        //look for key in hashmap
        HashMap<String, Node> hashMap = lruCache.getHashMap();
        DoublyLinkedList linkedList = lruCache.getLinkedList();
        Node getNode = hashMap.get(key);
        System.out.println("GET method ------------------------------------");
        System.out.println("Key's class passed from request: "+key.getClass()+" "+key);
        hashMap.forEach((k,n)-> System.out.println("Key's class in hashmap: "+k.getClass()+", key: "+k));
        System.out.println(hashMap.keySet());
        if (!hashMap.containsKey(key)) {
            return null;
        }
        linkedList.moveToFront(getNode);
        System.out.println("--------------------------------------------------");
        return getNode;
    }//@Transactional
    public synchronized Node put(Node node){
        //if capacity is reached, evict last node
        //add node to hashmap
        HashMap<String, Node> hashMap = lruCache.getHashMap();
        DoublyLinkedList linkedList = lruCache.getLinkedList();
        //if node already exists, then move to front. Avoid duplicate nodes in list
        if(hashMap.containsKey(node.getKey())){
            linkedList.moveToFront(hashMap.get(node.getKey()));//VERY IMPORTANT USE OF HASHMAP
        }else linkedList.addFirst(node);

        hashMap.put(node.getKey(),node);
        if(hashMap.size()>lruCache.getCapacity()){
            System.out.println("capacity has been reached! evicting last node... ");
            hashMap.remove(linkedList.removeLast().getKey());
        }
        return node;
    }
    public void displayAll(){
        System.out.println("displayAll method----------------------------------------------------");
        System.out.println("Capcaity and size: "+lruCache.getCapacity()+ " "+lruCache.getHashMap().size());
        lruCache.getLinkedList().displayAllNodes();
        System.out.println("----------------------------------------------------------------------");
    }
}
