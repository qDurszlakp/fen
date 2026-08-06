package com.sandbox.server.playground;

import lombok.Getter;

import java.util.List;
import java.util.Objects;

public record Room(String name, int number, List<F> spots) {

    public Room(String name, int number, List<F> spots) {
        this.name = name;
        this.number = number;
        this.spots = spots.stream()
                .map(e -> new F(e.getString()))
                .toList();
    }

    @Override
    public List<F> spots() {
        return spots.stream()
                .map(e -> new F(e.getString()))
                .toList();
    }

    @Getter
    class F {
        private final String string;

        public F(String string) {
            this.string = string;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof F f)) return false;

            return Objects.equals(string, f.string);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(string);
        }
    }

}
