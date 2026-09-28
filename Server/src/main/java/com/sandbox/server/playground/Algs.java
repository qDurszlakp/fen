package com.sandbox.server.playground;

import java.util.*;

public class Algs {

    public static void main(String[] args) {
        System.out.println(sockMerchant(List.of(1, 2, 1, 2, 1, 3, 2)));
    }




    public static int sockMerchant(List<Integer> ar) {

        int pairs = 0;

        // O(n)
        Set<Integer> helper = new HashSet<>();

        // O(n)
        for (Integer sock : ar) {
            if (!helper.add(sock)) {
                helper.remove(sock);
                pairs++;
            }
        }

        return pairs;
    }

    public static int lonelyInteger(List<Integer> a) {

        int result = 0;

        for (Integer integer : a) {
            result ^= integer;
        }

        return result;
    }

    public static int diagonalDifference(List<List<Integer>> arr) {

        if (arr.isEmpty() || arr.size() != arr.get(0).size()) {
            return 0;
        }

        int diag1 = 0;
        int diag2 = 0;

        //O(n)
        for (int i = 0 ; i < arr.size() ; i++) {
            diag1 += arr.get(i).get(i);
            diag2 += arr.get(i).get(arr.size() - i -1);
        }

        return Math.abs(diag1 - diag2);
    }

    public static void miniMaxSum(List<Integer> arr) {

        // O(n)
        long sum = arr.stream().mapToLong(Integer::longValue).sum();

        // O(1)
        long max = sum - arr.getFirst();
        long min = sum - arr.getFirst();

        long tmpMax, tmpMin;

        // O(n-1) -> O(n)
        for (int i = 1 ; i< arr.size() ; i++) {
            tmpMax = sum - arr.get(i);
            tmpMin = sum - arr.get(i);

            if (tmpMax > max) {
                max = tmpMax;
            }

            if (tmpMin < min) {
                min = tmpMin;
            }
        }

        System.out.println(min + " " + max);

        // compute complex O(n)
        // storage complex O(1)
    }

    class P {
        Character ch1;
        Character ch2;

        public P(Character ch1, Character ch2) {
            this.ch1 = ch1;
            this.ch2 = ch2;
        }
    }

    public record Pair(Character ch1, Character ch2) {
        public static Pair of(Character ch1, Character ch2) {
            return new Pair(ch1, ch2);
        }
    }

    public static int alternate(String s) {

        if (s.isEmpty()) {
            return 0;
        }

        Set<Character> unique = new HashSet<>();
        List<Pair> pairs = new ArrayList<>();

        // O(n)
        for (int i = 0; i < s.length(); i++) {
            unique.add(s.charAt(i));
        }

        List<Character> chars = new ArrayList<>(unique);

        // O(u*u)
        for (int i = 0; i < chars.size(); i++) {
            for (int j = i + 1; j < chars.size(); j++) {
                pairs.add(Pair.of(chars.get(i), chars.get(j)));
            }
        }

        int max = 0;

        // Sprawdzamy każdą wygenerowaną parę
        for (Pair pair : pairs) {
            char c1 = pair.ch1();
            char c2 = pair.ch2();

            StringBuilder filtered = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                char current = s.charAt(i);
                if (current == c1 || current == c2) {
                    filtered.append(current);
                }
            }

            if (isAlternating(filtered.toString())) {
                max = Math.max(max, filtered.length());
            }
        }

        return max;
    }

    // Pomocnicza metoda sprawdzająca naprzemienność
    private static boolean isAlternating(String s) {
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) {
                return false;
            }
        }
        return true;
    }

    public static String superReducedString(String s) {

        StringBuilder sb = new StringBuilder(s);

        // O(n-1) -> O(n-3) -> ....
        for (int i = 1; i < sb.length(); i++) {
            if (sb.charAt(i) == sb.charAt(i - 1)) {
                sb.delete(i - 1, i + 1);

                return superReducedString(sb.toString());
            }
        }

        // obliczenia O(n*n)
        // space O(n)

        return sb.isEmpty() ? "Empty String" : sb.toString();
    }

    public static String superReducedString2(String s) {

        Deque<Character> result = new ArrayDeque<>();

        // O(n) obliczeniowa
        // O(n) pamięciowa
        for (int i = 0; i < s.length(); i++) {

            Character latestChar = result.peek();

            if (latestChar != null && latestChar == s.charAt(i)) {
                result.pop();
            } else {
                result.push(s.charAt(i));
            }

        }

        if (result.isEmpty()) {
            return "Empty String";
        }

        StringBuilder sb = new StringBuilder();

        Iterator<Character> iterator = result.descendingIterator();

        while (iterator.hasNext()) {
            sb.append(iterator.next());
        }

        return sb.toString();
    }

    private static List<Integer> strings(String str1, String str2) {

        List<Integer> result = new ArrayList<>();

        int n = str1.length();
        int m = str2.length();

        if (n - m != 1) {
            return List.of(-1);
        }

        int l = 0;
        for (int i = 0; i < m; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                break;
            }
            l++;
        }

        int r = 0;
        for (int i = 0; i < m; i++) {
            if (str1.charAt(n - 1 - i) != str2.charAt(m - 1 - i)) {
                break;
            }
            r++;
        }

        if (l + r < m) {
            return List.of(-1);
        }

        int start = n - 1 - r;
        int end = l;

        for (int i = start; i <= end; i++) {
            result.add(i);
        }

        return result;
    }

    private static void f(int limit) {
        // 0, 1, 1, 2, 3, 5, 8, 13, ...

        int a = 0;
        int b = 1;

        int tmp;

        while (a < limit) {
            tmp = a + b;
            System.out.println(a);
            a = b;
            b = tmp;
        }

    }

}
