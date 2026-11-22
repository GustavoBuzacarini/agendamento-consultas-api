CREATE TABLE motivo_atendimento (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    descricao VARCHAR(200) NOT NULL UNIQUE,
    gravidade VARCHAR(10)  NOT NULL CHECK (gravidade IN ('VERDE', 'AMARELO', 'VERMELHO'))
);

-- Exemplos APENAS para demonstração. NÃO são classificação clínica: a lista real deve ser
-- definida e revisada por profissional de saúde (spec Q-001).
INSERT INTO motivo_atendimento (descricao, gravidade) VALUES
    ('[EXEMPLO] Consulta de rotina', 'VERDE'),
    ('[EXEMPLO] Sintoma que persiste há dias', 'AMARELO'),
    ('[EXEMPLO] Situação de risco imediato', 'VERMELHO');
