package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Polaczenie {
    private final int id;
    private final int stacja1Id;
    private final int stacja2Id;

    public Polaczenie(int id, int stacja1Id, int stacja2Id) {
        this.id = id;
        this.stacja1Id = stacja1Id;
        this.stacja2Id = stacja2Id;
    }

    public int getId() {
        return id;
    }

    public int getStacja1Id() {
        return stacja1Id;
    }

    public int getStacja2Id() {
        return stacja2Id;
    }

    public static List<Polaczenie> pobierzWszystkie() {
        List<Polaczenie> lista = new ArrayList<>();
        // Warszawa (1) – Kraków (2)
        lista.add(new Polaczenie(1, 1, 2));
        // Warszawa (1) – Gdańsk (3)
        lista.add(new Polaczenie(2, 1, 3));
        // Wrocław (4) – Kraków (2)
        lista.add(new Polaczenie(3, 4, 2));
        return lista;
    }

    public static Polaczenie znajdzBezposrednie(int a, int b) {
        for (Polaczenie p : pobierzWszystkie()) {
            if ((p.stacja1Id == a && p.stacja2Id == b) ||
                    (p.stacja1Id == b && p.stacja2Id == a)) {
                return p;
            }
        }
        return null;
    }
}
