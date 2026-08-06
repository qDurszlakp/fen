package com.sandbox.server.playground;

import java.util.Objects;

public class House {

    private String name;
    private int windows;
    private int number;

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof House house)) return false;

        return windows == house.windows && number == house.number && Objects.equals(name, house.name);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + windows;
        result = 31 * result + number;
        return result;
    }

}
