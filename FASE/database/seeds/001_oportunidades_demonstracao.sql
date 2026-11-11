-- Dados 100% ficticios para desenvolvimento local e demonstracao da API.
-- Nomes, contatos, CNPJs e links nao representam entidades ou processos reais.
-- Reexecutavel: organizacoes, categorias, localidades e oportunidades nao duplicam.

USE fase_db;
START TRANSACTION;

INSERT INTO organizacao (razao_social, cnpj, tipo, responsavel_solicitante, email_solicitante, telefone, cidade, uf, descricao, status_aprovacao, verificada, analisada_em)
SELECT dados.razao_social, dados.cnpj, dados.tipo, 'Contato demonstrativo', dados.email, '00000000000', dados.cidade, dados.uf,
       'Organizacao ficticia para demonstracao local do FASE.', 'APROVADA', TRUE, NOW()
FROM (
  SELECT 'Nuvem Criativa Tecnologia Ltda.' AS razao_social, '00000000000001' AS cnpj, 'EMPRESA' AS tipo, 'contato@nuvem-criativa.example' AS email, 'Sao Paulo' AS cidade, 'SP' AS uf
  UNION ALL SELECT 'Verde Norte Sustentabilidade Ltda.', '00000000000002', 'EMPRESA', 'contato@verde-norte.example', 'Manaus', 'AM'
  UNION ALL SELECT 'Instituto Horizonte de Aprendizagem', '00000000000003', 'ENSINO', 'contato@instituto-horizonte.example', 'Recife', 'PE'
  UNION ALL SELECT 'Coletivo Ponte Social', '00000000000004', 'ORGANIZACAO_SOCIAL', 'contato@ponte-social.example', 'Salvador', 'BA'
) dados
WHERE NOT EXISTS (SELECT 1 FROM organizacao existente WHERE existente.cnpj = dados.cnpj);

INSERT INTO categoria (tipo, nome)
SELECT dados.tipo, dados.nome FROM (
  SELECT 'VAGA' tipo, 'Jovem Aprendiz' nome UNION ALL SELECT 'VAGA', 'Atendimento' UNION ALL SELECT 'VAGA', 'Tecnologia'
  UNION ALL SELECT 'VAGA', 'Administracao' UNION ALL SELECT 'VAGA', 'Meio Ambiente'
  UNION ALL SELECT 'CURSO', 'Tecnologia' UNION ALL SELECT 'CURSO', 'Administracao' UNION ALL SELECT 'CURSO', 'Carreira'
  UNION ALL SELECT 'CURSO', 'Design' UNION ALL SELECT 'CURSO', 'Sustentabilidade'
) dados
WHERE NOT EXISTS (SELECT 1 FROM categoria existente WHERE existente.tipo = dados.tipo AND existente.nome = dados.nome);

INSERT INTO localidade (cidade, uf)
SELECT dados.cidade, dados.uf FROM (
  SELECT 'Sao Paulo' cidade, 'SP' uf UNION ALL SELECT 'Manaus', 'AM' UNION ALL SELECT 'Recife', 'PE'
  UNION ALL SELECT 'Salvador', 'BA' UNION ALL SELECT 'Curitiba', 'PR'
) dados
WHERE NOT EXISTS (SELECT 1 FROM localidade existente WHERE existente.cidade = dados.cidade AND existente.uf = dados.uf);

