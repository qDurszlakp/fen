package com.sandbox.server.playground;

import java.util.Arrays;

/**
 * Przegląd kolektorów w OpenJDK - ściąga na rozmowę.
 *
 * <h2>Podstawy wspólne dla wszystkich</h2>
 *
 * <b>Generacyjność.</b> Sterta dzielona na young gen (nowe obiekty) i old gen
 * (obiekty, które przeżyły kilka cykli i zostały promowane). Opiera się na
 * hipotezie generacyjnej: większość obiektów umiera młodo. Dzięki temu częste
 * i tanie zbiórki dotyczą tylko young gen (minor GC), a pełny przegląd sterty
 * (full GC) jest rzadki.
 * <p>
 * <b>Young gen w środku.</b> Eden (tu ląduje nowy obiekt) + dwie przestrzenie
 * Survivor. Żywe obiekty są KOPIOWANE z Edenu do Survivora, reszta jest z
 * miejsca uznana za wolną - nie trzeba przechodzić po śmieciach.
 * <p>
 * <b>Remembered sets + write barrier.</b> Problem: referencja z old gen do
 * young gen (old-to-young). Bez tego zbiórka samego young gen uznałaby taki
 * obiekt za nieosiągalny. JVM utrzymuje metadane o tym, które fragmenty old gen
 * mogą wskazywać na young gen; write barrier to kod doklejany do każdego zapisu
 * referencji, który te metadane aktualizuje.
 * <p>
 * <b>Kompaktowanie.</b> Wszystkie kolektory w OpenJDK przesuwają żywe obiekty
 * blisko siebie, żeby ograniczyć fragmentację. Efekt uboczny jest bardzo
 * korzystny: skoro wolna pamięć jest jednym ciągłym blokiem, alokacja to
 * przesunięcie wskaźnika (bump pointer allocation), a nie szukanie dziury.
 * <p>
 * <b>TLAB</b> (Thread-Local Allocation Buffer) - każdy wątek dostaje własny
 * kawałek Edenu, więc alokuje bez blokad. Dodatkowo ciasno upakowane obiekty
 * lepiej leżą w cache'u CPU.
 *
 * <h2>Kiedy w ogóle tykać ustawień GC</h2>
 *
 * Domyślne ustawienia wystarczają w większości przypadków. Dwa sygnały, że nie:
 * <ul>
 *   <li><b>Latencja</b> - pauzy GC (zwłaszcza full GC) pokrywają się ze skokami
 *       p99 czasu odpowiedzi. Widać to w logach GC albo w APM.</li>
 *   <li><b>Przepustowość</b> - aplikacja zwalnia mimo wysokiego zużycia CPU,
 *       a logi pokazują częste i długie zbiórki. Próg alarmowy: powyżej ~10%
 *       czasu CPU spędzonego w GC.</li>
 * </ul>
 *
 * Źródło: Datadog, "A deep dive into Java garbage collectors" (2025-10-17).
 */
public enum GarbageCollectors {

    /**
     * Najprostszy kolektor. Jeden wątek, wszystkie fazy stop-the-world
     * (aplikacja stoi na czas zbiórki). Generacyjny mimo prostoty.
     * <p>
     * Sens: przy jednym rdzeniu i małej stercie koordynacja wielu wątków
     * kosztowałaby więcej, niż dałaby. JVM sam go wybiera przy < 2 CPU
     * lub stercie < 2 GB.
     */
    SERIAL("-XX:+UseSerialGC",
            Goal.THROUGHPUT,
            "1 wątek, całość stop-the-world",
            "małe kontenery: < 2 GB sterty, < 2 CPU"),

    /**
     * "Throughput Collector". Ten sam algorytm co Serial, ale fazy zrównoleglone
     * na wiele wątków. Nadal w pełni stop-the-world - skraca pauzy względem
     * Seriala, ale ich nie eliminuje. Skaluje się z liczbą rdzeni.
     * <p>
     * Daje najwyższą surową przepustowość, bo nie płaci za bariery i
     * współbieżność jak kolektory low-latency.
     */
    PARALLEL("-XX:+UseParallelGC",
            Goal.THROUGHPUT,
            "wielowątkowy, całość stop-the-world",
            "batch, ETL, Spark, Kafka - gdy pauzy nie przeszkadzają"),

    /**
     * Garbage-First. Dzieli stertę na wiele małych regionów o stałym rozmiarze;
     * region może pełnić rolę Eden, Survivor, Old albo Humongous (obiekty
     * większe niż pół regionu). Po fazie znakowania szacuje, ile śmieci jest
     * w każdym regionie, i zbiera najpierw te najbardziej opłacalne - stąd
     * nazwa. Dzięki temu old gen zbierany jest przyrostowo.
     * <p>
     * Przyjmuje cel czasu pauzy (-XX:MaxGCPauseMillis), ale to cel, nie
     * gwarancja; przy przestrzeleniu sam przesuwa proporcje generacji.
     * Znakowanie old gen jest współbieżne, ale ewakuacja obiektów nadal
     * stop-the-world - dlatego to raczej kolektor przepustowościowy z
     * ograniczoną latencją niż prawdziwie low-latency. Cele poniżej 50 ms
     * są nierealistyczne.
     * <p>
     * Domyślny od JDK 9 przy >= 2 CPU i >= 2 GB sterty.
     */
    G1("-XX:+UseG1GC",
            Goal.BALANCED,
            "regiony, współbieżne znakowanie, ewakuacja stop-the-world",
            "domyślny, ogólnego przeznaczenia, duże sterty (> 32 GB)"),

