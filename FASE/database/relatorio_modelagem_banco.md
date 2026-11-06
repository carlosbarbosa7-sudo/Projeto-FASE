# Relatório de modelagem do banco de dados do FASE

## 1. Objetivo

Este documento registra a proposta inicial de banco de dados para o FASE — Força de Aprendizagem, Suporte e Empregabilidade. O modelo foi elaborado para apoiar o cadastro de usuários, a divulgação e consulta de vagas e cursos, o salvamento de oportunidades e a apresentação de serviços de apoio.

O script SQL correspondente está em `FASE/database/schema.sql` e cria o banco `fase_db` para MySQL 8.0.16 ou superior.

## 2. Escopo funcional considerado

O FASE organiza oportunidades e encaminha o usuário ao canal oficial da empresa ou instituição. A inscrição em cursos e a candidatura a vagas acontecem externamente. Por esse motivo, o modelo contém um endereço externo para cada oportunidade e não inclui tabelas de candidatura ou inscrição interna.

Empresas e instituições podem solicitar cadastro. A solicitação começa pendente e precisa ser aprovada antes de a organização publicar oportunidades. O público prioritário é de 16 a 24 anos; pessoas com mais de 24 anos também podem participar. O banco não define idade máxima.

## 3. Entidades e responsabilidades

| Entidade | Responsabilidade |
|---|---|
| `usuario` | Armazena dados de acesso, e-mail único, perfil de acesso e estado da conta. |
| `perfil_jovem` | Guarda informações de perfil do jovem, como nascimento, escolaridade e localização. |
| `formacao` | Registra uma ou mais formações associadas ao perfil do jovem. |
| `area_interesse` | Mantém o catálogo de áreas de interesse. |
| `perfil_area` | Relaciona jovens às áreas de interesse selecionadas. |
| `organizacao` | Guarda os dados de empresas e instituições, a solicitação de acesso e o estado de aprovação. |
| `categoria` | Classifica oportunidades por tipo e nome. |
| `localidade` | Mantém cidades e unidades federativas usadas na pesquisa. |
| `oportunidade` | Armazena vagas e cursos, seus requisitos, modalidade, situação, datas e link externo oficial. |
| `oportunidade_salva` | Registra as oportunidades salvas pelos jovens. |
| `servico_apoio` | Armazena informações e links de serviços e instituições de apoio. |

## 4. Relacionamentos principais

- Um usuário pode ter um perfil de jovem ou representar uma organização, conforme o perfil de acesso.
- Um perfil de jovem pode registrar várias formações e selecionar várias áreas de interesse. A relação com áreas é muitos-para-muitos e usa `perfil_area`.
- Uma organização pode divulgar várias oportunidades.
- Cada oportunidade pertence a uma categoria e pode estar associada a uma localidade. A localidade pode ficar vazia quando o curso ou a vaga for totalmente on-line ou remota.
- Jovens podem salvar várias oportunidades, e uma mesma oportunidade pode ser salva por vários jovens. A tabela `oportunidade_salva` implementa essa relação muitos-para-muitos.
- Uma organização pode estar relacionada a vários serviços de apoio. Os serviços também podem ser registrados sem vínculo direto com uma organização cadastrada.

## 5. Regras e integridade dos dados

- E-mail de usuário e CNPJ de organização são únicos.
- O CNPJ deve ser armazenado sem pontuação, em maiúsculas e validado pela aplicação. O formato atual pode conter letras e números nas primeiras 12 posições, seguido por dois dígitos verificadores.
- Solicitações de organização começam com `PENDENTE`; apenas organizações aprovadas devem poder publicar.
- Oportunidades começam com `PENDENTE_REVISAO`; somente oportunidades aprovadas devem aparecer no catálogo público.
- Cada oportunidade exige um `link_externo` para o canal oficial da instituição.
- Registros de oportunidade são encerrados ou desativados por estado, em vez de removidos, para preservar referências e favoritos.
- O esquema reserva `senha_hash` para contas persistidas; senhas não devem ser armazenadas em texto puro. A conta administrativa atual é configurada no ambiente local da API e não é salva no banco.
- A aplicação deve validar a idade mínima de 16 anos no cadastro. Não deve rejeitar pessoas com mais de 24 anos.
- Regras que dependem de mais de uma tabela, como impedir publicação por uma organização pendente, devem ser verificadas na API.

## 6. Decisões de escopo

As tabelas `candidatura` e `inscricao_curso` não foram incluídas porque o relatório define que o FASE direciona os usuários para os canais externos responsáveis. O sistema não seleciona candidatos, não realiza contratações e não ministra cursos.

