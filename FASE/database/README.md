# Banco de dados do FASE

O arquivo `schema.sql` cria o banco inicial `fase_db` para MySQL 8.0.16 ou superior.

## Executar

Abra o cliente MySQL e execute:

```sql
SOURCE caminho/para/FASE/database/schema.sql;
```

O usuario do MySQL precisa ter permissao para criar o banco e as tabelas. O script nao apaga bancos ou tabelas existentes; todas as tabelas usam `CREATE TABLE IF NOT EXISTS`.

Se o banco ja tiver sido criado com uma versao anterior do esquema, execute uma unica vez `migrations/001_remove_decimal_unsigned.sql`. Essa migracao remove `UNSIGNED` das colunas `DECIMAL` e adiciona verificacoes para impedir valores negativos. Reexecutar `schema.sql` nao altera tabelas ja existentes.

## Entidades

- `usuario`, `perfil_jovem`, `formacao`, `area_interesse` e `perfil_area`: contas e perfil do jovem.
- `categoria`, `localidade` e `oportunidade`: catalogo de vagas e cursos, com link para o canal externo.
- `oportunidade_salva`: favoritos do jovem.
- `servico_apoio`: informacoes e encaminhamento para servicos de apoio.

As organizacoes comecam com status `PENDENTE`; oportunidades submetidas comecam como `PENDENTE_REVISAO`. A aplicacao deve impedir a publicacao de oportunidades de organizacoes nao aprovadas.

## Dados demonstrativos

O script `seeds/001_oportunidades_demonstracao.sql` adiciona quatro organizacoes ficticias, cinco vagas e cinco cursos, com cidades, categorias, modalidades, requisitos e valores variados. Ele e repetivel e pode ser executado novamente sem duplicar esses registros. Os links usam o dominio reservado `.example` e nao levam a processos reais. Dados da versao anterior sao mantidos junto com os novos.

1. Inicie a API com `FASE/api/run-api.ps1`.
2. No MySQL Workbench, abra uma conexao com o banco `fase_db`.
3. Abra e execute `FASE/database/seeds/001_oportunidades_demonstracao.sql`.
4. Consulte `http://127.0.0.1:8080/api/oportunidades` ou filtre por `?tipo=VAGA` e `?tipo=CURSO`.

O seed aprova automaticamente somente organizacoes ficticias para facilitar a consulta local. Nao use estes registros em ambiente real.

