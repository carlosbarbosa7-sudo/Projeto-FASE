# Relatório de acompanhamento do projeto FASE

**Projeto:** FASE — Força de Aprendizagem, Suporte e Empregabilidade  
**Documento de referência:** `FASE_Projeto_Interdisciplinar_Revisado.docx`  
**Data de atualização:** 3 de novembro de 2026  
**Finalidade:** registrar o escopo, o estado atual da implementação, a aderência aos requisitos e as próximas etapas do sistema.

## 1. Apresentação

O FASE é uma proposta de plataforma digital para organizar e divulgar oportunidades de emprego, estágio, aprendizagem profissional e cursos. O público prioritário definido no relatório acadêmico é formado por jovens de 16 a 24 anos, sem impedir a participação de pessoas com mais de 24 anos. A plataforma deve facilitar a pesquisa e o acesso aos canais oficiais de empresas e instituições.

O escopo definido no relatório é de divulgação e encaminhamento. O sistema não realiza processos seletivos, contratações, inscrições em cursos nem presta os serviços anunciados.

Este documento descreve o que foi identificado e desenvolvido até a data indicada. “Implementado” significa que há código ou estrutura correspondente no projeto; não significa, por si só, que todos os cenários foram submetidos a testes completos ou que a aplicação esteja pronta para uso público.

## 2. Resumo executivo

O projeto possui um protótipo web conectado a uma API e a um banco MySQL. A consulta de oportunidades, os filtros e o envio de solicitações de cadastro de organizações estão implementados. O fluxo administrativo local inclui login, análise de instituições e revisão de oportunidades. Nesta etapa foram acrescentados cadastro e login de jovens, contas institucionais criadas após aprovação, alteração de senha e oportunidades salvas por jovens.

A implementação está **parcialmente alinhada** à proposta: cobre a divulgação e consulta de oportunidades, o encaminhamento para links oficiais, aprovação administrativa, autenticação básica por perfil e favoritos. Ainda faltam funções importantes do relatório, como formação e áreas de interesse completas, gestão de anúncios pelas próprias instituições, expiração automática, serviços de apoio e avaliações formais de usabilidade e desempenho.

## 3. Tecnologias e arquitetura

O sistema segue a arquitetura em camadas prevista no relatório:

| Camada | Tecnologia/implementação |
|---|---|
| Apresentação | HTML5, CSS3 e JavaScript, em `FASE/FASE_site` |
| Aplicação e regras de negócio | Java 25 e Spring Boot, em `FASE/api` |
| Persistência | MySQL, definida em `FASE/database/schema.sql` |
| Comunicação | API HTTP local, padrão `127.0.0.1:8080`; site local em `127.0.0.1:5500` |

A API usa Spring Data JPA e validação de dados. O esquema do banco reúne entidades para usuários e perfis, organizações, oportunidades, categorias, localidades, favoritos e serviços de apoio. A existência de tabelas no banco não significa que todas as funções associadas já tenham telas ou endpoints implementados.

## 4. Funcionalidades implementadas

### 4.1 Site e consulta pública

- Páginas para início, vagas, cursos, materiais, apresentação do projeto, login e cadastro de organizações.
- Consulta de vagas e cursos publicados pela API.
- Busca textual e filtros de consulta por categoria, localidade e modalidade na listagem.
- Exibição de detalhes da oportunidade e direcionamento ao link externo informado pela instituição.
- Estilos compartilhados e ajustes responsivos para telas menores.

### 4.2 Cadastro de organizações

- Formulário de solicitação de cadastro com tipo, nome, CNPJ, responsável, contato, site opcional, atuação e localidade.
- Envio para `POST /api/organizacoes`.
- Validação dos campos pela API; o CNPJ é verificado antes do registro.
- Solicitações novas ficam com status `PENDENTE`.
- Ao aprovar uma solicitação pendente, a API cria uma conta institucional vinculada à organização. O painel gera uma senha provisória aleatória, exibida uma única vez para o administrador entregar à instituição.
- Organizações aprovadas antes desta funcionalidade não recebem automaticamente uma conta.

### 4.3 Administração local

- Login administrativo em `login.html`.
- Painel `admin.html` para listar solicitações pendentes e oportunidades aguardando revisão.
- Ações para aprovar/recusar instituições e publicar/recusar oportunidades.
- A aprovação de uma nova instituição gera a credencial provisória; a instituição usa o e-mail cadastrado para entrar e pode alterar a senha.
- Credenciais administrativas informadas ao iniciar a API por `run-api.ps1`; não existe usuário ou senha padrão embutido no projeto.
- Sessões de todos os perfis mantidas em memória pela API, com expiração após oito horas.
- O cadastro de uma oportunidade é feito pela API administrativa e exige uma organização já aprovada.

