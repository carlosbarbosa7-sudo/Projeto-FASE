-- FASE: schema inicial do banco de dados
-- Requer MySQL 8.0.16 ou superior (suporte a CHECK constraints).
-- O FASE divulga oportunidades e encaminha para canais externos.
-- Este modelo não registra candidaturas nem inscrições em cursos.

CREATE DATABASE IF NOT EXISTS fase_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE fase_db;

CREATE TABLE IF NOT EXISTS usuario (
  id_usuario BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nome VARCHAR(160) NOT NULL,
  email VARCHAR(254) NOT NULL,
  senha_hash VARCHAR(255) NOT NULL COMMENT 'Armazenar apenas hash forte, nunca senha em texto puro.',
  perfil ENUM('JOVEM', 'INSTITUICAO', 'ADMIN') NOT NULL,
  ativo BOOLEAN NOT NULL DEFAULT TRUE,
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uq_usuario_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS perfil_jovem (
  id_usuario BIGINT UNSIGNED NOT NULL,
  data_nascimento DATE NOT NULL COMMENT 'A aplicação deve validar idade mínima de 16 anos. Não limitar a idade máxima a 24.',
  telefone VARCHAR(30) NULL,
  escolaridade VARCHAR(120) NULL,
  cidade VARCHAR(120) NULL,
  bairro VARCHAR(120) NULL,
  uf CHAR(2) NULL,
  PRIMARY KEY (id_usuario),
  CONSTRAINT fk_perfil_jovem_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS organizacao (
  id_organizacao BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_usuario BIGINT UNSIGNED NULL COMMENT 'Conta vinculada após aprovação administrativa.',
  razao_social VARCHAR(200) NOT NULL,
  cnpj CHAR(14) NOT NULL COMMENT 'Armazenar 12 caracteres alfanuméricos e 2 dígitos verificadores, sem pontuação e em maiúsculas.',
  tipo ENUM('EMPRESA', 'ENSINO', 'ORGAO_PUBLICO', 'ORGANIZACAO_SOCIAL', 'OUTRA') NOT NULL,
  responsavel_solicitante VARCHAR(160) NOT NULL,
  email_solicitante VARCHAR(254) NOT NULL,
  telefone VARCHAR(30) NOT NULL,
  site VARCHAR(2048) NULL,
  cidade VARCHAR(120) NOT NULL,
  uf CHAR(2) NOT NULL,
  descricao VARCHAR(1000) NULL,
  status_aprovacao ENUM('PENDENTE', 'APROVADA', 'RECUSADA') NOT NULL DEFAULT 'PENDENTE',
  verificada BOOLEAN NOT NULL DEFAULT FALSE,
  analisada_por BIGINT UNSIGNED NULL,
  analisada_em DATETIME NULL,
  motivo_decisao VARCHAR(500) NULL,
  criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_organizacao),
  UNIQUE KEY uq_organizacao_usuario (id_usuario),
  UNIQUE KEY uq_organizacao_cnpj (cnpj),
  KEY ix_organizacao_status (status_aprovacao),
  CONSTRAINT fk_organizacao_usuario
    FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_organizacao_analisada_por
    FOREIGN KEY (analisada_por) REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS formacao (
  id_formacao BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_usuario BIGINT UNSIGNED NOT NULL,
  instituicao VARCHAR(200) NOT NULL,
  curso VARCHAR(160) NOT NULL,
  data_inicio DATE NULL,
  data_fim DATE NULL,
  PRIMARY KEY (id_formacao),
  KEY ix_formacao_usuario (id_usuario),
  CONSTRAINT fk_formacao_perfil_jovem
    FOREIGN KEY (id_usuario) REFERENCES perfil_jovem (id_usuario)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT chk_formacao_datas
    CHECK (data_fim IS NULL OR data_inicio IS NULL OR data_fim >= data_inicio)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS area_interesse (
  id_area BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nome VARCHAR(120) NOT NULL,
  PRIMARY KEY (id_area),
  UNIQUE KEY uq_area_interesse_nome (nome)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS perfil_area (
  id_usuario BIGINT UNSIGNED NOT NULL,
  id_area BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (id_usuario, id_area),
  KEY ix_perfil_area_area (id_area),
  CONSTRAINT fk_perfil_area_jovem
    FOREIGN KEY (id_usuario) REFERENCES perfil_jovem (id_usuario)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_perfil_area_area
    FOREIGN KEY (id_area) REFERENCES area_interesse (id_area)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categoria (
  id_categoria BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  tipo ENUM('VAGA', 'CURSO') NOT NULL,
  nome VARCHAR(120) NOT NULL,
  PRIMARY KEY (id_categoria),
  UNIQUE KEY uq_categoria_tipo_nome (tipo, nome)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS localidade (
  id_localidade BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  cidade VARCHAR(120) NOT NULL,
  uf CHAR(2) NOT NULL,
  PRIMARY KEY (id_localidade),
  UNIQUE KEY uq_localidade_cidade_uf (cidade, uf)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS oportunidade (
  id_oportunidade BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organizacao BIGINT UNSIGNED NOT NULL,
  id_categoria BIGINT UNSIGNED NOT NULL,
  id_localidade BIGINT UNSIGNED NULL COMMENT 'NULL quando a oportunidade for totalmente remota/online.',
  tipo ENUM('VAGA', 'CURSO') NOT NULL,
  titulo VARCHAR(200) NOT NULL,
  descricao TEXT NOT NULL,
  requisitos TEXT NULL,
  modalidade ENUM('PRESENCIAL', 'HIBRIDO', 'REMOTO', 'ONLINE') NOT NULL,
  tipo_vaga ENUM('EMPREGO', 'ESTAGIO', 'APRENDIZ') NULL,
  remuneracao DECIMAL(10,2) NULL,
  carga_horaria SMALLINT UNSIGNED NULL,
  gratuito BOOLEAN NULL,
  valor_curso DECIMAL(10,2) NULL,
  data_inicio DATE NULL,
  data_fim DATE NULL,
  data_publicacao DATETIME NULL,
  data_encerramento DATETIME NULL,
  link_externo VARCHAR(2048) NOT NULL COMMENT 'URL oficial para candidatura, inscrição ou informações da instituição.',
  status ENUM('RASCUNHO', 'PENDENTE_REVISAO', 'PUBLICADA', 'ENCERRADA', 'RECUSADA') NOT NULL DEFAULT 'PENDENTE_REVISAO',
  revisado_por BIGINT UNSIGNED NULL,
  data_revisao DATETIME NULL,
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_oportunidade),
  KEY ix_oportunidade_busca (status, tipo, id_categoria, id_localidade),
  KEY ix_oportunidade_organizacao (id_organizacao),
  KEY ix_oportunidade_publicacao (data_publicacao),
  CONSTRAINT fk_oportunidade_organizacao
    FOREIGN KEY (id_organizacao) REFERENCES organizacao (id_organizacao)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_oportunidade_categoria
    FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_oportunidade_localidade
    FOREIGN KEY (id_localidade) REFERENCES localidade (id_localidade)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_oportunidade_revisor
    FOREIGN KEY (revisado_por) REFERENCES usuario (id_usuario)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT chk_oportunidade_datas
    CHECK (data_encerramento IS NULL OR data_publicacao IS NULL OR data_encerramento >= data_publicacao),
  CONSTRAINT chk_oportunidade_vaga_tipo
    CHECK (tipo <> 'VAGA' OR tipo_vaga IS NOT NULL),
  CONSTRAINT chk_oportunidade_curso_carga
    CHECK (tipo <> 'CURSO' OR carga_horaria IS NOT NULL),
  CONSTRAINT chk_oportunidade_valores_nao_negativos
    CHECK ((remuneracao IS NULL OR remuneracao >= 0) AND (valor_curso IS NULL OR valor_curso >= 0))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS oportunidade_salva (
  id_usuario BIGINT UNSIGNED NOT NULL,
  id_oportunidade BIGINT UNSIGNED NOT NULL,
  data_salvamento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id_usuario, id_oportunidade),
  KEY ix_oportunidade_salva_oportunidade (id_oportunidade),
  CONSTRAINT fk_oportunidade_salva_jovem
    FOREIGN KEY (id_usuario) REFERENCES perfil_jovem (id_usuario)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_oportunidade_salva_oportunidade
    FOREIGN KEY (id_oportunidade) REFERENCES oportunidade (id_oportunidade)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS servico_apoio (
  id_servico BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_organizacao BIGINT UNSIGNED NULL,
  id_localidade BIGINT UNSIGNED NULL,
  nome_instituicao VARCHAR(200) NOT NULL,
  titulo VARCHAR(200) NOT NULL,
  descricao TEXT NOT NULL,
  contato VARCHAR(254) NULL,
  link_externo VARCHAR(2048) NOT NULL,
  status ENUM('RASCUNHO', 'PUBLICADO', 'INATIVO') NOT NULL DEFAULT 'RASCUNHO',
  criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id_servico),
  KEY ix_servico_status_localidade (status, id_localidade),
  CONSTRAINT fk_servico_organizacao
    FOREIGN KEY (id_organizacao) REFERENCES organizacao (id_organizacao)
    ON UPDATE CASCADE ON DELETE SET NULL,
  CONSTRAINT fk_servico_localidade
    FOREIGN KEY (id_localidade) REFERENCES localidade (id_localidade)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;
