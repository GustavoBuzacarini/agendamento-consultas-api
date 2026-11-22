CREATE TABLE paciente (
    id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome                   VARCHAR(150) NOT NULL,
    cpf                    VARCHAR(11)  NOT NULL UNIQUE,
    data_nascimento        DATE         NOT NULL,
    gestante               BOOLEAN      NOT NULL DEFAULT FALSE,
    pessoa_com_deficiencia BOOLEAN      NOT NULL DEFAULT FALSE
);
