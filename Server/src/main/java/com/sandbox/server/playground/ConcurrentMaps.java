package com.sandbox.server.playground;

import java.util.*;
import java.util.concurrent.*;

/**
 * ConcurrentHashMap vs Collections.synchronizedMap.
 * <p>
 * The point: synchronizedMap makes every single METHOD atomic, but not a
 * SEQUENCE of methods. Anything shaped like "read, decide, write" is a race
 * unless you lock the whole sequence yourself. CHM ships those sequences
 * as single atomic operations.
 */
public class ConcurrentMaps {

    static final int THREADS = 8;
    static final int INCREMENTS = 10_000;
    static final String KEY = "hits";

    // ================================================================
    // A. synchronizedMap + read-modify-write  ->  BROKEN
    // ================================================================
    // get() is atomic. put() is atomic. Between them the lock is released,
    // so another thread reads the same old value and both write the same new
    // one - exactly the lost-update problem from ThreadsOldStyle.
    static int synchronizedMapBroken() throws Exception {
        Map<String, Integer> map = Collections.synchronizedMap(new HashMap<>());
        map.put(KEY, 0);

        run(() -> map.put(KEY, map.get(KEY) + 1));

        return map.get(KEY);
    }

    // ================================================================
    // B. synchronizedMap + manual lock  ->  correct, but coarse
    // ================================================================
    // You must synchronize on the map itself - that is the same monitor the
    // wrapper uses internally. Cost: the whole map is locked for the whole
    // sequence, so no two threads ever touch it at the same time.
    static int synchronizedMapManual() throws Exception {
        Map<String, Integer> map = Collections.synchronizedMap(new HashMap<>());
        map.put(KEY, 0);

        run(() -> {
            synchronized (map) {
                map.put(KEY, map.get(KEY) + 1);
            }
        });

        return map.get(KEY);
    }

    // ================================================================
    // C. ConcurrentHashMap + read-modify-write  ->  STILL BROKEN
    // ================================================================
    // Swapping the map type alone fixes nothing. The race is in YOUR code,
    // between two separate calls - not inside the map.
    static int chmBroken() throws Exception {
        Map<String, Integer> map = new ConcurrentHashMap<>();
        map.put(KEY, 0);

        run(() -> map.put(KEY, map.get(KEY) + 1));

        return map.get(KEY);
    }

    // ================================================================
    // D. ConcurrentHashMap + merge  ->  correct, and only the bucket is locked
    // ================================================================
    static int chmMerge() throws Exception {
        Map<String, Integer> map = new ConcurrentHashMap<>();

        run(() -> map.merge(KEY, 1, Integer::sum));

        return map.get(KEY);
    }

    // ================================================================
    // The atomic operations - available on BOTH maps
    // ================================================================
    // Common misconception: that putIfAbsent / merge / compute exist only on
    // ConcurrentHashMap. They are Map default methods, and Collections
    // .SynchronizedMap overrides every one of them (since Java 8):
    //
    //     public V merge(K key, V value, BiFunction<...> f) {
    //         synchronized (mutex) { return m.merge(key, value, f); }
    //     }
    //
    // So both maps give you atomicity here. The difference is HOW MUCH gets
    // locked: CHM locks one bucket, the wrapper locks the entire map.
    static void atomicOperations() {
        Map<String, Integer> chm = new ConcurrentHashMap<>();
        Map<String, Integer> sync = Collections.synchronizedMap(new HashMap<>());

        for (Map<String, Integer> map : List.of(chm, sync)) {
            map.putIfAbsent(KEY, 0);                    // insert only if missing
            map.merge(KEY, 1, Integer::sum);            // combine with the old value
            map.computeIfAbsent("other", k -> expensive());   // build only if missing
            map.compute(KEY, (k, v) -> v > 100 ? null : v + 1);   // null removes the entry
            map.replace(KEY, 5, 6);                     // CAS: only if it still equals 5
            map.remove(KEY, 6);                         // remove only if it still equals 6
        }
    }

