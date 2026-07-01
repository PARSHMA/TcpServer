package com.protocol.core;


import java.util.Map;


import java.util.concurrent.ConcurrentHashMap;


public class Store {

     private final Map<String, HmObject> hmObjectMap = new ConcurrentHashMap<>() ;

    private final Eviction eviction;

    public Store() {
        this.eviction = new Eviction(this);
    }

    public static final int KEYS_LIMIT = 5;

    public Map<String, HmObject> getHmObjectMap() {
        return hmObjectMap;
    }

    public  HmObject newObj(Object value, long durationMs) {
        long expiresAt = -1;

        if (durationMs > 0) {
            expiresAt = System.currentTimeMillis() + durationMs;
        }

        return new HmObject(value, expiresAt);
    }

    public  void set(String key, Object value, long durationMs) {
        if(hmObjectMap.size()>= KEYS_LIMIT){
            eviction.evict();
        }
        hmObjectMap.put(key, newObj(value, durationMs));
    }

    public  HmObject get(String key) {
        HmObject obj =  hmObjectMap.get(key);
        return obj;
    }

    public boolean delete(String key){
        if(hmObjectMap.remove(key) != null){
            return true;
        }
        return false;
    }


}
