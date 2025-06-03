package org.example.model;

public class NastepnaStacjaWrapper {
    private Stacja stacja;
    private int polaczenieId;
    private double odlegloscOdcinka;

    public NastepnaStacjaWrapper(Stacja stacja, int polaczenieId, double odlegloscOdcinka) {
        this.stacja = stacja;
        this.polaczenieId = polaczenieId;
        this.odlegloscOdcinka = odlegloscOdcinka;
    }

    public Stacja getStacja() {
        return stacja;
    }

    public int getPolaczenieId() {
        return polaczenieId;
    }

    public double getOdlegloscOdcinka() {
        return odlegloscOdcinka;
    }

    @Override
    public String toString() {

        return String.format("%s (Połączenie ID: %d, Dystans: %.2f km)",
                stacja.getNazwa(), polaczenieId, odlegloscOdcinka);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NastepnaStacjaWrapper that = (NastepnaStacjaWrapper) o;
        return stacja.getId() == that.stacja.getId() && polaczenieId == that.polaczenieId;
    }
}