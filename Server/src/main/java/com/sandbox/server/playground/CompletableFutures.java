package com.sandbox.server.playground;

import java.util.*;
import java.util.concurrent.*;

public class CompletableFutures {

    record User(long id, String name) {
    }

    // Dedicated pool for blocking I/O - see point 7 for why this matters.
    static final ExecutorService IO = Executors.newFixedThreadPool(8);

    static User loadUser(long id) {
        sleep(100);
        if (id < 0) throw new IllegalArgumentException("bad id: " + id);
        return new User(id, "user-" + id);
    }

    static List<String> loadOrders(User user) {
        sleep(100);
        return List.of(user.name() + "/order-1", user.name() + "/order-2");
    }

    // ================================================================

    public static void main(String[] args) throws Exception {

        // ------------------------------------------------------------
        // 1. thenApply - map. The function returns a plain value.
        // ------------------------------------------------------------
        String name = CompletableFuture
                .supplyAsync(() -> loadUser(1), IO)
                .thenApply(User::name)                  // User -> String
                .join();
        System.out.println("1. thenApply    : " + name);

        // ------------------------------------------------------------
        // 2. thenCompose - flatMap. The function returns another future.
        // ------------------------------------------------------------
        // With thenApply here you would get CompletableFuture<CompletableFuture<..>>
        // and would have to unwrap it by hand.
        List<String> orders = CompletableFuture
                .supplyAsync(() -> loadUser(2), IO)
                .thenCompose(user -> CompletableFuture.supplyAsync(() -> loadOrders(user), IO))
                .join();
        System.out.println("2. thenCompose  : " + orders);

        // What thenApply gives you instead - note the nested type:
        CompletableFuture<CompletableFuture<List<String>>> nested = CompletableFuture
                .supplyAsync(() -> loadUser(2), IO)
                .thenApply(user -> CompletableFuture.supplyAsync(() -> loadOrders(user), IO));
        System.out.println("   thenApply    : " + nested.join().join() + "  <- two joins needed");

        // ------------------------------------------------------------
        // 3. thenCombine - two INDEPENDENT calls, run in parallel
        // ------------------------------------------------------------
        long start = System.currentTimeMillis();
        String combined = CompletableFuture
                .supplyAsync(() -> loadUser(3), IO)
                .thenCombine(
                        CompletableFuture.supplyAsync(() -> loadUser(4), IO),
                        (a, b) -> a.name() + " + " + b.name()
                )
                .join();
        System.out.println("3. thenCombine  : " + combined
                + "  (" + (System.currentTimeMillis() - start) + " ms, not 200)");

        // ------------------------------------------------------------
        // 4. allOf - N calls in parallel, then collect the results
        // ------------------------------------------------------------
        // allOf returns CompletableFuture<Void>, so the results have to be
        // pulled from the original futures afterwards.
        start = System.currentTimeMillis();
        List<CompletableFuture<User>> futures = new ArrayList<>();
        for (long id = 10; id < 15; id++) {
            long finalId = id;
            futures.add(CompletableFuture.supplyAsync(() -> loadUser(finalId), IO));
        }

        List<User> users = CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList())
                .join();

        System.out.println("4. allOf        : " + users.size() + " users in "
                + (System.currentTimeMillis() - start) + " ms");

        // ------------------------------------------------------------
        // 5. exceptionally - fallback value, only runs on failure
        // ------------------------------------------------------------
        User fallback = CompletableFuture
                .supplyAsync(() -> loadUser(-1), IO)          // throws
                .exceptionally(ex -> {
                    // ex is a CompletionException wrapping the real cause
                    System.out.println("5. exceptionally: caught " + ex.getCause().getMessage());
                    return new User(0, "guest");
                })
                .join();
        System.out.println("   result       : " + fallback);

        // ------------------------------------------------------------
        // 6. handle - sees BOTH outcomes; whenComplete - side effect only
        // ------------------------------------------------------------
        String handled = CompletableFuture
                .supplyAsync(() -> loadUser(-1), IO)
                .handle((user, ex) -> ex != null ? "failed: " + ex.getCause().getMessage()
                                                 : "ok: " + user.name())
                .join();
        System.out.println("6. handle       : " + handled);

        // whenComplete does NOT swallow the exception - it observes and rethrows
        try {
            CompletableFuture
                    .supplyAsync(() -> loadUser(-1), IO)
                    .whenComplete((user, ex) -> System.out.println(
                            "   whenComplete : observed " + (ex != null ? "failure" : "success")))
                    .join();
        } catch (CompletionException e) {
            System.out.println("   still threw  : " + e.getCause().getMessage());
        }

        // ------------------------------------------------------------
        // 7. Explicit executor
        // ------------------------------------------------------------
        // Without the second argument every *Async call runs on
        // ForkJoinPool.commonPool(), sized at (cores - 1) and SHARED with
        // parallel streams and everything else in the JVM. A few blocking
        // calls there and the whole application stalls.
        System.out.println("7. commonPool parallelism: " + ForkJoinPool.getCommonPoolParallelism()
                + "  <- shared by the whole JVM, never block on it");

        CompletableFuture.supplyAsync(() -> loadUser(5), IO)     // <- always pass a pool
                .thenApplyAsync(User::name, IO)                  // thenApply* too
                .join();

        // ------------------------------------------------------------
        // 8. Timeouts
        // ------------------------------------------------------------
        try {
            CompletableFuture
                    .supplyAsync(() -> {
                        sleep(1000);
                        return "slow";
                    }, IO)
                    .orTimeout(200, TimeUnit.MILLISECONDS)       // fails with TimeoutException
                    .join();
        } catch (CompletionException e) {
            System.out.println("8. orTimeout    : " + e.getCause().getClass().getSimpleName());
        }

        String onTimeout = CompletableFuture
                .supplyAsync(() -> {
                    sleep(1000);
                    return "slow";
                }, IO)
                .completeOnTimeout("default", 200, TimeUnit.MILLISECONDS)   // value instead
                .join();
        System.out.println("   completeOnTimeout: " + onTimeout);

        IO.shutdown();
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
        }
    }
}

/*
OUTPUT:

1. thenApply    : user-1
2. thenCompose  : [user-2/order-1, user-2/order-2]
   thenApply    : [user-2/order-1, user-2/order-2]  <- two joins needed
3. thenCombine  : user-3 + user-4  (102 ms, not 200)
4. allOf        : 5 users in 102 ms
5. exceptionally: caught bad id: -1
   result       : User[id=0, name=guest]
6. handle       : failed: bad id: -1
   whenComplete : observed failure
   still threw  : bad id: -1
7. commonPool parallelism: 15  <- shared by the whole JVM, never block on it
8. orTimeout    : TimeoutException
   completeOnTimeout: default

CHEAT SHEET:
  thenApply    - value  -> value            (map)
  thenCompose  - value  -> future           (flatMap; avoids nested futures)
  thenCombine  - two independent futures    (they run in parallel)
  allOf        - N futures; returns Void, join() each one to collect
  anyOf        - first one to finish wins
  exceptionally- fallback value on failure only
  handle       - (value, throwable) -> value; sees both outcomes
  whenComplete - observe only; the exception still propagates
  orTimeout    - fail with TimeoutException
  completeOnTimeout - substitute a default value

TWO RULES:
  1. always pass your own Executor - the default commonPool is shared
     JVM-wide and blocking on it starves parallel streams and everything else
  2. always set a timeout - a future with no timeout waits forever
*/
