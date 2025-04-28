-- pavlo.sql
CREATE TABLE stacje
(
    id_stacji SERIAL PRIMARY KEY,
    nazwa     VARCHAR(100) NOT NULL
);

CREATE TABLE tory
(
    id_stacji    INTEGER REFERENCES stacje (id_stacji),
    numer        INTEGER CHECK (numer > 0),
    numer_peronu INTEGER NOT NULL CHECK (numer_peronu > 0),
    PRIMARY KEY (id_stacji, numer)
);

CREATE TABLE trasa
(
    id_trasy          SERIAL PRIMARY KEY,
    poczatkowa_stacja INTEGER REFERENCES stacje (id_stacji)
);

CREATE TABLE polaczenia_miedzy_stacjami
(
    id_pol_miedzy_stacjami SERIAL PRIMARY KEY,
    stacja1                INTEGER        NOT NULL REFERENCES stacje (id_stacji),
    stacja2                INTEGER        NOT NULL REFERENCES stacje (id_stacji),
    odleglosc              NUMERIC(10, 2) NOT NULL
);

CREATE TABLE stacje_na_trasie
(
    id_trasy               INTEGER REFERENCES trasa (id_trasy),
    stacja1                INTEGER REFERENCES stacje (id_stacji),
    tor1                   INTEGER NOT NULL,
    stacja2                INTEGER REFERENCES stacje (id_stacji),
    tor2                   INTEGER,
    id_pol_miedzy_stacjami INTEGER NOT NULL REFERENCES polaczenia_miedzy_stacjami (id_pol_miedzy_stacjami),
    FOREIGN KEY (stacja1, tor1)
        REFERENCES tory (id_stacji, numer),
    FOREIGN KEY (stacja2, tor2)
        REFERENCES tory (id_stacji, numer),
    PRIMARY KEY (id_trasy, stacja1)
);

ALTER TABLE trasa
    ADD FOREIGN KEY (id_trasy, poczatkowa_stacja)
        REFERENCES stacje_na_trasie (id_trasy, stacja1);

-- blazej.sql
CREATE TABLE lokomotywy
(
    id_lokomotywy SERIAL PRIMARY KEY,
    stan          VARCHAR(50) NOT NULL DEFAULT 'sprawna'
);

CREATE TABLE typy_pociagow
(
    id_typu_pociagu SERIAL PRIMARY KEY,
    typ             VARCHAR(100) UNIQUE NOT NULL,
    predkosc        INTEGER CHECK (predkosc > 0)
);


CREATE TABLE pociagi
(
    id_pociagu      SERIAL PRIMARY KEY,
    id_lokomotywy   INTEGER NOT NULL REFERENCES lokomotywy (id_lokomotywy),
    id_typu_pociagu INTEGER NOT NULL REFERENCES typy_pociagow (id_typu_pociagu)
);

CREATE TABLE typy_wagonu
(
    id_typ_wagonu SERIAL PRIMARY KEY,
    nazwa_typu    VARCHAR(100) UNIQUE NOT NULL,
    ilosc_miejsc  INTEGER             NOT NULL CHECK (ilosc_miejsc >= 0),
    klasa         VARCHAR(10)
);

CREATE TABLE wagony
(
    id_wagonu     SERIAL PRIMARY KEY,
    id_typ_wagonu INTEGER     NOT NULL REFERENCES typy_wagonu (id_typ_wagonu),
    stan          VARCHAR(50) NOT NULL DEFAULT 'sprawny'
);

CREATE TABLE miejsce
(
    id_miejsca           SERIAL PRIMARY KEY,
    id_wagonu            INTEGER NOT NULL REFERENCES wagony (id_wagonu),
    nr_miejsca_w_wagonie INTEGER NOT NULL CHECK (nr_miejsca_w_wagonie > 0),
    UNIQUE (id_wagonu, nr_miejsca_w_wagonie)
);

CREATE TABLE sklad_pociagu
(
    id_skladu         SERIAL PRIMARY KEY,
    id_pociagu        INTEGER NOT NULL REFERENCES pociagi (id_pociagu),
    id_wagonu         INTEGER NOT NULL REFERENCES wagony (id_wagonu),
    miejsce_w_pociagu INTEGER NOT NULL CHECK (miejsce_w_pociagu > 0),
    UNIQUE (id_pociagu, id_wagonu),
    UNIQUE (id_pociagu, miejsce_w_pociagu)
);


-- kuba.sql
CREATE TABLE klient
(
    id_klienta SERIAL PRIMARY KEY,
    pesel      CHAR(11) UNIQUE NOT NULL,
    imie       VARCHAR(50)     NOT NULL,
    nazwisko   VARCHAR(50)     NOT NULL
);

CREATE TABLE znizka
(
    id_znizki SERIAL PRIMARY KEY,
    wysokosc  NUMERIC(4, 2) NOT NULL CHECK (wysokosc BETWEEN 0 AND 1)
);

CREATE TABLE bilet
(
    id_biletu         SERIAL PRIMARY KEY,
    id_klienta        INT            NOT NULL REFERENCES klient (id_klienta),
    cena              NUMERIC(10, 2) NOT NULL,
    typ_znizki        INT REFERENCES znizka (id_znizki),
    data_odjazdu      TIMESTAMP      NOT NULL,
    data_przyjazdu    TIMESTAMP      NOT NULL CHECK (data_przyjazdu >= data_odjazdu),
    poczatkowa_stacja INT            NOT NULL REFERENCES stacje (id_stacji),
    koncowa_stacja    INT            NOT NULL REFERENCES stacje (id_stacji)
);

CREATE TABLE odcinek
(
    id_odcinek             SERIAL PRIMARY KEY,
    id_pociagu             INT       NOT NULL REFERENCES pociagi (id_pociagu),
    id_pol_miedzy_stacjami INT       NOT NULL REFERENCES polaczenia_miedzy_stacjami (id_pol_miedzy_stacjami),
    data_od                TIMESTAMP NOT NULL,
    data_do                TIMESTAMP NOT NULL CHECK (data_od <= data_do)
);

CREATE TABLE wykupione_miejsca
(
    id_miejsca INT NOT NULL REFERENCES miejsce (id_miejsca),
    id_odcinek INT NOT NULL REFERENCES odcinek (id_odcinek),
    id_biletu  INT NOT NULL REFERENCES bilet (id_biletu),
    PRIMARY KEY (id_miejsca, id_odcinek)
);

CREATE TABLE rozklad_jazdy
(
    id_trasy   INT       NOT NULL REFERENCES trasa (id_trasy),
    id_pociagu INT       NOT NULL REFERENCES pociagi (id_pociagu),
    "data"     TIMESTAMP NOT NULL
    -- PRIMARY KEY (id_trasy, id_pociagu, "data")
);

