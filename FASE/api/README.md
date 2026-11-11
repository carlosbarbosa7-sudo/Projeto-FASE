# API do FASE

API inicial em Spring Boot para consultar oportunidades publicadas no banco MySQL `fase_db`.

## Requisitos

- Java 25 LTS
- Maven 3.6.3 ou superior (a distribuição local deste ambiente fica em `.tools/apache-maven-3.10.0`)
- MySQL com o banco `fase_db` criado pelo script `../database/schema.sql`

## Configurar a conexão

`run-api.ps1` pergunta o nome do usuário MySQL (Enter usa `root`) e a senha, que não aparece enquanto você digita. Não coloque a senha nos arquivos do projeto nem a envie no chat. Se o servidor MySQL não estiver em `localhost:3306`, configure `DB_URL` antes de iniciar. O endereço padrão é `jdbc:mysql://localhost:3306/fase_db`.

## Iniciar

No PowerShell, entre nesta pasta (`FASE/api`) e execute:

```powershell
./run-api.ps1
```

O script pede o usuário e a senha da conta administradora e, em seguida, as credenciais do MySQL. As senhas são digitadas sem aparecer na tela e não são gravadas nos arquivos. A conta administrativa existe somente enquanto a API estiver em execução; escolha credenciais fortes e não as compartilhe.

A API ficará disponível em `http://127.0.0.1:8080` e aceita conexões somente deste computador por padrão. O Hibernate está configurado para não criar nem alterar tabelas (`ddl-auto=none`); o banco já foi criado pelo script SQL.

## Abrir o protótipo conectado

Mantenha a API rodando no primeiro terminal. Em um segundo terminal, entre em `FASE/FASE_site` e execute `./run-site.ps1`. Abra `http://127.0.0.1:5500` no navegador. Para acessar o painel, abra `http://127.0.0.1:5500/login.html` e entre com a conta configurada ao iniciar a API. A configuração do VS Code também abre o site depois que o servidor local estiver iniciado. Para carregar alterações recentes no código da API, pare a instância anterior com `Ctrl+C` e inicie-a novamente.

## Endpoints de consulta

- `GET /api/oportunidades` — lista oportunidades publicadas.
- `GET /api/oportunidades?tipo=VAGA` — filtra por tipo (`VAGA` ou `CURSO`).
- `GET /api/oportunidades?busca=design` — busca em título, descrição, requisitos, categoria e instituição.
- `GET /api/oportunidades?modalidade=REMOTO` — filtra por modalidade (`PRESENCIAL`, `HIBRIDO`, `REMOTO` ou `ONLINE`).
- `GET /api/oportunidades/{id}` — retorna os detalhes de uma oportunidade publicada.

Os filtros podem ser combinados. O código CORS permite, por padrão, o protótipo servido em `http://127.0.0.1:5500`; altere `FASE_CORS_ORIGIN` se o endereço local do protótipo for diferente.

## Solicitações e administração local

- `POST /api/organizacoes` — recebe o formulário do site. Valida os campos e o CNPJ e grava a organização com status `PENDENTE`. Não cria conta de acesso nem permite publicação automática.
- `POST /api/auth/login` — autentica conta administrativa local, jovem ou institucional aprovada e retorna uma sessão.
- `POST /api/auth/register/youth` — cria uma conta de jovem (idade mínima 16 anos), armazena a senha protegida por hash e inicia uma sessão.
- `POST /api/auth/change-password` — altera a senha da conta autenticada.
- `POST /api/auth/logout` — encerra a sessão apresentada no cabeçalho de autorização.
- `GET /api/me` — retorna os dados básicos da conta autenticada.
- `GET /api/me/oportunidades-salvas` — lista oportunidades salvas pelo perfil jovem.
- `POST /api/me/oportunidades-salvas/{id}` e `DELETE /api/me/oportunidades-salvas/{id}` — salvam ou removem uma oportunidade publicada.
- `GET /api/admin/organizacoes/pendentes` — lista solicitações aguardando análise.
- `GET /api/admin/organizacoes?status=PENDENTE|APROVADA|RECUSADA` — lista instituições por status para consulta administrativa.
- `PATCH /api/admin/organizacoes/{id}/status` — recebe `{"status":"APROVADA","motivo":"..."}` ou `{"status":"RECUSADA","motivo":"..."}`.
- `GET /api/admin/categorias?tipo=CURSO` e `GET /api/admin/localidades` — consulta os IDs necessários para cadastrar oportunidades.
- `POST /api/admin/oportunidades` — cadastra uma oportunidade vinculada a uma organização aprovada. Ela começa `PENDENTE_REVISAO`.
- `GET /api/admin/oportunidades/pendentes` — lista oportunidades aguardando análise.
- `PATCH /api/admin/oportunidades/{id}/status` — recebe `{"status":"PUBLICADA"}` ou `{"status":"RECUSADA"}`.

Todas as rotas `/api/admin/**` exigem uma sessão com perfil `ADMIN`. A sessão expira após oito horas ou quando o usuário sai do painel. O painel local em `admin.html` permite aprovar/recusar instituições e publicar/recusar oportunidades. Ao aprovar uma instituição, o painel gera uma senha provisória aleatória; o administrador entrega manualmente o e-mail de acesso e a senha à instituição. Jovens podem criar uma conta pela página `cadastro-jovem.html`; o login compartilhado em `login.html` encaminha cada perfil à sua área. As senhas de contas persistidas são armazenadas com hash PBKDF2; as credenciais do administrador local são configuradas ao iniciar a API. Este fluxo destina-se ao protótipo local; não exponha a API publicamente sem uma implantação com HTTPS e controles de segurança apropriados.

O formulário público salva dados de contato no banco local. Use somente dados fictícios durante o desenvolvimento. O acesso para organizações é criado após aprovação; ainda não há painel para que elas próprias cadastrem e gerenciem oportunidades.

