package com.protocol.core;

public class Eviction {

    private final Store store;

    public Eviction(Store store) {
        this.store = store;
    }


    //Evicts the first key it found while iterating the map
    //TODO: Make it efficient by doing through sampling
    public void evictFirst(){
        String key = store.getHmObjectMap().keySet().stream()
                .findFirst()
                .orElse(null);
        System.out.println("key to be deleted: " + key);
        store.delete(key);
    }

    //TODO: Make the eviction strategy configuration driven
    //TODO: support multiple eviction strategies
    public void evict(){
          evictFirst();
    }
}
