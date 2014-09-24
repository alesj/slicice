package org.openblend.slicice.common;

import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class Table implements Comparable<Table> {
    private String username;
    private Set<Integer> own = new TreeSet<>();
    private Set<Integer> miss = new TreeSet<>();

    private int canGet;
    private int canOffer;

    private int x = -1;

    protected PrintStream out = System.out;

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public void calculate(Table other) {
        calculate(other, false);
    }

    public void print(Table other) {
        calculate(other, true);
    }

    private void calculate(Table other, boolean print) {
        if (print) {
            out.println("Username: " + getUsername());
        }

        Set<Integer> t_miss = new TreeSet<>(getOwn());
        t_miss.retainAll(other.getMiss());
        if (print) {
            out.println(String.format("I can get [%s]: %s", t_miss.size(), t_miss));
        } else {
            setCanGet(t_miss.size());
        }

        Set<Integer> t_own = new TreeSet<>(other.getOwn());
        t_own.retainAll(getMiss());
        if (print) {
            out.println(String.format("I can offer [%s]: %s", t_own.size(), t_own));
        } else {
            setCanOffer(t_own.size());
        }

        if (print) {
            out.println("--------------");
        }
    }

    public boolean match() {
        return (canOffer > 0 && canGet > 0);
    }

    public void reset() {
        x = -1;
    }

    public void doSwitch() {
        if (x == canOffer) {
            setX(canGet);
        } else {
            setX(canOffer);
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Integer> getOwn() {
        return own;
    }

    public Set<Integer> getMiss() {
        return miss;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setCanGet(int canGet) {
        this.canGet = canGet;
    }

    public void setCanOffer(int canOffer) {
        this.canOffer = canOffer;
    }

    @Override
    public int compareTo(Table t) {
        return t.x - x;
    }

    @Override
    public String toString() {
        return String.format("Username: %s | own: %s | miss: %s", username, own, miss);
    }
}
