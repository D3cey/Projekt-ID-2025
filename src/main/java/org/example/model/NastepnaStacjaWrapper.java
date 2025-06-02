package org.example.model; // lub org.example.dto

import org.example.model.Stacja;
import java.util.Objects;

public class NastepnaStacjaWrapper {
    private Stacja stacja;
    private int polaczenieId;         // ID obiektu Polaczenie (czyli polaczenia_miedzy_stacjami.id)
    private double odlegloscOdcinka; // Odległość tego konkretnego odcinka

    public NastepnaStacjaWrapper(Stacja stacja, int polaczenieId, double odlegloscOdcinka) {
        this.stacja = stacja;
        this.polaczenieId = polaczenieId;
        this.odlegloscOdcinka = odlegloscOdcinka;
    }

    public Stacja getStacja() { return stacja; }
    public int getPolaczenieId() { return polaczenieId; }
    public double getOdlegloscOdcinka() { return odlegloscOdcinka; }

    @Override
    public String toString() {
        // Format: "Nazwa Stacji (Połączenie ID: X, Dystans: Y.YY km)"
        return String.format("%s (Połączenie ID: %d, Dystans: %.2f km)",
                stacja.getNazwa(), polaczenieId, odlegloscOdcinka);
    }

    // Opcjonalnie: equals i hashCode, jeśli ComboBox miałby problemy z zarządzaniem tymi obiektami
    // (np. przy ustawianiu wartości programowo lub porównywaniu).
    // Porównujemy na podstawie ID stacji i ID połączenia, które ją tu prowadzi.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NastepnaStacjaWrapper that = (NastepnaStacjaWrapper) o;
        return stacja.getId() == that.stacja.getId() && polaczenieId == that.polaczenieId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stacja.getId(), polaczenieId);
    }
}