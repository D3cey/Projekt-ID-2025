-- pavlo.sql
CREATE TABLE stacje
(
    id    SERIAL PRIMARY KEY,
    nazwa VARCHAR(100) NOT NULL
);

CREATE TABLE tory
(
    stacja_id    INTEGER REFERENCES stacje (id),
    numer        INTEGER CHECK (numer > 0),
    numer_peronu INTEGER NOT NULL CHECK (numer_peronu > 0),
    PRIMARY KEY (stacja_id, numer)
);

CREATE TABLE trasa
(
    id                   SERIAL PRIMARY KEY,
    poczatkowa_stacja_id INTEGER REFERENCES stacje (id)
);

CREATE TABLE polaczenia_miedzy_stacjami
(
    id         SERIAL PRIMARY KEY,
    stacja1_id INTEGER        NOT NULL REFERENCES stacje (id),
    stacja2_id INTEGER        NOT NULL REFERENCES stacje (id),
    odleglosc  NUMERIC(10, 2) NOT NULL
);

CREATE TABLE stacje_na_trasie
(
    trasa_id                      INTEGER REFERENCES trasa (id),
    stacja1_id                    INTEGER REFERENCES stacje (id),
    tor1                          INTEGER NOT NULL,
    stacja2_id                    INTEGER REFERENCES stacje (id),
    tor2                          INTEGER,
    polaczenia_miedzy_stacjami_id INTEGER NOT NULL REFERENCES polaczenia_miedzy_stacjami (id),
    FOREIGN KEY (stacja1_id, tor1)
        REFERENCES tory (stacja_id, numer),
    FOREIGN KEY (stacja2_id, tor2)
        REFERENCES tory (stacja_id, numer),
    PRIMARY KEY (trasa_id, stacja1_id)
);

ALTER TABLE trasa
    ADD FOREIGN KEY (id, poczatkowa_stacja_id)
        REFERENCES stacje_na_trasie (trasa_id, stacja1_id);

-- blazej.sql
CREATE TABLE lokomotywy
(
    id   SERIAL PRIMARY KEY,
    stan VARCHAR(50) NOT NULL DEFAULT 'sprawna'
);

CREATE TABLE typy_pociagow
(
    id       SERIAL PRIMARY KEY,
    typ      VARCHAR(100) UNIQUE NOT NULL,
    predkosc INTEGER CHECK (predkosc > 0)
);


CREATE TABLE pociagi
(
    id             SERIAL PRIMARY KEY,
    lokomotywa_id  INTEGER NOT NULL REFERENCES lokomotywy (id),
    typ_pociagu_id INTEGER NOT NULL REFERENCES typy_pociagow (id)
);

CREATE TABLE typy_wagonu
(
    id           SERIAL PRIMARY KEY,
    nazwa_typu   VARCHAR(100) UNIQUE NOT NULL,
    ilosc_miejsc INTEGER             NOT NULL CHECK (ilosc_miejsc >= 0),
    klasa        VARCHAR(10)
);

CREATE TABLE wagony
(
    id            SERIAL PRIMARY KEY,
    typ_wagonu_id INTEGER     NOT NULL REFERENCES typy_wagonu (id),
    stan          VARCHAR(50) NOT NULL DEFAULT 'sprawny'
);

CREATE TABLE miejsce
(
    id                   SERIAL PRIMARY KEY,
    wagon_id             INTEGER NOT NULL REFERENCES wagony (id),
    nr_miejsca_w_wagonie INTEGER NOT NULL CHECK (nr_miejsca_w_wagonie > 0),
    UNIQUE (wagon_id, nr_miejsca_w_wagonie)
);

CREATE TABLE sklad_pociagu
(
    id                SERIAL PRIMARY KEY,
    pociag_id         INTEGER NOT NULL REFERENCES pociagi (id),
    wagon_id          INTEGER NOT NULL REFERENCES wagony (id),
    miejsce_w_pociagu INTEGER NOT NULL CHECK (miejsce_w_pociagu > 0),
    UNIQUE (pociag_id, wagon_id),
    UNIQUE (pociag_id, miejsce_w_pociagu)
);

-- kuba.sql
CREATE TABLE klient
(
    id       SERIAL PRIMARY KEY,
    pesel    CHAR(11) UNIQUE NOT NULL,
    imie     VARCHAR(50)     NOT NULL,
    nazwisko VARCHAR(50)     NOT NULL
);

CREATE TABLE znizka
(
    id       SERIAL PRIMARY KEY,
    wysokosc NUMERIC(4, 2) NOT NULL CHECK (wysokosc BETWEEN 0 AND 1)
);

CREATE TABLE bilet
(
    id                   SERIAL PRIMARY KEY,
    klient_id            INT            NOT NULL REFERENCES klient (id),
    cena                 NUMERIC(10, 2) NOT NULL,
    znizka_id            INT REFERENCES znizka (id),
    data_odjazdu         TIMESTAMP      NOT NULL,
    data_przyjazdu       TIMESTAMP      NOT NULL CHECK (data_przyjazdu >= data_odjazdu),
    poczatkowa_stacja_id INT            NOT NULL REFERENCES stacje (id),
    koncowa_stacja_id    INT            NOT NULL REFERENCES stacje (id)
);

CREATE TABLE odcinek
(
    id                            SERIAL PRIMARY KEY,
    pociag_id                     INT       NOT NULL REFERENCES pociagi (id),
    polaczenia_miedzy_stacjami_id INT       NOT NULL REFERENCES polaczenia_miedzy_stacjami (id),
    data_od                       TIMESTAMP NOT NULL,
    data_do                       TIMESTAMP NOT NULL CHECK (data_od <= data_do)
);

CREATE TABLE wykupione_miejsca
(
    miejsce_id INT NOT NULL REFERENCES miejsce (id),
    odcinek_id INT NOT NULL REFERENCES odcinek (id),
    bilet_id   INT NOT NULL REFERENCES bilet (id),
    PRIMARY KEY (miejsce_id, odcinek_id)
);

CREATE TABLE rozklad_jazdy
(
    trasa_id     INT       NOT NULL REFERENCES trasa (id),
    pociag_id    INT       NOT NULL REFERENCES pociagi (id),
    czas_odjazdu TIMESTAMP NOT NULL
);
