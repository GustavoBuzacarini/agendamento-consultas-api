CREATE TABLE profissional (
    id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome                 VARCHAR(150) NOT NULL,
    especialidade        VARCHAR(100) NOT NULL,
    duracao_consulta_min INTEGER      NOT NULL CHECK (duracao_consulta_min BETWEEN 5 AND 240)
);

CREATE TABLE expediente (
    profissional_id BIGINT      NOT NULL REFERENCES profissional (id) ON DELETE CASCADE,
    dia_semana      VARCHAR(10) NOT NULL,
    hora_inicio     TIME        NOT NULL,
    hora_fim        TIME        NOT NULL,
    CHECK (hora_fim > hora_inicio)
);

CREATE INDEX idx_expediente_profissional ON expediente (profissional_id);
