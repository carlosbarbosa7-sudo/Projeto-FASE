# Banco de dados do FASE

O arquivo `schema.sql` cria o banco inicial `fase_db` para MySQL 8.0.16 ou superior.

## Executar

Abra o cliente MySQL e execute:

```sql
SOURCE caminho/para/FASE/database/schema.sql;
```

O usuário do MySQL precisa ter permissão para criar o banco e as tabelas. O script não apaga bancos ou tabelas existentes; todas as tabelas usam `CREATE TABLE IF NOT EXISTS`.

Se o banco já tiver sido criado com uma versão anterior do esquema, execute uma única vez `migrations/001_remove_decimal_unsigned.sql`. Essa migração remove `UNSIGNED` das colunas `DECIMAL` e adiciona verificações para impedir valores negativos. Reexecutar `schema.sql` não altera tabelas já existentes.

## Entidades

- `usuario`, `perfil_jovem`, `formacao`, `area_interesse` e `perfil_area`: contas e perfil do jovem.
- `categoria`, `localidade` e `oportunidade`: catálogo de vagas e cursos, com link para o canal externo oficial.
- `oportunidade_salva`: favoritos do jovem.
- `servico_apoio`: informações e encaminhamento para serviços de apoio.

As organizações começam com status `PENDENTE`; oportunidades submetidas começam como `PENDENTE_REVISAO`. A aplicação deve impedir a publicação de oportunidades de organizações não aprovadas.

O público prioritário é de 16 a 24 anos, mas pessoas com mais de 24 também podem participar. O banco não impõe limite máximo de idade; a aplicação deve validar idade mínima de 16 anos. As tabelas não registram candidatura ou inscrição em cursos: `link_externo` guarda o destino oficial desses processos.

Este é o esquema inicial. A API Spring Boot deverá aplicar as regras de autorização por perfil, validar dados como CNPJ e URLs, calcular a elegibilidade etária e não expor `senha_hash` nas respostas.
## Dados demonstrativos para conferir a API

O script `seeds/001_oportunidades_demonstracao.sql` insere uma vaga e quatro cursos fictícios, com uma organização de demonstração. Os três cursos novos variam área, modalidade, cidade e preço. Ele serve somente para desenvolvimento local; os links usam `example.org` e não são inscrições reais. Se você já executou a versão anterior, execute o arquivo atualizado novamente: os registros antigos serão mantidos e os três cursos adicionais serão incluídos sem duplicação.

1. Deixe a API rodando com `FASE/api/run-api.ps1`.
2. No MySQL Workbench, abra uma conexão com o banco `fase_db`.
3. Selecione **File > Open SQL Script...** e abra `FASE/database/seeds/001_oportunidades_demonstracao.sql`.
4. Execute o script pelo ícone do raio (**Execute**). Ele pode ser executado novamente sem duplicar os registros demonstrativos.
5. Abra `http://127.0.0.1:8080/api/oportunidades` no navegador. A lista deve retornar uma vaga e quatro cursos fictícios.
6. Para conferir filtros, experimente `/api/oportunidades?tipo=VAGA` e `/api/oportunidades?tipo=CURSO`.

O seed cria uma organização demonstrativa já aprovada somente para facilitar a consulta local. Não use estes registros em ambiente real.