Os campos de aprovação e revisão apoiam o fluxo da API: solicitações de organizações ficam pendentes até a análise administrativa, e oportunidades submetidas aguardam revisão antes da publicação. O painel local fornece as ações para esse fluxo.

## 7. Estado atual e próximas etapas

O script `FASE/database/schema.sql` foi executado no MySQL Workbench. O banco `fase_db` e suas tabelas foram criados sem erros, conforme o registro de execução apresentado. Ao executar novamente, o Workbench informou que o banco e as tabelas já existiam; esses avisos são esperados quando se usa `IF NOT EXISTS` e indicam que os objetos não foram recriados.

Durante a execução inicial, o MySQL também apresentou avisos de descontinuação para o uso de `UNSIGNED` em colunas `DECIMAL`. Para corrigir isso, as colunas monetárias `oportunidade.remuneracao` e `oportunidade.valor_curso` foram definidas como `DECIMAL(10,2)` com sinal, e foi incluída uma restrição `CHECK` para impedir valores negativos. A migração `FASE/database/migrations/001_remove_decimal_unsigned.sql` foi executada no banco existente. A conferência no Workbench mostrou `decimal(10,2)` para ambas as colunas.

Assim, a estrutura do banco está criada e a correção dos campos monetários foi confirmada. A API em `FASE/api` usa Spring Boot, JPA e o conector MySQL. Ela consulta oportunidades publicadas por tipo, modalidade e busca. O projeto usa Java 25 e Maven, e a API compilou com sucesso usando `mvn -DskipTests package`. O script `FASE/api/run-api.ps1` solicita as credenciais administrativas e do MySQL sem exibir ou gravar as senhas. Após executar o seed demonstrativo, `GET /api/oportunidades` retornou a vaga fictícia “Vaga demonstrativa — Jovem Aprendiz em Administração” (ID 1) e o curso “Curso demonstrativo de Introdução à Programação” (ID 2). Também foi conferido `GET /api/oportunidades?tipo=CURSO`, que retornou somente o curso (ID 2).

As páginas `vagas.html` e `cursos.html` consultam a API, montam cartões com os dados retornados e aplicam busca e filtros. O formulário `cadastro.html` envia solicitações para `POST /api/organizacoes`; os dados são validados e gravados com status `PENDENTE`. O painel `admin.html`, acessível após o login em `login.html`, permite aprovar ou recusar organizações e publicar ou recusar oportunidades. A conta administrativa é configurada ao iniciar a API e suas sessões expiram após oito horas. O cadastro de uma organização não cria automaticamente uma conta para ela; organizações ainda não enviam suas próprias oportunidades.

O formulário exibe mensagens de validação da API quando há campos inválidos. A solicitação de instituição foi enviada, analisada e aprovada no ambiente local.

Após uma tentativa de cadastro de instituição retornar “Informe um CNPJ válido com 14 dígitos”, foi identificado que o validador da API aceitava somente números. A validação foi atualizada para aceitar as 12 primeiras posições alfanuméricas e conferir os dois dígitos verificadores conforme o cálculo oficial. O formulário também permite digitar letras. CNPJs numéricos continuam aceitos. É necessário recompilar e reiniciar a API para usar a correção.

Foi criado `FASE/FASE_site/run-site.ps1` para servir o protótipo em `http://127.0.0.1:5500`, origem permitida pelo CORS; a configuração do VS Code abre esse endereço. O script de seed inclui três cursos fictícios adicionais (Excel Básico, Comunicação Profissional e Design Gráfico). O usuário executou a versão atualizada do seed e confirmou que os testes das páginas e dos filtros funcionaram.

Próximas etapas recomendadas:

1. Conferir o login administrativo e os fluxos de aprovação e revisão com dados fictícios no ambiente local.
2. Implementar autenticação e autorização para contas de organizações aprovadas, permitindo que cadastrem suas próprias oportunidades.
3. Implementar salvamento de oportunidades e conectar os demais formulários do protótipo ao backend.

## 8. Observação sobre privacidade e segurança

O modelo deve coletar apenas dados necessários às funções descritas. O formulário de organização grava dados de contato no banco local; use somente dados fictícios durante o desenvolvimento. A conta administrativa é configurada localmente no processo da API e não é armazenada no banco; use a aplicação apenas localmente. Antes de qualquer uso real, implemente armazenamento protegido de credenciais, autorização por perfil, transporte HTTPS, limitação de tentativas e outros controles de segurança. A API não deve retornar campos internos como `senha_hash`.
