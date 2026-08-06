package com.sandbox.server.playground;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;

/**
 * The four classic memory leaks in a web application. In each case an object
 * that should die when the request ends stays in memory, because something
 * still reaches it from a GC root.
 * <p>
 * WeakReference is only a probe here: get() != null means the object is alive.
 *
 * @see GcRoots for what counts as a GC root
 */
public class MemoryLeaks {

    record Session(String user, byte[] data) {
        Session(String user) {
            this(user, new byte[1024]);
        }
    }

    // ================================================================
    // 1. FORGOTTEN REFERENCE IN A STATIC FIELD
    // ================================================================
    // A static field is a GC root: it lives as long as the class, which
    // usually means the whole lifetime of the application. Someone appends
    // on every request and never removes. The list grows forever.

    static class LeakyRegistry {
        static final List<Session> ACTIVE = new ArrayList<>();   // <-- root

        static void onLogin(Session s) {
            ACTIVE.add(s);
        }
        // no onLogout() -> nothing ever leaves the list
    }

    static class FixedRegistry {
        static final List<Session> ACTIVE = new ArrayList<>();

        static void onLogin(Session s) {
            ACTIVE.add(s);
        }

        static void onLogout(Session s) {
            ACTIVE.remove(s);      // symmetric cleanup
        }
    }

    // ================================================================
    // 2. UNBOUNDED CACHE
    // ================================================================
    // A plain HashMap used as a "cache". Keys keep arriving, nothing is ever
    // evicted, because nobody set a size limit or a TTL. This is a slow-motion
    // leak: fine on dev with 10 keys, fatal in production after a week.

    static class LeakyCache {
        static final Map<String, Session> CACHE = new HashMap<>();   // <-- root

        static Session get(String user) {
            return CACHE.computeIfAbsent(user, Session::new);
        }
    }

