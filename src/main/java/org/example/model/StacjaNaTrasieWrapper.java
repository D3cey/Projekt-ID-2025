package org.example.model;

import javafx.beans.property.SimpleBooleanProperty;

public class StacjaNaTrasieWrapper {
    private Stacja stacja;
    private int trasaId;
    private SimpleBooleanProperty zatrzymujeSie;

    public StacjaNaTrasieWrapper(Stacja stacja, int trasaId, boolean zatrzymujeSie) {
        this.stacja = stacja;
        this.trasaId = trasaId;
        this.zatrzymujeSie = new SimpleBooleanProperty(zatrzymujeSie);
    }

    public Stacja getStacja() {
        return stacja;
    }

    public int getTrasaId() {
        return trasaId;
    }

    // Zwraca właściwość JavaFX, do której można bindować CheckBox
    public SimpleBooleanProperty zatrzymujeSieProperty() {
        return zatrzymujeSie;
    }

    // Standardowy getter
    public boolean czySieZatrzymuje() {
        return zatrzymujeSie.get();
    }

    // Standardowy setter
    public void setZatrzymujeSie(boolean value) {
        this.zatrzymujeSie.set(value);
    }
}