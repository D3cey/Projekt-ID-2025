package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class PolaczeniePociagowe {
    private final int id;
    private final int stacjaPoczatkowaId;
    private final int stacjaKoncowaId;
    private final List<Polaczenie> odcinki = new ArrayList<>();

    public PolaczeniePociagowe(int id, int a, int b, List<Polaczenie> odcinki) {
        this.id = id;
        this.stacjaPoczatkowaId = a;
        this.stacjaKoncowaId = b;
        this.odcinki.addAll(odcinki);
    }

    public int getStacjaPoczatkowaId() { return stacjaPoczatkowaId; }
    public int getStacjaKoncowaId()    { return stacjaKoncowaId; }
    public List<Polaczenie> getOdcinki() { return odcinki; }

    public static List<PolaczeniePociagowe> pobierzWszystkie() {
        List<Polaczenie> w = Polaczenie.pobierzWszystkie();
        List<PolaczeniePociagowe> l = new ArrayList<>();


        l.add(new PolaczeniePociagowe(
                1, 4, 3,
                List.of(
                        w.get(2),
                        new Polaczenie(4, 2, 1),
                        w.get(1)
                )));

        return l;
    }


    public static PolaczeniePociagowe znajdzTrase(int a, int b) {
        return pobierzWszystkie().stream()
                .filter(t -> (t.stacjaPoczatkowaId == a && t.stacjaKoncowaId == b) ||
                        (t.stacjaPoczatkowaId == b && t.stacjaKoncowaId == a))
                .findFirst().orElse(null);
    }
}
