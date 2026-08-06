package com.sandbox.server.playground;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * To samo co {@link ThreadsOldStyle}, ale na wątkach wirtualnych (Java 21).
 * Kod zadań jest IDENTYCZNY - blokujący. Zmienia się tylko sposób
 * tworzenia wątków i to, ile ich może istnieć naraz.
 */
public class ThreadsVirtual {

    static String callApi(int id) throws InterruptedException {
        Thread.sleep(100);   // wątek wirtualny NIE blokuje tu wątku systemowego
        return "odpowiedz-" + id;
    }

    // ------------------------------------------------------------
    // 1. Jeden wątek uruchomiony ręcznie
    // ------------------------------------------------------------
    static void jedenWatek() throws Exception {
        Thread t = Thread.ofVirtual().start(() ->     // zamiast new Thread(...)
                System.out.println("  robie cos w watku: " + Thread.currentThread()));

        t.join();   // join dziala tak samo
    }

    // ------------------------------------------------------------
    // 2. "Pula" + odbieranie wyników
    // ------------------------------------------------------------
    // To NIE jest pula - executor tworzy nowy wątek na KAŻDE zadanie.
    // Wątków wirtualnych się nie puluje, bo ich stworzenie jest tanie.
    static void pulaIWyniki() throws Exception {
        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {

            List<Future<String>> futures = new ArrayList<>();
            for (int i = 1; i <= 4; i++) {
                int id = i;
                futures.add(exec.submit(() -> callApi(id)));
            }

            for (Future<String> f : futures) {
                System.out.println("  " + f.get());
            }
        }   // close() czeka na zakonczenie zadan - stad brak shutdown()
    }

    // ------------------------------------------------------------
    // 3. Skala: 200 zadań I/O, każde na własnym wątku
    // ------------------------------------------------------------
    // Brak puli = brak wąskiego gardła. Wszystkie 200 zadań startuje naraz
    // i czeka równolegle, więc całość trwa tyle, co jedno zadanie.
    static void skala() throws Exception {
        long start = System.currentTimeMillis();

        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                int id = i;
                futures.add(exec.submit(() -> callApi(id)));
            }
            for (Future<String> f : futures) {
                f.get();
            }
        }

        System.out.println("  200 zadan po 100 ms, watek na zadanie: "
                + (System.currentTimeMillis() - start) + " ms");
    }

    // ------------------------------------------------------------
    // 4. Naprawdę duża skala - 100 000 wątków
    // ------------------------------------------------------------
    // Na wątkach platformowych to by sie nie udalo: 100 000 x ~1 MB stosu.
    // Watek wirtualny startuje z kilkuset bajtami na stercie i rosnie w miare potrzeb.
    static void duzaSkala() throws Exception {
        long start = System.currentTimeMillis();

        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 100_000; i++) {
                exec.submit(() -> {
                    Thread.sleep(100);
                    return null;
                });
            }
        }

        System.out.println("  100 000 watkow: " + (System.currentTimeMillis() - start) + " ms");
    }

    // ------------------------------------------------------------
    // 5. Ograniczanie współbieżności
    // ------------------------------------------------------------
    // Kiedys rozmiar puli byl ukrytym limitem: 10 watkow = max 10 zapytan do bazy.
    // Teraz tego limitu nie ma, wiec 200 watkow ruszyloby na pule polaczen naraz.
    // Limit trzeba nalozyc jawnie - na ZASOB, nie na watki.
    static void limit() throws Exception {
        Semaphore polaczeniaDb = new Semaphore(10);
        long start = System.currentTimeMillis();

        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 200; i++) {
                int id = i;
                exec.submit(() -> {
                    polaczeniaDb.acquire();
                    try {
                        return callApi(id);
                    } finally {
                        polaczeniaDb.release();
                    }
                });
            }
        }

        System.out.println("  200 zadan, semafor na 10: "
                + (System.currentTimeMillis() - start) + " ms");
    }

    // ------------------------------------------------------------
    // 6. Wspólny stan - tu NIC się nie zmienia
    // ------------------------------------------------------------
    // Watki wirtualne sa tak samo wspolbiezne jak platformowe.
    // Wyscigi wygladaja identycznie i rozwiazuje sie je identycznie.
    static int licznikBezOchrony = 0;

    static void wspolnyStan() throws Exception {
        licznikBezOchrony = 0;
        var licznikAtomowy = new java.util.concurrent.atomic.AtomicInteger();

        try (ExecutorService exec = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                exec.submit(() -> {
                    licznikBezOchrony++;
                    licznikAtomowy.incrementAndGet();
                });
            }
        }

        System.out.println("  oczekiwane:     10000");
        System.out.println("  bez ochrony:    " + licznikBezOchrony + "  <- ten sam problem");
        System.out.println("  AtomicInteger:  " + licznikAtomowy.get());
    }

    public static void main(String[] args) throws Exception {
        System.out.println("1. jeden watek");
        jedenWatek();

        System.out.println("\n2. watek na zadanie + wyniki");
        pulaIWyniki();

        System.out.println("\n3. skala");
        skala();

        System.out.println("\n4. duza skala");
        duzaSkala();

        System.out.println("\n5. limit przez semafor");
        limit();

        System.out.println("\n6. wspolny stan");
        wspolnyStan();
    }
}

/*
WYJŚCIE:

1. jeden watek
  robie cos w watku: VirtualThread[#35]/runnable@ForkJoinPool-1-worker-1
                     ^ widac carriera, na ktorym wisi watek wirtualny

2. watek na zadanie + wyniki
  odpowiedz-1
  odpowiedz-2
  odpowiedz-3
  odpowiedz-4

3. skala
  200 zadan po 100 ms, watek na zadanie: 105 ms     <- stary styl: 2005 ms

4. duza skala
  100 000 watkow: 326 ms                            <- na platformowych: OutOfMemoryError

5. limit przez semafor
  200 zadan, semafor na 10: 2004 ms                 <- tyle co pula 10 watkow

6. wspolny stan
  oczekiwane:     10000
  bez ochrony:    9685  <- ten sam problem (kazdy przebieg da inna liczbe)
  AtomicInteger:  10000

PODSUMOWANIE:
  - kod zadania sie NIE zmienia, dalej piszesz blokujaco
  - zysk tylko przy I/O (pkt 3 i 4); przy liczeniu na CPU zysku nie ma
  - pkt 5: rozmiar puli byl darmowym limitem, teraz trzeba go dodac samemu
  - pkt 6: problemy ze wspolnym stanem zostaja bez zmian
*/
