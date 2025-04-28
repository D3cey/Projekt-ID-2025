CREATE TABlE stacje(
id SERIAL PRIMARY KEY,
nazwa VARCHAR(100) NOT NULL
);
CREATE TABLE tory(
id_stacji INTEGER REFERENCES stacje(id),
numer INTEGER CHECK(numer > 0),
numer_peronu INTEGER NOT NULL CHECK(numer_peronu > 0),
PRIMARY KEY(id_stacji, numer)
);
CREATE TABLE trasa(
id SERIAL PRIMARY KEY,
poczatkowa_stacja INTEGER REFERENCES stacje(id)
);
CREATE TABLE polaczenia_miedzy_stacjami(
id SERIAL PRIMARY KEY,
stacja1 INTEGER NOT NULL REFERENCES stacje(id),
stacja2 INTEGER NOT NULL REFERENCES stacje(id),
odleglosc NUMERIC(10, 2) NOT NULL
);
CREATE TABLE stacje_na_trasie(
id_trasy INTEGER REFERENCES trasa(id),
stacja1 INTEGER REFERENCES stacje(id),
tor1 INTEGER NOT NULL,
stacja2 INTEGER REFERENCES stacje(id),
tor2 INTEGER,
id_pol_miedzy_stacjami INTEGER NOT NULL REFERENCES polaczenia_miedzy_stacjami(id),
FOREIGN KEY(stacja1, tor1)
REFERENCES tory(id_stacji, numer),
FOREIGN KEY(stacja2, tor2)
REFERENCES tory(id_stacji, numer),
PRIMARY KEY(id_trasy, stacja1)
);
ALTER TABLE trasa
ADD FOREIGN KEY(id, poczatkowa_stacja)
REFERENCES stacje_na_trasie(id_trasy, stacja1);