Esse fluxo é destinado ao desenvolvimento local. As sessões são perdidas quando a API reinicia, não há recuperação de senha e as instituições ainda não têm um painel próprio para gerenciar anúncios.

### 4.4 Contas de jovens e oportunidades salvas

- Cadastro de jovem com dados básicos e idade mínima de 16 anos, sem limite máximo de idade.
- Senhas persistidas protegidas com hash PBKDF2-HMAC-SHA256 e salt aleatório.
- Login compartilhado para jovens, instituições aprovadas e administrador, com autorização por perfil nas rotas administrativas e de favoritos.
- Área da conta com consulta de perfil, alteração de senha e listagem de oportunidades salvas.
- Jovens podem salvar oportunidades publicadas na listagem e remover itens salvos na área da conta.
- A conta institucional permite consultar seus dados e alterar senha; autoria e gestão de oportunidades pela própria instituição ainda não estão disponíveis.

### 4.5 Banco e dados de demonstração

- Esquema MySQL com restrições, relacionamentos e status para organizações e oportunidades.
- Migração para ajustar colunas monetárias e impedir valores negativos.
- Script de dados demonstrativos para testar listagens de oportunidades.
- O modelo registra o link externo das candidaturas e inscrições; o FASE não processa esses procedimentos.

## 5. Aderência aos requisitos funcionais do relatório

| Requisito | Situação observada | Observação |
|---|---|---|
| RF01 — Cadastro e autenticação de jovens | Parcialmente implementado | Jovens podem se cadastrar e entrar; recuperação de senha e testes integrados ainda faltam. |
| RF02 — Perfil, formação e áreas de interesse | Parcial | Há perfil básico e escolaridade; formação e áreas de interesse não estão integradas ao fluxo. |
| RF03 — Cadastro e categorização de oportunidades | Parcial | Existem categorias e cadastro administrativo pela API; falta uma interface completa de autoria e gestão. |
| RF04 — Publicação por empresas e instituições identificadas | Parcial | Instituições aprovadas têm login; cadastro e gestão de oportunidades continuam sob responsabilidade do administrador. |
| RF05 — Pesquisa por palavra-chave | Implementado parcialmente | Busca disponível nas listagens; a cobertura depende dos campos consultados pelo fluxo. |
| RF06 — Filtro por categoria, localização e área profissional | Parcial | Categoria e localidade são filtráveis; área profissional/interesses do perfil não está integrada. |
| RF07 — Detalhes, instituição, publicação e prazo/situação | Parcial | Detalhes, instituição e link são exibidos; a apresentação completa de datas, prazos e situação ainda precisa ser concluída. |
| RF08 — Salvar e remover oportunidades | Parcialmente implementado | Jovens podem salvar oportunidades publicadas e removê-las na área da conta; ainda falta validação funcional ponta a ponta. |
| RF09 — Informações e contato de serviços de apoio | Não implementado no fluxo da aplicação | O banco prevê serviços de apoio, mas não há catálogo conectado demonstrado. |
| RF10 — Direcionamento ao canal oficial | Implementado | As oportunidades incluem link externo para a instituição responsável. |
| RF11 — Atualizar, encerrar ou retirar anúncios pelo responsável | Não implementado | Instituições aprovadas têm conta, mas ainda não existe painel próprio para gerenciar anúncios. |
| RF12 — Revisão, denúncia e desativação de conteúdo | Parcial | O administrador pode revisar oportunidades; denúncias e expiração automática não foram implementadas. |

## 6. Aderência aos requisitos não funcionais

| Requisito | Situação observada |
|---|---|
| RNF01 — Interface responsiva | Há regras responsivas no CSS; cobertura de usabilidade em diferentes aparelhos ainda precisa ser verificada. |
| RNF02 — Navegação e mensagens compreensíveis | O site e os formulários incluem navegação e mensagens de estado/erro; falta avaliação formal com participantes. |
| RNF03 — Persistência e cópias de segurança | O MySQL e o esquema estão presentes; política e procedimento de backup não foram implementados ou validados. |
| RNF04 — Controle de acesso por perfil | Há sessões e verificações de perfil para administração e favoritos; cobertura completa de autorização e testes ainda é necessária. |
| RNF05 — Minimização de dados e aviso de privacidade | O formulário coleta dados de contato da organização; aviso de privacidade e revisão formal do tratamento de dados ainda são necessários antes de dados reais. |
| RNF06 — Proteção de credenciais e dados | Senhas de jovens e instituições são armazenadas com hash PBKDF2; a senha administrativa é configurada no processo local e as sessões ficam em memória. Faltam HTTPS, proteção contra tentativas repetidas, recuperação de senha e controles de produção. |
| RNF07 — Modularidade e histórico de alterações | O código da API está separado em pacotes por domínio; histórico/auditoria completo das alterações de oportunidades não foi demonstrado. |
| RNF08 — Pesquisa em até 2 segundos | Não há medição documentada conforme a meta (base de até 1.000 oportunidades, cinco execuções por consulta e registro do ambiente). |

