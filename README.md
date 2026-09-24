# API de Gerenciamento de Tarefas

[![CI](https://github.com/MiguelZGobbo/API-de-Gerenciamento-de-Tarefas/actions/workflows/ci.yml/badge.svg)](https://github.com/MiguelZGobbo/API-de-Gerenciamento-de-Tarefas/actions/workflows/ci.yml)

API REST para organizar tarefas por nome, responsável e data de entrega. O projeto demonstra um CRUD com contrato HTTP definido, validação de entrada, persistência em MySQL e testes que cobrem tanto a lógica da aplicação quanto sua execução com banco real.

## O que a API faz

- Cria, lista, consulta, atualiza e exclui tarefas.
- Exige `nome`, `responsavel` e `dataEntrega` na criação e na atualização. Textos em branco ou com mais de 255 caracteres e datas inválidas ou fora do formato `yyyy-MM-dd` são rejeitados.
- Retorna `201` na criação, `200` nas consultas e atualizações, `204` na exclusão, `400` para requisições inválidas e `404` para tarefas inexistentes. Erros seguem o formato `ProblemDetail`.
- Persiste os dados em MySQL com schema versionado pelo Flyway e oferece documentação interativa com Swagger UI.

## Stack

Java 17 · Spring Boot 3.1.5 (Web MVC, Data JPA e Validation) · MySQL 8 · Flyway · springdoc OpenAPI · Maven Wrapper · Docker Compose · JUnit 5, Mockito e MockMvc · GitHub Actions

## Executar localmente

É necessário ter **Java 17** e **Docker Compose**. O Maven Wrapper incluído no repositório baixa a versão do Maven usada pelo projeto.

```bash
git clone https://github.com/MiguelZGobbo/API-de-Gerenciamento-de-Tarefas.git
cd API-de-Gerenciamento-de-Tarefas
```

Na pasta do projeto, copie o exemplo de configuração, inicie o MySQL e execute a aplicação. O Compose espera o banco ficar saudável antes de retornar.

**Linux/macOS**

```bash
cp .env.example .env
docker compose up -d --wait
DB_USERNAME=tarefas DB_PASSWORD=tarefas_dev_password ./mvnw spring-boot:run
```

**Windows (PowerShell)**

```powershell
Copy-Item .env.example .env
docker compose up -d --wait
$env:DB_USERNAME = 'tarefas'
$env:DB_PASSWORD = 'tarefas_dev_password'
.\mvnw.cmd spring-boot:run
```

A API fica em `http://localhost:8080`. Os valores de usuário e senha acima são apenas para desenvolvimento local; se alterar o `.env`, use os mesmos valores ao iniciar a aplicação. Para encerrar o banco, execute `docker compose down`.

| Variável | Uso |
| --- | --- |
| `DB_URL` | URL JDBC da aplicação. Opcional para o MySQL local na porta 3306; necessária para outro endereço. |
| `DB_USERNAME` | Usuário do MySQL usado pela aplicação e pelo Compose. Obrigatória na aplicação. |
| `DB_PASSWORD` | Senha desse usuário. Obrigatória na aplicação. |
| `MYSQL_ROOT_PASSWORD` | Senha do usuário root do contêiner, opcional para a configuração local do Compose. |

O arquivo `.env` é lido pelo Compose; ao executar a aplicação com o Maven Wrapper, informe as variáveis à sessão do terminal como nos comandos acima. Não versione credenciais reais.

## Como está organizado

O fluxo é `Controller → Service → Repository → MySQL`. O controller recebe e devolve DTOs, o service executa o CRUD e o repository usa Spring Data JPA. A entidade `Tarefa` permanece interna; o `GlobalExceptionHandler` concentra as respostas de erro. O Flyway cria o schema e o Hibernate o valida na inicialização.

```text
src/main/java/com/miguel/tarefas/
  controller/  dto/  service/  repository/  model/  exception/
src/main/resources/db/migration/   # migrações Flyway
src/test/java/com/miguel/tarefas/  # testes HTTP, de service e de integração
.github/workflows/ci.yml           # testes no GitHub Actions
compose.yml                         # MySQL local
.env.example                        # configuração de exemplo
mvnw / mvnw.cmd                     # Maven Wrapper
```

## Testes e automação

`./mvnw test` executa testes do controller e do service sem exigir MySQL. No Windows, use `.\mvnw.cmd test`.

Com o banco iniciado e as variáveis configuradas, `./mvnw -Pintegration-tests verify` (ou `.\mvnw.cmd -Pintegration-tests verify`) também inicia a API, confirma a migração Flyway e exercita o CRUD por HTTP. O [GitHub Actions](https://github.com/MiguelZGobbo/API-de-Gerenciamento-de-Tarefas/actions/workflows/ci.yml) executa esses dois níveis em jobs separados, com MySQL temporário no job de integração, e valida a configuração do Compose. O Dependabot verifica atualizações do Maven e das Actions semanalmente.

## Documentação da API

Com a aplicação em execução, acesse o [Swagger UI](http://localhost:8080/swagger-ui.html) ou o [documento OpenAPI](http://localhost:8080/v3/api-docs). A [collection do Postman](./API%20Tarefas.postman_collection.json) também está disponível.

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/tarefas` | Criar tarefa |
| `GET` | `/tarefas` | Listar tarefas |
| `GET` | `/tarefas/{id}` | Consultar tarefa |
| `PUT` | `/tarefas/{id}` | Atualizar tarefa |
| `DELETE` | `/tarefas/{id}` | Excluir tarefa |

## Decisões técnicas

- **DTOs separados da entidade JPA:** mantêm o contrato HTTP explícito e permitem validar entradas antes de persistir.
- **Camadas simples:** controller, service e repository têm responsabilidades distintas sem acrescentar abstrações desnecessárias ao CRUD.
- **Flyway com `ddl-auto=validate`:** mudanças no schema são versionadas; o baseline está configurado para reconhecer bancos criados antes da migração.
- **Testes em dois níveis:** os testes rápidos funcionam sem banco local, e a integração no CI verifica a aplicação completa contra MySQL.

Projeto iniciado em contexto acadêmico e aprimorado para apresentar práticas de desenvolvimento backend. Desenvolvido por [Miguel Zager Gobbo](https://github.com/MiguelZGobbo).
