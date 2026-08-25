package com.sandbox.server.playground;

public class LowQualityFromIndia {

    public static void main(String[] args) {
        //System.out.println(f("+654"));
        //fibonaci((25));
        System.out.println(r("Assasin"));
    }

    static int f(String in) {

        boolean negative = in.charAt(0) == '-';
        int start = in.charAt(0) == '-' || in.charAt(0) == '+' ? 1 : 0;

        int result = 0;

        for (int i = start ; i < in.length() ; i++) {

            char ch = in.charAt(i);

            if (ch < '0' || ch > '9') {
                throw new NumberFormatException("Invalid character: " + ch);
            }

            result = result * 10 + (ch - '0');

        }

        return negative ? -result : result;
    }

    static void fibonaci(int max) {

        // 0, 1, 1, 2, 3, 5, 8, ...

        int a = 0;
        int b = 1;
        int tmp;

        while (a < max) {
            System.out.println(a);
            tmp = a + b;
            a = b;
            b = tmp;
        }
    }

    static String r(String in) {

        StringBuilder result = new StringBuilder();

        for (int i = 1; i <= in.length(); i++) {
            result.append(in.charAt(in.length() - i));
        }

        return result.toString();
    }
}
