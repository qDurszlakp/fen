package com.sandbox.server.playground;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

/**
 * Trzy sposoby ochrony wspólnego stanu.
 * Uzupełnia {@link ThreadsOldStyle} pkt 4, gdzie licznik był bez ochrony.
 */
public class Locks {

    // ================================================================
    // 1. synchronized - najprostszy, wbudowany w język
    // ================================================================
    // Każdy obiekt w Javie ma monitor. synchronized go zajmuje na wejściu
    // i zwalnia na wyjściu - także gdy poleci wyjątek. Nie da się zapomnieć
    // o zwolnieniu, bo robi to JVM.
    // Wada: brak timeoutu, brak przerwania, brak podziału na odczyt/zapis.
    static class SynchronizedCounter {
        private int value;

        // synchronized na metodzie = zamek na THIS
        synchronized void increment() {
            value++;
        }

        int get() {
            // synchronized na bloku - można wskazać dowolny obiekt jako zamek
            synchronized (this) {
                return value;
            }
        }
    }

    // ================================================================
    // 2. ReentrantLock - to samo, ale sterowane jawnie
    // ================================================================
    // Zawsze unlock() w finally, inaczej wyjątek zostawia zamek zajęty
    // na zawsze. To cena za elastyczność, której nie ma synchronized:
    //   tryLock()            - spróbuj, nie czekaj
    //   tryLock(t, unit)     - czekaj najwyżej t
    //   lockInterruptibly()  - czekanie da się przerwać
    //   new ReentrantLock(true) - kolejka FIFO zamiast wyścigu
    static class LockCounter {
        private final ReentrantLock lock = new ReentrantLock();
        private int value;

        void increment() {
            lock.lock();
            try {
                value++;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Wersja, która nie czeka - gdy zamek zajęty, po prostu odpuszcza.
         */
        boolean incrementIfFree() {
            if (!lock.tryLock()) {
                return false;
            }
            try {
                value++;
                return true;
            } finally {
                lock.unlock();
            }
        }

        int get() {
            lock.lock();
            try {
                return value;
            } finally {
                lock.unlock();
            }
        }
    }

    // ================================================================
    // 3. ReentrantReadWriteLock - wielu czytelników naraz
    // ================================================================
    // readLock()  - dowolnie wielu naraz, o ile nikt nie pisze
    // writeLock() - na wyłączność, blokuje czytelników i innych pisarzy
    // Sens tylko przy przewadze odczytów; przy równych proporcjach
    // narzut zarządzania dwoma zamkami zjada zysk.
    static class Cache {
        private final ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
        private String data = "stare-dane";

        String read() throws InterruptedException {
            rw.readLock().lock();
            try {
                Thread.sleep(100);   // udajemy kosztowny odczyt
                return data;
            } finally {
                rw.readLock().unlock();
            }
        }

        void write(String value) throws InterruptedException {
            rw.writeLock().lock();
            try {
                Thread.sleep(100);
                data = value;
            } finally {
                rw.writeLock().unlock();
            }
        }
    }

    // Ten sam cache, ale na zwykłym zamku - do porównania czasu
    static class CacheExclusive {
        private final ReentrantLock lock = new ReentrantLock();
        private String data = "stare-dane";

        String read() throws InterruptedException {
            lock.lock();
            try {
                Thread.sleep(100);
                return data;
            } finally {
                lock.unlock();
            }
        }
    }

    // ================================================================

    public static void main(String[] args) throws Exception {

        // --- 1 i 2: poprawność licznika ---
        SynchronizedCounter sync = new SynchronizedCounter();
        LockCounter lock = new LockCounter();

        try (ExecutorService exec = Executors.newFixedThreadPool(8)) {
            for (int i = 0; i < 10_000; i++) {
                exec.submit(sync::increment);
                exec.submit(lock::increment);
            }
        }

        System.out.println("oczekiwane:      10000");
        System.out.println("synchronized:    " + sync.get());
        System.out.println("ReentrantLock:   " + lock.get());

        // --- tryLock: kto nie zdąży, ten odpada ---
        LockCounter busy = new LockCounter();
        int udane = 0;
        for (int i = 0; i < 5; i++) {
            udane += busy.incrementIfFree() ? 1 : 0;
        }
        System.out.println("\ntryLock, 5 prob bez rywalizacji: " + udane + " udanych");

        // --- 3: 4 równoczesne odczyty ---
        System.out.println("\n4 rownoczesne odczyty po 100 ms:");
        System.out.println("  ReadWriteLock:  " + czas4Odczyty(new Cache()) + " ms  <- rownolegle");
        System.out.println("  ReentrantLock:  " + czas4OdczytyExcl(new CacheExclusive()) + " ms  <- po kolei");

        // --- 4: zapis wchodzi między odczyty ---
        System.out.println("\n3 odczyty + 1 zapis, wszystkie naraz:");
        System.out.println("  " + czasZOdczytemIZapisem(new Cache()) + " ms"
                + "  <- zapis blokuje czytelnikow, wiec 2 tury zamiast 1");
    }

    /**
     * Zapis na wyłączność: czytelnicy muszą poczekać, aż pisarz skończy
     * (albo odwrotnie - pisarz czeka na czytelników). Stąd ~200 ms zamiast 100 ms.
     */
    static long czasZOdczytemIZapisem(Cache cache) throws Exception {
        long start = System.currentTimeMillis();
        try (ExecutorService exec = Executors.newFixedThreadPool(4)) {
            exec.submit(() -> {
                cache.write("nowe-dane");
                return null;
            });
            for (int i = 0; i < 3; i++) {
                exec.submit(cache::read);
            }
        }
        return System.currentTimeMillis() - start;
    }

    static long czas4Odczyty(Cache cache) throws Exception {
        long start = System.currentTimeMillis();
        try (ExecutorService exec = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < 4; i++) {
                exec.submit(cache::read);
            }
        }
        return System.currentTimeMillis() - start;
    }

    static long czas4OdczytyExcl(CacheExclusive cache) throws Exception {
        long start = System.currentTimeMillis();
        try (ExecutorService exec = Executors.newFixedThreadPool(4)) {
            for (int i = 0; i < 4; i++) {
                exec.submit(cache::read);
            }
        }
        return System.currentTimeMillis() - start;
    }
}

/*
WYJŚCIE:

oczekiwane:      10000
synchronized:    10000
ReentrantLock:   10000

tryLock, 5 prob bez rywalizacji: 5 udanych

4 rownoczesne odczyty po 100 ms:
  ReadWriteLock:  101 ms  <- rownolegle
  ReentrantLock:  401 ms  <- po kolei

3 odczyty + 1 zapis, wszystkie naraz:
  201 ms  <- zapis blokuje czytelnikow, wiec 2 tury zamiast 1

KIEDY CO:
  synchronized   - domyślny wybór, gdy sekcja krytyczna jest krótka i prosta
  ReentrantLock  - gdy potrzebny timeout, tryLock, przerywanie lub uczciwość
  ReadWriteLock  - gdy odczytów jest DUŻO więcej niż zapisów

WĄTKI WIRTUALNE:
  Wszystkie trzy czekają przez LockSupport.park(), więc odmontowują wątek
  wirtualny i zwalniają carriera. Na JDK 21-23 wyjątkiem był synchronized,
  ktory przypinal - naprawione w JDK 24 (JEP 491).
*/
