package com.spaziocodice.examples.ldfs;

import java.util.Optional;

import static java.util.function.Predicate.not;

public abstract class Utility {
    public static String localIdentifierFrom(String uri) {
        return Optional.ofNullable(uri)
                .filter(not(String::isEmpty))
                .map(v -> v.contains("/") ? v.substring(uri.lastIndexOf("/") + 1) : v)
                .orElseThrow(IllegalArgumentException::new);
    }

    public static String withoutMailScheme(String uri) {
        return uri.replace("mailTo:", "");
    }
}