    // Bounded LRU: a LinkedHashMap in access-order mode drops the eldest entry
    // once the limit is exceeded. In a real project you would use Caffeine,
    // or @Cacheable with maximumSize / expireAfterWrite configured.
    static class BoundedCache {
        static final int MAX = 3;
        static final Map<String, Session> CACHE = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Session> eldest) {
                return size() > MAX;
            }
        };

        static Session get(String user) {
            return CACHE.computeIfAbsent(user, Session::new);
        }
    }

    // ================================================================
    // 3. ThreadLocal ON A POOLED THREAD
    // ================================================================
    // A ThreadLocal stores its value in a map INSIDE the Thread object, and a
    // live thread is a GC root. In a pool the thread does not die after a TASK -
    // it goes back to the pool carrying the value with it, and only dies when
    // the pool itself is shut down. In a web app that pool lives as long as the
    // application, so with 200 Tomcat threads that is 200 objects retained for
    // months, and request N sees request N-1's data.

    static final ThreadLocal<Session> CONTEXT = new ThreadLocal<>();

    static void handleLeaky(Session session) {
        CONTEXT.set(session);
        // ... handle the request ...
        // no CONTEXT.remove() -> the value stays on the pooled thread
    }

    static void handleFixed(Session session) {
        CONTEXT.set(session);
        try {
            // ... handle the request ...
        } finally {
            CONTEXT.remove();          // ALWAYS in finally
        }
    }

    // ================================================================
    // 4. CLASSLOADER RETENTION ON HOT DEPLOY
    // ================================================================
    // The worst variant, because one leaked reference retains megabytes.
    // In an application server the thread pool outlives the deployed
    // application. If anything owned by the container (a pooled thread's
    // ThreadLocal, a static registry in a JDK or library class, a JDBC driver
    // registered in DriverManager) points at an object from the webapp, then:
    //
    //     instance -> its Class -> the webapp ClassLoader -> ALL its classes
    //
    // The whole ClassLoader is pinned, so every class it ever loaded, plus all
    // their static fields, survives the undeploy. Redeploy a few times and the
    // metaspace is gone. The demo below shortens the chain to a direct
    // reference; the retention mechanism is identical.

    static class WebappClassLoader extends ClassLoader {
        private final byte[] loadedClasses = new byte[64 * 1024];   // pretend metadata

        WebappClassLoader() {
            super("webapp", ClassLoader.getPlatformClassLoader());
        }
    }

    // ================================================================

    public static void main(String[] args) throws Exception {

        // --- 1. static field ---
        Session s1 = new Session("anna");
        WeakReference<Session> leakyStatic = new WeakReference<>(s1);
        LeakyRegistry.onLogin(s1);
        s1 = null;                                  // the "request" ended

        Session s2 = new Session("bartek");
        WeakReference<Session> fixedStatic = new WeakReference<>(s2);
        FixedRegistry.onLogin(s2);
        FixedRegistry.onLogout(s2);
        s2 = null;

        gc();
        System.out.println("1. static list, no remove()  : " + alive(leakyStatic));
        System.out.println("1. static list with remove() : " + alive(fixedStatic));

        // --- 2. cache ---
        for (int i = 0; i < 100; i++) {
            LeakyCache.get("user" + i);
            BoundedCache.get("user" + i);
        }
        gc();
        System.out.println("\n2. HashMap as a cache, size : " + LeakyCache.CACHE.size());
        System.out.println("2. bounded LRU (max " + BoundedCache.MAX + "), size : " + BoundedCache.CACHE.size());

        // --- 3. ThreadLocal on pooled threads ---
        // TWO separate single-thread pools. On one shared pool the remove()
        // from the "fixed" variant would wipe the entry left by the "leaky"
        // one - both tasks would land on the same thread and hide the leak.
        ExecutorService leakyPool = Executors.newFixedThreadPool(1);
        ExecutorService fixedPool = Executors.newFixedThreadPool(1);

        WeakReference<Session> leakyTl = leakyPool.submit(() -> {
            Session session = new Session("celina");
            handleLeaky(session);
            return new WeakReference<>(session);
        }).get();

        WeakReference<Session> fixedTl = fixedPool.submit(() -> {
            Session session = new Session("dawid");
            handleFixed(session);
            return new WeakReference<>(session);
        }).get();

        // does the NEXT task on the same thread see the previous one's data?
        String seenLeaky = leakyPool.submit(MemoryLeaks::peek).get();
        String seenFixed = fixedPool.submit(MemoryLeaks::peek).get();

        gc();
        System.out.println("\n3. ThreadLocal, no remove() : " + alive(leakyTl));
        System.out.println("3. ThreadLocal with remove(): " + alive(fixedTl));
        System.out.println("3. next task sees           : " + seenLeaky);
        System.out.println("3. same, but with remove()  : " + seenFixed);

        // --- 4. classloader retention ---
        ExecutorService container = Executors.newFixedThreadPool(1);
        ThreadLocal<WebappClassLoader> containerHeld = new ThreadLocal<>();

        WeakReference<WebappClassLoader> undeployed = container.submit(() -> {
            WebappClassLoader loader = new WebappClassLoader();
            containerHeld.set(loader);          // the container "forgets" this
            return new WeakReference<>(loader);
        }).get();

        gc();
        System.out.println("\n4. after undeploy           : " + alive(undeployed));

        // the container cleans up its ThreadLocal on undeploy
        container.submit(containerHeld::remove).get();
        gc();
        System.out.println("4. after cleanup            : " + alive(undeployed));

        leakyPool.shutdown();
        fixedPool.shutdown();
        container.shutdown();
    }

    static String peek() {
        Session leftover = CONTEXT.get();
        return leftover == null ? "null (clean)" : leftover.user() + " (someone else's data!)";
    }

    static String alive(WeakReference<?> ref) {
        return ref.get() != null ? "ALIVE (leak)" : "collected";
    }

    static void gc() throws InterruptedException {
        System.gc();
        Thread.sleep(100);
    }
}

/*
OUTPUT:

1. static list, no remove()  : ALIVE (leak)
1. static list with remove() : collected

2. HashMap as a cache, size : 100
2. bounded LRU (max 3), size : 3

3. ThreadLocal, no remove() : ALIVE (leak)
3. ThreadLocal with remove(): collected
3. next task sees           : celina (someone else's data!)
3. same, but with remove()  : null (clean)

4. after undeploy           : ALIVE (leak)
4. after cleanup            : collected

Line "3. next task sees" is not just a memory leak but a security bug:
user B's request gets user A's context from the same pooled thread.
Hence the pattern: set() ... try { ... } finally { remove(); }

Case 4 is why application servers log
"The web application created a ThreadLocal but failed to remove it"
on undeploy - Tomcat actively hunts for exactly this leak.
*/
