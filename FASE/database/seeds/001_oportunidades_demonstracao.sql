-- Dados fictícios apenas para desenvolvimento local e conferência da API.
-- Não representam vagas, cursos, empresas ou links reais.

USE fase_db;
START TRANSACTION;

INSERT INTO organizacao (
  razao_social, cnpj, tipo, responsavel_solicitante, email_solicitante,
  telefone, cidade, uf, descricao, status_aprovacao, verificada, analisada_em
)
SELECT
  'Organização Demonstrativa FASE', '00000000000000', 'EMPRESA',
  'Contato demonstrativo', 'contato@fase-demo.example', '00000000000',
  'São Paulo', 'SP', 'Registro fictício para testar a API local.',
  'APROVADA', TRUE, NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM organizacao WHERE cnpj = '00000000000000'
);

INSERT INTO categoria (tipo, nome)
SELECT 'VAGA', 'Jovem Aprendiz'
WHERE NOT EXISTS (
  SELECT 1 FROM categoria WHERE tipo = 'VAGA' AND nome = 'Jovem Aprendiz'
);

INSERT INTO categoria (tipo, nome)
SELECT 'CURSO', 'Tecnologia'
WHERE NOT EXISTS (
  SELECT 1 FROM categoria WHERE tipo = 'CURSO' AND nome = 'Tecnologia'
);

INSERT INTO categoria (tipo, nome)
SELECT 'CURSO', 'Administração'
WHERE NOT EXISTS (
  SELECT 1 FROM categoria WHERE tipo = 'CURSO' AND nome = 'Administração'
);

INSERT INTO categoria (tipo, nome)
SELECT 'CURSO', 'Carreira'
WHERE NOT EXISTS (
  SELECT 1 FROM categoria WHERE tipo = 'CURSO' AND nome = 'Carreira'
);

INSERT INTO categoria (tipo, nome)
SELECT 'CURSO', 'Design'
WHERE NOT EXISTS (
  SELECT 1 FROM categoria WHERE tipo = 'CURSO' AND nome = 'Design'
);

INSERT INTO localidade (cidade, uf)
SELECT 'São Paulo', 'SP'
WHERE NOT EXISTS (
  SELECT 1 FROM localidade WHERE cidade = 'São Paulo' AND uf = 'SP'
);

INSERT INTO localidade (cidade, uf)
SELECT 'Manaus', 'AM'
WHERE NOT EXISTS (
  SELECT 1 FROM localidade WHERE cidade = 'Manaus' AND uf = 'AM'
);

SET @id_organizacao_demo = (
  SELECT id_organizacao FROM organizacao WHERE cnpj = '00000000000000' LIMIT 1
);
SET @id_categoria_vaga = (
  SELECT id_categoria FROM categoria WHERE tipo = 'VAGA' AND nome = 'Jovem Aprendiz' LIMIT 1
);
SET @id_categoria_curso = (
  SELECT id_categoria FROM categoria WHERE tipo = 'CURSO' AND nome = 'Tecnologia' LIMIT 1
);
SET @id_categoria_administracao = (
  SELECT id_categoria FROM categoria WHERE tipo = 'CURSO' AND nome = 'Administração' LIMIT 1
);
SET @id_categoria_carreira = (
  SELECT id_categoria FROM categoria WHERE tipo = 'CURSO' AND nome = 'Carreira' LIMIT 1
);
SET @id_categoria_design = (
  SELECT id_categoria FROM categoria WHERE tipo = 'CURSO' AND nome = 'Design' LIMIT 1
);
SET @id_localidade_demo = (
  SELECT id_localidade FROM localidade WHERE cidade = 'São Paulo' AND uf = 'SP' LIMIT 1
);
SET @id_localidade_manaus = (
  SELECT id_localidade FROM localidade WHERE cidade = 'Manaus' AND uf = 'AM' LIMIT 1
);

INSERT INTO oportunidade (
  id_organizacao, id_categoria, id_localidade, tipo, titulo, descricao,
  requisitos, modalidade, tipo_vaga, remuneracao, link_externo, status,
  data_publicacao, data_revisao
)
SELECT
  @id_organizacao_demo, @id_categoria_vaga, @id_localidade_demo, 'VAGA',
  'Vaga demonstrativa — Jovem Aprendiz em Administração',
  'Registro fictício para conferir a listagem de vagas da API FASE.',
  'Exemplo de requisito: estar cursando ou ter concluído o ensino médio.',
  'HIBRIDO', 'APRENDIZ', 950.00,
  'https://example.org/fase-demo-aprendiz', 'PUBLICADA', NOW(), NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM oportunidade
  WHERE id_organizacao = @id_organizacao_demo
    AND link_externo = 'https://example.org/fase-demo-aprendiz'
);

