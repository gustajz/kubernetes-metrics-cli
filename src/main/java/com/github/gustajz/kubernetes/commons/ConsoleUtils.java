package com.github.gustajz.kubernetes.commons;

/** @author gustavojotz */
public class ConsoleUtils {

    @SuppressWarnings("java:S106")
    public static void clean() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