    // ================================================================
    // What you DO have to write by hand - on either map
    // ================================================================
    // Any sequence YOU compose out of several calls. No map can make that
    // atomic for you, because it never sees it as one operation.
    static void compoundByHand() {
        Map<String, Integer> sync = Collections.synchronizedMap(new HashMap<>());

        // BROKEN on both map types - two calls, a gap in between
        // if (!map.containsKey(KEY)) map.put(KEY, 0);

        // synchronizedMap: lock on the map itself, that is the wrapper's monitor
        synchronized (sync) {
            if (!sync.containsKey(KEY)) sync.put(KEY, 0);
        }

        // ConcurrentHashMap: express it as ONE atomic call instead
        Map<String, Integer> chm = new ConcurrentHashMap<>();
        chm.computeIfAbsent(KEY, k -> 0);
    }

    // ================================================================
    // Iteration
    // ================================================================
    // synchronizedMap: the iterator is NOT synchronized. Iterating while
    // another thread writes throws ConcurrentModificationException, so you
    // have to hold the lock for the whole loop.
    // CHM: weakly consistent iterator - never throws, may or may not show
    // entries added during the walk.
    static String iterate(Map<Integer, Integer> map) throws Exception {
        for (int i = 0; i < 10_000; i++) {
            map.put(i, i);
        }

        Thread writer = new Thread(() -> {
            for (int i = 10_000; i < 40_000; i++) {
                map.put(i, i);
            }
        });

        try {
            writer.start();
            long sum = 0;
            for (Integer v : map.values()) {     // the walk
                sum += v;
            }
            writer.join();
            return "walked, no exception";
        } catch (ConcurrentModificationException e) {
            writer.join();
            return "ConcurrentModificationException";
        }
    }

    // ================================================================

    public static void main(String[] args) throws Exception {
        int expected = THREADS * INCREMENTS;
        System.out.println("expected: " + expected + "\n");

        System.out.println("A. synchronizedMap + get/put  : " + synchronizedMapBroken() + "  <- lost updates");
        System.out.println("B. synchronizedMap + manual   : " + synchronizedMapManual());
        System.out.println("C. ConcurrentHashMap + get/put: " + chmBroken() + "  <- lost updates too!");
        System.out.println("D. ConcurrentHashMap + merge  : " + chmMerge());

        atomicOperations();
        compoundByHand();

        System.out.println("\niteration while another thread writes:");
        System.out.println("  synchronizedMap  : " + iterate(Collections.synchronizedMap(new HashMap<>())));
        System.out.println("  ConcurrentHashMap: " + iterate(new ConcurrentHashMap<>()));
    }

    static int expensive() {
        return 42;
    }

    /**
     * Runs the given action THREADS x INCREMENTS times, concurrently.
     */
    static void run(Runnable action) throws Exception {
        try (ExecutorService exec = Executors.newFixedThreadPool(THREADS)) {
            for (int t = 0; t < THREADS; t++) {
                exec.submit(() -> {
                    for (int i = 0; i < INCREMENTS; i++) {
                        action.run();
                    }
                });
            }
        }
    }
}

/*
OUTPUT:

expected: 80000

A. synchronizedMap + get/put  : 18259  <- lost updates
B. synchronizedMap + manual   : 80000
C. ConcurrentHashMap + get/put: 19948  <- lost updates too!
D. ConcurrentHashMap + merge  : 80000

iteration while another thread writes:
  synchronizedMap  : ConcurrentModificationException
  ConcurrentHashMap: walked, no exception

(A and C vary between runs - that is what a race looks like.)

TAKEAWAYS:
  - swapping HashMap for ConcurrentHashMap fixes NOTHING on its own (C)
  - the race lives between your two calls, not inside the map
  - merge / compute / computeIfAbsent / putIfAbsent / replace(k,old,new) /
    remove(k,v) are atomic on BOTH maps - SynchronizedMap overrides them all
  - the real differences are:
      granularity - CHM locks one bucket, the wrapper locks the whole map
      iteration   - the wrapper needs the lock held for the entire loop,
                    CHM iterators are weakly consistent and never throw
  - a sequence you compose yourself is never atomic on either map
*/