## 7. Fluxos atuais

### Solicitação e análise de uma organização

1. A organização envia o formulário no site.
2. A API valida e grava a solicitação com status `PENDENTE`.
3. O administrador entra no painel e analisa a solicitação.
4. Ao aprovar uma solicitação pendente, a API cria uma conta institucional e o painel exibe uma senha provisória para entrega manual; ao recusar, nenhuma conta é criada.
5. A instituição aprovada entra usando o e-mail cadastrado e pode alterar a senha. O administrador ainda registra as oportunidades em nome dela.

### Cadastro e revisão de oportunidade

1. O administrador cadastra uma oportunidade vinculada a uma organização aprovada.
2. A oportunidade inicia em `PENDENTE_REVISAO`.
3. O administrador pode publicar ou recusar a oportunidade.
4. O site público lista oportunidades publicadas e direciona o usuário ao link externo.

O fluxo ainda não permite que a instituição faça login e gerencie diretamente seus anúncios.

## 8. Validação e evidências disponíveis

- Foi realizada compilação limpa da API com Maven e Java 25; o resultado foi **BUILD SUCCESS**.
- A compilação não executa testes automatizados; não há suíte de testes da API no projeto.
- Foi verificada a sintaxe do script de inicialização PowerShell.
- A validação pelo navegador não foi concluída nesta etapa porque o runtime Playwright não encontrou o navegador Chromium instalado.
- O usuário informou que conseguiu acessar o sistema e que o login administrativo funcionou.
- Não foi registrada uma validação funcional completa do cadastro de jovem, favoritos, aprovação institucional e login institucional contra uma instância ativa de MySQL.
- Não foram apresentados resultados de testes com participantes nem medições de desempenho conforme os critérios acadêmicos do relatório.

## 9. Limitações e cuidados

- O sistema permanece um protótipo local e não deve ser exposto publicamente como está.
- Deve-se usar somente dados fictícios durante o desenvolvimento, conforme as orientações já existentes no projeto.
- A conta administrativa é configurada a cada inicialização da API; suas sessões são temporárias e mantidas em memória.
- Organizações aprovadas antes da inclusão de contas institucionais permanecem sem usuário vinculado; o fluxo atual só cria conta ao aprovar uma solicitação pendente nova.
- Não há recuperação de senha, edição completa de perfil nem gestão de anúncios pela instituição.
- Não se deve afirmar que os requisitos foram totalmente atendidos sem testes funcionais, de segurança, privacidade, usabilidade e desempenho.
- O sistema divulga oportunidades e encaminha para canais oficiais; não realiza seleção, contratação ou inscrição.

## 10. Próximas etapas recomendadas

1. Validar com MySQL o cadastro de jovem, login, alteração de senha, favoritos e o ciclo de aprovação e login institucional; confirmar o comportamento com dados fictícios.
2. Definir uma forma administrativa segura de vincular contas às organizações que já estavam aprovadas antes desta alteração.
3. Implementar formação e áreas de interesse no perfil jovem.
4. Criar um painel institucional para cadastrar, atualizar, encerrar e retirar anúncios, mantendo a revisão administrativa.
5. Completar datas, prazos, validade e expiração automática de anúncios; avaliar também o fluxo de denúncia.
6. Implementar e conectar o catálogo de serviços de apoio.
7. Definir aviso de privacidade, minimização, retenção e segurança antes de coletar dados reais.
8. Definir backup e executar testes de recuperação.
9. Criar casos de teste rastreáveis aos RFs e RNFs e validar a interface no navegador.
10. Realizar avaliação exploratória de usabilidade prevista no relatório e medir desempenho da pesquisa conforme o critério de aceite.
11. Atualizar o relatório acadêmico original com evidências reais, limitações e resultados obtidos, distinguindo entregas concluídas de resultados esperados.

## 11. Conclusão

O FASE já possui uma base funcional para consulta de vagas e cursos, envio de solicitações de organizações, análise administrativa local, autenticação inicial por perfil e oportunidades salvas para jovens. A API compila com Java 25, mas os novos fluxos ainda não foram validados de ponta a ponta contra o banco.

O sistema ainda é uma versão inicial e não cobre todo o conjunto de requisitos da proposta. Em especial, faltam perfis completos, gestão de anúncios pelas instituições, catálogo conectado de apoio, expiração, controles de produção e evidências da avaliação acadêmica. A próxima fase deve priorizar os testes funcionais dos fluxos novos e implementar os requisitos restantes de forma incremental, registrando testes e limitações.
