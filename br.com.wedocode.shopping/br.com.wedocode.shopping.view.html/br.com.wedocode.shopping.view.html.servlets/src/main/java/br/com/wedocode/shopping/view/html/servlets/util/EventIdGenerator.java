package br.com.wedocode.shopping.view.html.servlets.util;

import java.util.concurrent.atomic.AtomicInteger;

public class EventIdGenerator {

    private static AtomicInteger count = new AtomicInteger();

    public static int next() {
        return EventIdGenerator.count.incrementAndGet();
    }

    public static String nextAsString() {
        return String.valueOf(next());
    }

}