-- Vagas ficticias
INSERT INTO oportunidade (id_organizacao, id_categoria, id_localidade, tipo, titulo, descricao, requisitos, modalidade, tipo_vaga, remuneracao, link_externo, status, data_publicacao, data_revisao)
SELECT org.id_organizacao, cat.id_categoria, loc.id_localidade, 'VAGA', dados.titulo, dados.descricao, dados.requisitos, dados.modalidade, dados.tipo_vaga, dados.remuneracao, dados.link, 'PUBLICADA', NOW(), NOW()
FROM (
  SELECT '00000000000001' cnpj, 'Tecnologia' categoria, 'Sao Paulo' cidade, 'SP' uf, 'Pessoa Desenvolvedora Jr. (demonstracao)' titulo, 'Vaga ficticia para exercitar a busca e os filtros. Processo seletivo inexistente.' descricao, 'Conhecimentos basicos de logica e vontade de aprender.' requisitos, 'HIBRIDO' modalidade, 'EMPREGO' tipo_vaga, 3200.00 remuneracao, 'https://vagas.nuvem-criativa.example/desenvolvedor-jr' link
  UNION ALL SELECT '00000000000001', 'Atendimento', 'Sao Paulo', 'SP', 'Jovem Aprendiz - Atendimento (demonstracao)', 'Oportunidade ficticia de aprendizagem em atendimento e suporte.', 'Ensino medio cursando ou concluido. Comunicacao e organizacao.', 'PRESENCIAL', 'APRENDIZ', 1050.00, 'https://vagas.nuvem-criativa.example/aprendiz-atendimento'
  UNION ALL SELECT '00000000000002', 'Meio Ambiente', 'Manaus', 'AM', 'Assistente de Projetos Ambientais (demonstracao)', 'Vaga ficticia para apoio a projetos de sustentabilidade.', 'Interesse por meio ambiente e conhecimentos basicos de planilhas.', 'HIBRIDO', 'ESTAGIO', 1400.00, 'https://vagas.verde-norte.example/assistente-projetos'
  UNION ALL SELECT '00000000000003', 'Administracao', 'Recife', 'PE', 'Estagio em Rotinas Administrativas (demonstracao)', 'Oportunidade ficticia de estagio para praticar rotinas de escritorio.', 'Ensino medio tecnico ou superior em andamento.', 'PRESENCIAL', 'ESTAGIO', 1200.00, 'https://vagas.instituto-horizonte.example/estagio-administrativo'
  UNION ALL SELECT '00000000000004', 'Jovem Aprendiz', 'Salvador', 'BA', 'Jovem Aprendiz - Comunicacao (demonstracao)', 'Vaga ficticia de aprendizagem em comunicacao comunitaria.', 'Ensino medio cursando ou concluido. Interesse por comunicacao.', 'REMOTO', 'APRENDIZ', 900.00, 'https://vagas.ponte-social.example/aprendiz-comunicacao'
) dados
JOIN organizacao org ON org.cnpj = dados.cnpj
JOIN categoria cat ON cat.tipo = 'VAGA' AND cat.nome = dados.categoria
JOIN localidade loc ON loc.cidade = dados.cidade AND loc.uf = dados.uf
WHERE NOT EXISTS (SELECT 1 FROM oportunidade existente WHERE existente.link_externo = dados.link);

-- Cursos ficticios
INSERT INTO oportunidade (id_organizacao, id_categoria, id_localidade, tipo, titulo, descricao, requisitos, modalidade, carga_horaria, gratuito, valor_curso, data_inicio, link_externo, status, data_publicacao, data_revisao)
SELECT org.id_organizacao, cat.id_categoria, loc.id_localidade, 'CURSO', dados.titulo, dados.descricao, dados.requisitos, dados.modalidade, dados.carga_horaria, dados.gratuito, dados.valor, DATE_ADD(CURRENT_DATE, INTERVAL dados.dias DAY), dados.link, 'PUBLICADA', NOW(), NOW()
FROM (
  SELECT '00000000000001' cnpj, 'Tecnologia' categoria, NULL cidade, NULL uf, 'Programacao Web para Iniciantes (demonstracao)' titulo, 'Curso ficticio sobre fundamentos de HTML, CSS e JavaScript.' descricao, 'Nenhum pre-requisito. Conteudo somente para demonstracao.' requisitos, 'ONLINE' modalidade, 40 carga_horaria, TRUE gratuito, 0.00 valor, 7 dias, 'https://cursos.nuvem-criativa.example/programacao-web' link
  UNION ALL SELECT '00000000000003', 'Administracao', 'Recife', 'PE', 'Excel e Organizacao Financeira (demonstracao)', 'Curso ficticio de planilhas e organizacao financeira pessoal.', 'Conhecimentos basicos de informatica.', 'HIBRIDO', 24, FALSE, 39.90, 14, 'https://cursos.instituto-horizonte.example/excel-financas'
  UNION ALL SELECT '00000000000004', 'Carreira', 'Salvador', 'BA', 'Comunicacao e Preparacao para Entrevistas (demonstracao)', 'Curso ficticio com atividades de comunicacao e simulacao de entrevistas.', 'Nenhum pre-requisito.', 'PRESENCIAL', 16, TRUE, 0.00, 21, 'https://cursos.ponte-social.example/comunicacao-entrevistas'
  UNION ALL SELECT '00000000000001', 'Design', NULL, NULL, 'Design Grafico e Identidade Visual (demonstracao)', 'Curso ficticio introdutorio de composicao visual e identidade de marca.', 'Acesso a computador ou celular. Nenhum conhecimento previo exigido.', 'REMOTO', 30, FALSE, 59.90, 28, 'https://cursos.nuvem-criativa.example/design-grafico'
  UNION ALL SELECT '00000000000002', 'Sustentabilidade', 'Manaus', 'AM', 'Introducao a Sustentabilidade (demonstracao)', 'Curso ficticio sobre consumo consciente e praticas sustentaveis.', 'Nenhum pre-requisito.', 'ONLINE', 20, TRUE, 0.00, 10, 'https://cursos.verde-norte.example/sustentabilidade'
) dados
JOIN organizacao org ON org.cnpj = dados.cnpj
JOIN categoria cat ON cat.tipo = 'CURSO' AND cat.nome = dados.categoria
LEFT JOIN localidade loc ON loc.cidade = dados.cidade AND loc.uf = dados.uf
WHERE NOT EXISTS (SELECT 1 FROM oportunidade existente WHERE existente.link_externo = dados.link);

COMMIT;

-- Confira em http://127.0.0.1:8080/api/oportunidades