INSERT INTO oportunidade (
  id_organizacao, id_categoria, id_localidade, tipo, titulo, descricao,
  requisitos, modalidade, carga_horaria, gratuito, valor_curso, data_inicio,
  link_externo, status, data_publicacao, data_revisao
)
SELECT
  @id_organizacao_demo, @id_categoria_curso, NULL, 'CURSO',
  'Curso demonstrativo de Introdução à Programação',
  'Registro fictício para conferir a listagem de cursos da API FASE.',
  'Nenhum requisito prévio. Este conteúdo é apenas um dado de demonstração.',
  'ONLINE', 40, TRUE, 0.00, DATE_ADD(CURRENT_DATE, INTERVAL 7 DAY),
  'https://example.org/fase-demo-curso', 'PUBLICADA', NOW(), NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM oportunidade
  WHERE id_organizacao = @id_organizacao_demo
    AND link_externo = 'https://example.org/fase-demo-curso'
);

INSERT INTO oportunidade (
  id_organizacao, id_categoria, id_localidade, tipo, titulo, descricao,
  requisitos, modalidade, carga_horaria, gratuito, valor_curso, data_inicio,
  link_externo, status, data_publicacao, data_revisao
)
SELECT
  @id_organizacao_demo, @id_categoria_administracao, @id_localidade_manaus, 'CURSO',
  'Curso demonstrativo de Excel Básico',
  'Registro fictício presencial para experimentar filtros de área e cidade.',
  'Conhecimentos básicos de informática.', 'HIBRIDO', 30, FALSE, 49.90,
  DATE_ADD(CURRENT_DATE, INTERVAL 14 DAY),
  'https://example.org/fase-demo-excel', 'PUBLICADA', NOW(), NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM oportunidade
  WHERE id_organizacao = @id_organizacao_demo
    AND link_externo = 'https://example.org/fase-demo-excel'
);

INSERT INTO oportunidade (
  id_organizacao, id_categoria, id_localidade, tipo, titulo, descricao,
  requisitos, modalidade, carga_horaria, gratuito, valor_curso, data_inicio,
  link_externo, status, data_publicacao, data_revisao
)
SELECT
  @id_organizacao_demo, @id_categoria_carreira, @id_localidade_demo, 'CURSO',
  'Curso demonstrativo de Comunicação Profissional',
  'Registro fictício gratuito para experimentar filtros de carreira e modalidade.',
  'Nenhum requisito prévio. Este conteúdo é apenas um dado de demonstração.',
  'PRESENCIAL', 20, TRUE, 0.00, DATE_ADD(CURRENT_DATE, INTERVAL 21 DAY),
  'https://example.org/fase-demo-comunicacao', 'PUBLICADA', NOW(), NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM oportunidade
  WHERE id_organizacao = @id_organizacao_demo
    AND link_externo = 'https://example.org/fase-demo-comunicacao'
);

INSERT INTO oportunidade (
  id_organizacao, id_categoria, id_localidade, tipo, titulo, descricao,
  requisitos, modalidade, carga_horaria, gratuito, valor_curso, data_inicio,
  link_externo, status, data_publicacao, data_revisao
)
SELECT
  @id_organizacao_demo, @id_categoria_design, NULL, 'CURSO',
  'Curso demonstrativo de Design Gráfico',
  'Registro fictício remoto para experimentar a categoria Design.',
  'Nenhum requisito prévio. Este conteúdo é apenas um dado de demonstração.',
  'REMOTO', 24, FALSE, 29.90, DATE_ADD(CURRENT_DATE, INTERVAL 28 DAY),
  'https://example.org/fase-demo-design', 'PUBLICADA', NOW(), NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM oportunidade
  WHERE id_organizacao = @id_organizacao_demo
    AND link_externo = 'https://example.org/fase-demo-design'
);

COMMIT;

-- Verifique o resultado da API em http://127.0.0.1:8080/api/oportunidades