    /**
     * Kolektor low-latency od Red Hata. Cel: pauzy stale poniżej 10 ms.
     * Prawie wszystko robi współbieżnie z aplikacją - łącznie z ewakuacją,
     * czyli przenoszeniem obiektów w trakcie działania programu.
     * <p>
     * Kluczowy mechanizm: READ BARRIER. Skoro obiekt może zostać przesunięty
     * w trakcie, gdy aplikacja go używa, każdy odczyt referencji przechodzi
     * przez kod sprawdzający, czy referencja jest jeszcze aktualna. Jeśli wątek
     * aplikacji natrafi na obiekt jeszcze nieprzeniesiony, sam dokonuje
     * ewakuacji - praca GC rozkłada się na wątki aplikacji zamiast wymagać
     * koordynacji. Ceną są właśnie bariery: niższa przepustowość.
     * <p>
     * Działa na 32- i 64-bitowych systemach.
     */
    SHENANDOAH("-XX:+UseShenandoahGC",
            Goal.LATENCY,
            "ewakuacja współbieżna, read barriers, pauzy < 10 ms",
            "serwery WWW, aplikacje user-facing; 32-bit też"),

    /**
     * Kolektor low-latency od Oracle'a, cele zbliżone do Shenandoaha:
     * niemal cała praca (włącznie z kompaktowaniem) współbieżnie, spójność
     * referencji pilnowana read barrierami.
     * <p>
     * Obsługuje sterty od kilku GB do terabajtów i szybko oddaje pamięć do
     * systemu operacyjnego - nawet przed końcem cyklu.
     * <p>
     * Tryb generacyjny doszedł w JDK 21 i został domyślnym trybem ZGC w JDK 23;
     * wariant niegeneracyjny usunięto w JDK 24. Tylko 64-bit.
     */
    Z("-XX:+UseZGC",
            Goal.LATENCY,
            "wszystko współbieżnie, read barriers, generacyjny od JDK 21",
            "duża skala + niska latencja, sterty do TB; tylko 64-bit"),

    /**
     * Kolektor, który nie zbiera niczego - alokuje, aż sterta się skończy,
     * i wtedy kończy JVM. Do testów wydajności alokacji i krótkich zadań,
     * nie do produkcji.
     */
    EPSILON("-XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC",
            Goal.NONE,
            "nie zwalnia pamięci w ogóle",
            "benchmarki, testy, zadania krótsze niż zapełnienie sterty");

    public enum Goal {THROUGHPUT, BALANCED, LATENCY, NONE}

    private final String flag;
    private final String goal;
    private final String how;
    private final String useCase;

    GarbageCollectors(String flag, Goal goal, String how, String useCase) {
        this.flag = flag;
        this.goal = goal.name();
        this.how = how;
        this.useCase = useCase;
    }

    public String flag() {
        return flag;
    }

    public String goal() {
        return goal;
    }

    public String how() {
        return how;
    }

    public String useCase() {
        return useCase;
    }

    /**
     * Który kolektor jest aktywny w TEJ maszynie wirtualnej.
     */
    public static String current() {
        return java.lang.management.ManagementFactory.getGarbageCollectorMXBeans().stream()
                .map(java.lang.management.GarbageCollectorMXBean::getName)
                .reduce((a, b) -> a + ", " + b)
                .orElse("brak");
    }

    public static void main(String[] args) {
        System.out.printf("%-11s %-11s %s%n", "KOLEKTOR", "CEL", "ZASTOSOWANIE");
        Arrays.stream(values()).forEach(gc ->
                System.out.printf("%-11s %-11s %s%n", gc, gc.goal(), gc.useCase()));

        System.out.println("\nJava: " + Runtime.version());
        System.out.println("Aktywny GC: " + current());
    }
}

/*
WYJŚCIE (Corretto 25):

KOLEKTOR    CEL         ZASTOSOWANIE
SERIAL      THROUGHPUT  małe kontenery: < 2 GB sterty, < 2 CPU
PARALLEL    THROUGHPUT  batch, ETL, Spark, Kafka - gdy pauzy nie przeszkadzają
G1          BALANCED    domyślny, ogólnego przeznaczenia, duże sterty (> 32 GB)
SHENANDOAH  LATENCY     serwery WWW, aplikacje user-facing; 32-bit też
Z           LATENCY     duża skala + niska latencja, sterty do TB; tylko 64-bit
EPSILON     NONE        benchmarki, testy, zadania krótsze niż zapełnienie sterty

Java: 25.0.2+10-LTS
Aktywny GC: G1 Young Generation, G1 Concurrent GC, G1 Old Generation

SKRÓT DECYZYJNY:
  przepustowość ważniejsza od pauz  -> Parallel (albo Serial przy 1 CPU)
  nie wiesz / ogólny backend        -> G1 (domyślny, zostaw)
  liczy się p99, sterta do kilku GB -> Shenandoah
  liczy się p99, sterta ogromna     -> ZGC

Kompromis jest zawsze ten sam: kolektory low-latency (Shenandoah, ZGC) płacą
barierami przy każdym odczycie referencji, więc mają niższą przepustowość niż
Parallel. Nie ma kolektora, który wygrywa na obu osiach.
*/
