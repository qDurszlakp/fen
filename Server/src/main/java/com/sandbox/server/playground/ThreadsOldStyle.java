package com.sandbox.server.playground;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Wielowątkowość w starym stylu - wątki platformowe.
 * Odpowiednik z wątkami wirtualnymi: {@link ThreadsVirtual}.
 */
public class ThreadsOldStyle {

    /**
     * Symulacja wywołania I/O: HTTP, zapytanie do bazy. Wątek czeka i nic nie robi.
     */
    static String callApi(int id) throws InterruptedException {
        Thread.sleep(100);
        return "odpowiedz-" + id;
    }

    // ------------------------------------------------------------
    // 1. Jeden wątek uruchomiony ręcznie
    // ------------------------------------------------------------
    static void jedenWatek() throws Exception {
        Thread t = new Thread(() -> System.out.println("  robie cos w watku: "
                + Thread.currentThread().getName()));

        t.start();   // uruchom
        t.join();    // czekaj az skonczy
    }

    // ------------------------------------------------------------
    // 2. Pula wątków + odbieranie wyników
    // ------------------------------------------------------------
    static void pulaIWyniki() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);

        List<Future<String>> futures = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            int id = i;
            futures.add(pool.submit(() -> callApi(id)));   // submit zwraca Future
        }

        for (Future<String> f : futures) {
            System.out.println("  " + f.get());            // get() blokuje do wyniku
        }

        pool.shutdown();
    }

    // ------------------------------------------------------------
    // 3. Skala: 200 zadań I/O na puli 10 wątków
    // ------------------------------------------------------------
    // Watek platformowy = watek systemu operacyjnego. Rezerwuje ~1 MB
    // przestrzeni adresowej na stos (realnie zajmuje kilkadziesiat KB RAM),
    // ale przede wszystkim jest bytem jadra: wpisem w schedulerze,
    // a jego przelaczenie wymaga przejscia do jadra.
    // Dlatego nie zrobisz ich 200 tysiecy - stad pula o stalym rozmiarze.
    // Skutek: 200 zadan musi sie przecisnac przez 10 waskich gardel,
    // czyli 20 tur po 100 ms.
    static void skala() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(10);

        long start = System.currentTimeMillis();

        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            int id = i;
            futures.add(pool.submit(() -> callApi(id)));
        }
        for (Future<String> f : futures) {
            f.get();
        }

        pool.shutdown();
        System.out.println("  200 zadan po 100 ms na puli 10 watkow: "
                + (System.currentTimeMillis() - start) + " ms");
    }

    // ------------------------------------------------------------
    // 4. Wspólny stan - dlaczego w ogóle potrzebujemy synchronizacji
    // ------------------------------------------------------------
    static int licznikBezOchrony = 0;

    static void wspolnyStan() throws Exception {
        licznikBezOchrony = 0;
        var licznikAtomowy = new java.util.concurrent.atomic.AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 10_000; i++) {
            pool.submit(() -> {
                licznikBezOchrony++;        // ++ to odczyt, dodanie i zapis - da sie przerwac
                licznikAtomowy.incrementAndGet();   // niepodzielne
            });
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("  oczekiwane:     10000");
        System.out.println("  bez ochrony:    " + licznikBezOchrony + "  <- zgubione inkrementacje");
        System.out.println("  AtomicInteger:  " + licznikAtomowy.get());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("1. jeden watek");
        jedenWatek();

        System.out.println("\n2. pula + wyniki");
        pulaIWyniki();

        System.out.println("\n3. skala");
        skala();

        System.out.println("\n4. wspolny stan");
        wspolnyStan();
    }
}

/*
WYJŚCIE:

1. jeden watek
  robie cos w watku: Thread-0

2. pula + wyniki
  odpowiedz-1
  odpowiedz-2
  odpowiedz-3
  odpowiedz-4

3. skala
  200 zadan po 100 ms na puli 10 watkow: 2005 ms      <- 20 tur po 100 ms

4. wspolny stan
  oczekiwane:     10000
  bez ochrony:    9829  <- zgubione inkrementacje (kazdy przebieg da inna liczbe)
  AtomicInteger:  10000
*/
