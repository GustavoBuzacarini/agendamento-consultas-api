-- Contas de acesso (fase 2). A senha é guardada só como hash BCrypt.
CREATE TABLE usuario (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario     VARCHAR(100) NOT NULL UNIQUE,
    senha_hash  VARCHAR(100) NOT NULL,
    perfil      VARCHAR(10)  NOT NULL CHECK (perfil IN ('RECEPCAO', 'PACIENTE')),
    paciente_id BIGINT UNIQUE REFERENCES paciente (id),
    criado_em   TIMESTAMP    NOT NULL,
    -- paciente precisa estar ligado a um cadastro de paciente; recepção não
    CHECK ((perfil = 'PACIENTE' AND paciente_id IS NOT NULL) OR (perfil = 'RECEPCAO' AND paciente_id IS NULL))
);
