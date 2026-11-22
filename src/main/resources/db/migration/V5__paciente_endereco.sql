-- Endereço opcional do paciente, preenchido pela consulta de CEP (ViaCEP).
ALTER TABLE paciente
    ADD COLUMN cep        VARCHAR(8),
    ADD COLUMN logradouro VARCHAR(200),
    ADD COLUMN bairro     VARCHAR(100),
    ADD COLUMN cidade     VARCHAR(100),
    ADD COLUMN uf         VARCHAR(2);
