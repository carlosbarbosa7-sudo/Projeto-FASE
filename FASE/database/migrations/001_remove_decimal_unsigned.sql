-- Executar uma única vez em bancos fase_db já criados pelo schema anterior.
-- Remove UNSIGNED de DECIMAL (descontinuado no MySQL) e preserva a regra
-- de que remuneração e valor de curso não podem ser negativos.

USE fase_db;

ALTER TABLE oportunidade
  MODIFY remuneracao DECIMAL(10,2) NULL,
  MODIFY valor_curso DECIMAL(10,2) NULL,
  ADD CONSTRAINT chk_oportunidade_valores_nao_negativos
    CHECK (
      (remuneracao IS NULL OR remuneracao >= 0)
      AND (valor_curso IS NULL OR valor_curso >= 0)
    );
