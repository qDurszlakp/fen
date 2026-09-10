package com.sandbox.server.playground;

import javax.swing.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Tmp {

    // =========================================================================
    // MODELE DANYCH
    // =========================================================================
    public record Product(String id, String name, String category, double price) {}

    public record OrderItem(Product product, int quantity) {
        public double getSubtotal() {
            return product.price() * quantity;
        }
    }

    public enum OrderStatus {
        NEW, PAID, SHIPPED, CANCELLED
    }

    public record Order(
            String id,
            String customer,
            List<OrderItem> items,
            LocalDate date,
            OrderStatus status
    ) {
        public double getTotalAmount() {
            return items.stream()
                    .mapToDouble(OrderItem::getSubtotal)
                    .sum();
        }
    }

    // =========================================================================
    // METODA GŁÓWNA - ZESTAW DANYCH I ZADANIA
    // =========================================================================
    public static void main(String[] args) {

        // --- Zestaw danych testowych: Produkty ---
        List<Product> products = List.of(
                new Product("P1", "Laptop Pro", "Elektronika", 4500.0),
                new Product("P2", "Smartfon X", "Elektronika", 2800.0),
                new Product("P3", "Słuchawki BT", "Elektronika", 350.0),
                new Product("P4", "Mysz bezprzewodowa", "Elektronika", 120.0),
                new Product("P5", "Klawiatura mechaniczna", "Elektronika", 350.0),
                new Product("P6", "Czysty Kod", "Książki", 65.0),
                new Product("P7", "Java. Podręcznik", "Książki", 120.0),
                new Product("P8", "Wzorce projektowe", "Książki", 89.0),
                new Product("P9", "Koszulka bawełniana", "Odzież", 49.0),
                new Product("P10", "Bluza z kapturem", "Odzież", 180.0),
                new Product("P11", "Kawa ziarnista 1kg", "Spożywcze", 85.0),
                new Product("P12", "Herbata zielona", "Spożywcze", 25.0)
        );

        // --- Zestaw danych testowych: Zamówienia ---
        List<Order> orders = List.of(
                new Order("O1", "Jan Kowalski", List.of(
                        new OrderItem(products.get(0), 1), // Laptop 4500
                        new OrderItem(products.get(2), 2)  // 2x Słuchawki 700
                ), LocalDate.of(2026, 1, 15), OrderStatus.SHIPPED), // suma: 5200

                new Order("O2", "Anna Nowak", List.of(
                        new OrderItem(products.get(5), 1), // Czysty kod 65
                        new OrderItem(products.get(6), 1), // Java 120
                        new OrderItem(products.get(10), 2) // 2x Kawa 170
                ), LocalDate.of(2026, 1, 20), OrderStatus.PAID), // suma: 355

                new Order("O3", "Piotr Wiśniewski", List.of(
                        new OrderItem(products.get(1), 1), // Smartfon 2800
                        new OrderItem(products.get(4), 1)  // Klawiatura 350
                ), LocalDate.of(2026, 2, 2), OrderStatus.CANCELLED), // suma: 3150

                new Order("O4", "Jan Kowalski", List.of(
                        new OrderItem(products.get(3), 1), // Mysz 120
                        new OrderItem(products.get(4), 1)  // Klawiatura 350
                ), LocalDate.of(2026, 2, 10), OrderStatus.PAID), // suma: 470

                new Order("O5", "Katarzyna Wójcik", List.of(
                        new OrderItem(products.get(8), 3), // 3x Koszulka 147
                        new OrderItem(products.get(9), 1)  // Bluza 180
                ), LocalDate.of(2026, 2, 14), OrderStatus.NEW), // suma: 327

                new Order("O6", "Anna Nowak", List.of(
                        new OrderItem(products.get(1), 1) // Smartfon 2800
                ), LocalDate.of(2026, 3, 1), OrderStatus.SHIPPED), // suma: 2800

                new Order("O7", "Michał Lewandowski", List.of(
                        new OrderItem(products.get(10), 5), // 5x Kawa 425
                        new OrderItem(products.get(11), 4)  // 4x Herbata 100
                ), LocalDate.of(2026, 3, 5), OrderStatus.PAID) // suma: 525
        );

        // --- Dodatkowy zestaw danych: Teksty i liczby ---
        List<String> textSentences = List.of(
                "Java Stream API jest potężnym narzędziem",
                "Programowanie funkcyjne w Java ułatwia przetwarzanie danych",
                "Kolekcje i strumienie w języku Java"
        );

        List<Integer> numbers = List.of(12, 5, 8, 19, 21, 8, 4, 15, 30, 2, 19, 7);

        /*
         * Zadanie 1:
         * Znajdź nazwy wszystkich produktów z kategorii "Elektronika", których cena wynosi co najmniej 350 zł.
         * Wynik zapisz do List<String>.
         *
         * Podpowiedź: filter, map, toList
         */

        List<String> list = products.stream()
                .filter(product -> "Elektronika".equalsIgnoreCase(product.category))
                .filter(product -> product.price >= 350.0)
                .map(Product::name)
                .toList();

        System.out.println(list);

        /*
         * Zadanie 2:
         * Posortuj produkty malejąco po cenie.
         * Jeśli dwa produkty mają tę samą cenę, posortuj je alfabetycznie po nazwie.
         *
         * Podpowiedź: sorted, Comparator.comparing(...).reversed().thenComparing(...)
         */

        List<String> list1 = products.stream()
                .sorted(Comparator.comparing(Product::price).reversed().thenComparing(Product::name))
                .map(Product::id)
                .toList();

        System.out.println(list1);

        /*
         * Zadanie 3:
         * a) Sprawdź, czy w systemie istnieje JAKIEKOLWIEK zamówienie o statusie CANCELLED o wartości > 2000 zł.
         * b) Sprawdź, czy WSZYSTKIE produkty kosztują więcej niż 10 zł.
         * c) Sprawdź, czy ŻADEN produkt nie kosztuje powyżej 10 000 zł.
         *
         * Podpowiedź: anyMatch, allMatch, noneMatch
         */

        List<Order> list2 = orders.stream()
                .filter(order -> OrderStatus.CANCELLED.equals(order.status))
                .filter(order -> order.getTotalAmount() > 2000.0)
                .toList();

        boolean b = orders.stream()
                .allMatch(order -> order.getTotalAmount() > 10.0);

        boolean a = orders.stream()
                .noneMatch(order -> order.getTotalAmount() > 10000.0);


        /*
         * Zadanie 4:
         * Znajdź PIERWSZY produkt z kategorii "Książki" droższy niż 80 zł.
         * Jeśli taki produkt istnieje, zwróć jego nazwę, a jeśli nie - tekst "Brak produktu".
         *
         * Podpowiedź: filter, findFirst, map, orElse
         */

        String name = products.stream()
                .filter(product -> "Książki".equalsIgnoreCase(product.category))
                .filter((product -> product.price > 80.0))
                .sorted(Comparator.comparing(Product::price))
                .map(Product::name)
                .findFirst()
                .orElse("Brak");

        System.out.println(name);



        /*
         * Zadanie 5:
         * Pobierz 3 najtańsze unikalne ceny produktów.
         *
         * Podpowiedź: map, distinct, sorted, limit
         */

        List<Double> list3 = products.stream()
                .distinct()
                .sorted(Comparator.comparing(Product::price))
                .limit(3)
                .map(Product::price)
                .toList();

        System.out.println(list3);

        // =====================================================================
        // CZĘŚĆ 2: SPŁASZCZANIE STRUKTUR I STRUMIENIE LICZBOWE (flatMap, IntStream)
        // =====================================================================

        /*
         * Zadanie 6:
         * Wyciągnij listę wszystkich UNIKALNYCH produktów (obiektów Product), które zostały
         * zakupione w zamówieniach o statusie PAID lub SHIPPED.
         *
         * Podpowiedź: filter (status), flatMap (zamówienie -> items), map (item -> product), distinct
         */

        List<Product> list4 = orders.stream()
                .filter(order -> List.of(OrderStatus.PAID, OrderStatus.SHIPPED).contains(order.status))
                .flatMap(order -> order.items().stream())
                .map(OrderItem::product)
                .distinct()
                .toList();


        /*
         * Zadanie 7:
         * Mając listę zdań `textSentences`:
         * Wyciągnij wszystkie UNIKALNE słowa, zamień je na małe litery, odrzuć słowa krótsze
         * niż 4 znaki i posortuj alfabetycznie.
         *
         * Podpowiedź: flatMap z Arrays.stream(sentence.split("\\s+")), map(String::toLowerCase), filter, distinct, sorted
         */

        




        /*
         * Zadanie 8:
         * Korzystając ze strumieni prymitywnych (DoubleStream / mapToDouble):
         * a) Oblicz łączną wartość wszystkich zamówień o statusie SHIPPED.
         * b) Oblicz średnią cenę produktu w kategorii "Elektronika".
         * c) Wyznacz pełne statystyki (DoubleSummaryStatistics) dla cen wszystkich produktów w sklepie.
         *
         * Podpowiedź: mapToDouble, sum, average, summaryStatistics
         */
        // double shippedTotal = ...
        // OptionalDouble avgElectronicsPrice = ...
        // DoubleSummaryStatistics stats = ...
        // System.out.println("Zadanie 8a: " + shippedTotal);
        // System.out.println("Zadanie 8b: " + avgElectronicsPrice.orElse(0.0));
        // System.out.println("Zadanie 8c: " + stats);


        /*
         * Zadanie 9:
         * Użyj `IntStream.rangeClosed(1, 100)`:
         * a) Oblicz sumę kwadratów wszystkich liczb parzystych z zakresu 1..100.
         * b) Stwórz listę Stringów w formacie: "Liczba: 2", "Liczba: 4" itd. dla liczb podzielnych przez 7.
         *
         * Podpowiedź: filter, map, mapToObj
         */
        // long sumOfEvenSquares = ...
        // List<String> divisibleBy7 = ...
        // System.out.println("Zadanie 9a: " + sumOfEvenSquares);
        // System.out.println("Zadanie 9b: " + divisibleBy7);



        // =====================================================================
        // CZĘŚĆ 3: GRUPOWANIE I KOLEKTORY (Collectors.groupingBy, partitioningBy)
        // =====================================================================

        /*
         * Zadanie 10:
         * Pogrupuj produkty według kategorii.
         * Oczekiwany wynik: Map<String, List<Product>>
         *
         * Podpowiedź: Collectors.groupingBy(Product::category)
         */
        // Map<String, List<Product>> task10 = ...
        // System.out.println("Zadanie 10: " + task10);


        /*
         * Zadanie 11:
         * Zlicz ile produktów znajduje się w każdej kategorii.
         * Oczekiwany wynik: Map<String, Long>
         *
         * Podpowiedź: Collectors.groupingBy(..., Collectors.counting())
         */
        // Map<String, Long> task11 = ...
        // System.out.println("Zadanie 11: " + task11);


        /*
         * Zadanie 12:
         * Stwórz mapę, w której kluczem jest kategoria, a wartością lista TYLKO nazw produktów (List<String>)
         * w tej kategorii.
         * Oczekiwany wynik: Map<String, List<String>>
         *
         * Podpowiedź: Collectors.groupingBy(..., Collectors.mapping(Product::name, Collectors.toList()))
         */
        // Map<String, List<String>> task12 = ...
        // System.out.println("Zadanie 12: " + task12);


        /*
         * Zadanie 13:
         * Dla każdej kategorii znajdź najdroższy produkt.
         * Oczekiwany wynik: Map<String, Optional<Product>> lub Map<String, Product>
         *
         * Podpowiedź: Collectors.groupingBy z Collectors.maxBy(...) lub Collectors.collectingAndThen
         */
        // Map<String, Optional<Product>> task13 = ...
        // System.out.println("Zadanie 13: " + task13);


        /*
         * Zadanie 14:
         * Oblicz łączną wartość wszystkich produktów w magazynie dla każdej kategorii
         * (suma cen produktów w danej kategorii).
         * Oczekiwany wynik: Map<String, Double>
         *
         * Podpowiedź: Collectors.groupingBy(..., Collectors.summingDouble(Product::price))
         */
        // Map<String, Double> task14 = ...
        // System.out.println("Zadanie 14: " + task14);


        /*
         * Zadanie 15:
         * Podziel zamówienia na dwie grupy za pomocą `partitioningBy`:
         * - true: zamówienia zrealizowane/opłacone (status SHIPPED lub PAID)
         * - false: pozostałe zamówienia (NEW, CANCELLED)
         * Oczekiwany wynik: Map<Boolean, List<Order>>
         *
         * Podpowiedź: Collectors.partitioningBy(order -> ...)
         */
        // Map<Boolean, List<Order>> task15 = ...
        // System.out.println("Zadanie 15: " + task15);


        /*
         * Zadanie 16:
         * Wielopoziomowe grupowanie (Nested groupingBy):
         * Pogrupuj zamówienia najpierw po statusie (OrderStatus),
         * a wewnątrz każdego statusu pogrupuj je po nazwisku klienta.
         * Oczekiwany wynik: Map<OrderStatus, Map<String, List<Order>>>
         *
         * Podpowiedź: Collectors.groupingBy(Order::status, Collectors.groupingBy(Order::customer))
         */
        // Map<OrderStatus, Map<String, List<Order>>> task16 = ...
        // System.out.println("Zadanie 16: " + task16);



        // =====================================================================
        // CZĘŚĆ 4: ZAAWANSOWANE KOLEKTORY, REDUKCJA I ZADANIA BIZNESOWE
        // =====================================================================

        /*
         * Zadanie 17:
         * Konwersja do Mapy za pomocą `Collectors.toMap`:
         * a) Stwórz mapę: id produktu -> nazwa produktu (Map<String, String>).
         * b) Stwórz mapę: kategoria -> najtańszy produkt w danej kategorii, radząc sobie z kolizją
         *    kluczy przy pomocy merge function (BinaryOperator).
         *
         * Podpowiedź: Collectors.toMap(keyMapper, valueMapper, mergeFunction)
         */
        // Map<String, String> idToNameMap = ...
        // Map<String, Product> cheapestByCategory = ...
        // System.out.println("Zadanie 17a: " + idToNameMap);
        // System.out.println("Zadanie 17b: " + cheapestByCategory);


        /*
         * Zadanie 18:
         * Sformatuj unikalne nazwiska klientów w jeden czytelny łańcuch tekstowy:
         * "Zarejestrowani klienci: [Anna Nowak, Jan Kowalski, Katarzyna Wójcik, Michał Lewandowski, Piotr Wiśniewski]"
         * Nazwiska posortuj alfabetycznie.
         *
         * Podpowiedź: map(Order::customer), distinct, sorted, Collectors.joining(", ", "Zarejestrowani klienci: [", "]")
         */
        // String formattedCustomers = ...
        // System.out.println("Zadanie 18: " + formattedCustomers);


        /*
         * Zadanie 19:
         * Użyj metody `reduce`:
         * a) Oblicz sumę wszystkich liczb z listy `numbers` używając 2-argumentowej metody reduce(identity, accumulator).
         * b) Znajdź najdłuższe słowo ze wszystkich zdań w `textSentences` używając 1-argumentowego reduce(accumulator).
         *
         * Podpowiedź: reduce(0, Integer::sum), reduce((s1, s2) -> ...)
         */
        // int sumOfNumbers = ...
        // Optional<String> longestWord = ...
        // System.out.println("Zadanie 19a: " + sumOfNumbers);
        // System.out.println("Zadanie 19b: " + longestWord.orElse(""));


        /*
         * Zadanie 20 (Java 12+):
         * Użyj kolektora `Collectors.teeing`, aby w jednym przebiegu strumienia obliczyć
         * różnicę między najwyższą a najniższą ceną produktu na liście `products`.
         *
         * Podpowiedź: Collectors.teeing(
         *     Collectors.maxBy(Comparator.comparingDouble(Product::price)),
         *     Collectors.minBy(Comparator.comparingDouble(Product::price)),
         *     (maxOpt, minOpt) -> ...
         * )
         */
        // double priceDifference = ...
        // System.out.println("Zadanie 20: " + priceDifference);


        /*
         * Zadanie 21 (Java 9+):
         * Mając posortowaną rosnąco listę produktów (według ceny):
         * a) Użyj `takeWhile`, aby pobrać wszystkie produkty o cenie poniżej 100 zł (zatrzymaj się na pierwszym >= 100).
         * b) Użyj `dropWhile`, aby pominąć wszystkie produkty o cenie poniżej 100 zł i pobrać resztę.
         *
         * Podpowiedź: products.stream().sorted(Comparator.comparingDouble(Product::price)).takeWhile(...)
         */
        // List<Product> cheapProducts = ...
        // List<Product> remainingProducts = ...
        // System.out.println("Zadanie 21a (takeWhile): " + cheapProducts);
        // System.out.println("Zadanie 21b (dropWhile): " + remainingProducts);


        /*
         * Zadanie 22 (Zadanie przekrojowe / E2E):
         * Stwórz zestawienie łącznej kwoty wydanej przez poszczególnych klientów na zrealizowane lub opłacone
         * zamówienia (status PAID lub SHIPPED).
         * Wynik powinien być mapą lub listą posortowaną malejąco po wydanej kwocie:
         * Klient -> Łączna suma (np. Jan Kowalski wydał 5200 + 470 = 5670.0).
         *
         * Podpowiedź: filter po statusie, Collectors.groupingBy(Order::customer, Collectors.summingDouble(Order::getTotalAmount))
         * Następnie posortuj wpisy mapy po wartości malejąco.
         */
        // Map<String, Double> customerSpendings = ...
        // System.out.println("Zadanie 22: " + customerSpendings);
    }
}