-- Garante no banco que o mesmo profissional não tenha duas consultas ativas sobrepostas,
-- mesmo com dois pedidos simultâneos (a validação em Java é a primeira barreira, esta é a última).
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE consulta (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    paciente_id     BIGINT      NOT NULL REFERENCES paciente (id),
    profissional_id BIGINT      NOT NULL REFERENCES profissional (id),
    motivo_id       BIGINT      NOT NULL REFERENCES motivo_atendimento (id),
    inicio          TIMESTAMP   NOT NULL,
    fim             TIMESTAMP   NOT NULL,
    status          VARCHAR(12) NOT NULL CHECK (status IN ('AGENDADA', 'CONFIRMADA', 'REALIZADA', 'CANCELADA', 'FALTOU')),
    prioridade      VARCHAR(12) NOT NULL CHECK (prioridade IN ('NORMAL', 'PRIORITARIA', 'URGENTE')),
    criada_em       TIMESTAMP   NOT NULL,
    CHECK (fim > inicio),
    CONSTRAINT consulta_sem_sobreposicao
        EXCLUDE USING gist (profissional_id WITH =, tsrange(inicio, fim) WITH &&)
        WHERE (status IN ('AGENDADA', 'CONFIRMADA'))
);

CREATE INDEX idx_consulta_paciente ON consulta (paciente_id, inicio);
CREATE INDEX idx_consulta_profissional ON consulta (profissional_id, inicio);
