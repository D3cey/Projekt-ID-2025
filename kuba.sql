CREATE TABLE klient (
    id_klienta      SERIAL          PRIMARY KEY,
    pesel           CHAR(11)        UNIQUE NOT NULL,
    imie            VARCHAR(50)     NOT NULL,
    nazwisko        VARCHAR(50)     NOT NULL
);

CREATE TABLE znizka (
    id_znizki       SERIAL          PRIMARY KEY,
    wysokosc        NUMERIC(4,2)    NOT NULL CHECK (wysokosc BETWEEN 0 AND 1)
);

CREATE TABLE bilet (
    id_biletu           SERIAL          PRIMARY KEY,
    id_klienta          INT             NOT NULL REFERENCES klient(id_klienta),
    cena                NUMERIC(10,2)   NOT NULL,
    typ_znizki          INT             REFERENCES znizka(id_znizki),
    data_odjazdu        TIMESTAMP       NOT NULL,
    data_przyjazdu      TIMESTAMP       NOT NULL CHECK(data_przyjazdu<=data_odjazdu),
    poczatkowa_stacja   INT             NOT NULL REFERENCES stacje(id_stacji),
    koncowa_stacja      INT             NOT NULL REFERENCES stacje(id_stacji)
);

CREATE TABLE odcinek (
    id_odcinek              SERIAL  PRIMARY KEY,
    id_pociagu              INT     NOT NULL REFERENCES pociagi(id_pociagu),
    id_pol_miedzy_stacjami  INT     NOT NULL REFERENCES polaczenia_miedzy_stacjami(id_pol_miedzy_stacjami),
    data_od                 TIMESTAMP   NOT NULL,
    data_do                 TIMESTAMP   NOT NULL CHECK(data_od<=data_do)
);

CREATE TABLE wykupione_miejsca (
    id_miejsca      INT     NOT NULL REFERENCES miejsce(id_miejsca),
    id_odcinek      INT     NOT NULL REFERENCES odcinek(id_odcinek),
    id_biletu       INT     NOT NULL REFERENCES bilet(id_biletu),
    PRIMARY KEY (id_miejsca, id_odcinek)
);

CREATE TABLE rozklad_jazdy (
    id_trasy     INT         NOT NULL REFERENCES trasa(id_trasy),
    id_pociagu   INT         NOT NULL REFERENCES pociagi(id_pociagu),
    "data"         TIMESTAMP   NOT NULL,
    
);
