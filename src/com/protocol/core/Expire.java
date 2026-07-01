package com.protocol.core;

import java.util.*;

public class Expire {
    private static final int SAMPLE_SIZE = 20;
    private static final float STOP_RATIO = 0.25f;

    Random random = new Random();

    private final Store store = new Store();

    public void activeExpireCycle() {

        if (store.getHmObjectMap().isEmpty()) {
            return;
        }

        while (true) {

            int expired = 0;

            List<String> expireKeys = new ArrayList<>(store.getHmObjectMap().keySet());

            int sampleSize = Math.min(SAMPLE_SIZE, expireKeys.size());

            Set<Integer> sampled = new HashSet<>();

            while (sampled.size() < sampleSize) {
                sampled.add(random.nextInt(expireKeys.size()));
            }

            List<String> keysToDelete = new ArrayList<>();

            long now = System.currentTimeMillis();

            for (int index : sampled) {

                String key = expireKeys.get(index);
                long expireAt = store.getHmObjectMap().get(key).expiresAt;

                if (expireAt <= now) {
                    expired++;
                    keysToDelete.add(key);
                }
            }

            for (String key : keysToDelete) {
                store.getHmObjectMap().remove(key);

            }

            float ratio = (float) expired / sampleSize;
             System.out.println("deleted the expired keys  but undeleted keys. total keys " +  store.getHmObjectMap().size());
            // Continue if many sampled keys were expired
            if (ratio < STOP_RATIO) {
                break;
            }
        }
    }
}
