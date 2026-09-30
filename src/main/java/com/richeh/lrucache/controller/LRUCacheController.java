package com.richeh.lrucache.controller;

import com.richeh.lrucache.models.Node;
import com.richeh.lrucache.service.LRUService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class LRUCacheController {
    private final LRUService lruService = new LRUService();

    @PostMapping("/{capacity}")
    public void setLruCapacity(@PathVariable int capacity){
        lruService.setLruCacheCapacity(capacity);
    }

    @GetMapping("/{key}")
    public ResponseEntity<Object> get(@PathVariable Object key){
        Node node = lruService.get(key);
        if(node == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().body(node.getKey());
    }
    @GetMapping()
    public void getAll(){
        lruService.displayAll();
    }
    @PostMapping
    public ResponseEntity<Object> put(@RequestBody Node node){
        return ResponseEntity.ok().body(lruService.put(node).getKey());
    }
}
