package at.hagenberg.master.montecarlo.entities;

import java.util.Objects;

public abstract class Opponent {

    protected String name;

    public Opponent(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Opponent op = (Opponent) o;
        return Objects.equals(name, op.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }


}
