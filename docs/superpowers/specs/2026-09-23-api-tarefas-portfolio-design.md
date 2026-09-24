# API de tarefas: preparação para portfólio

## Objetivo

Agregar valor ao CRUD existente para que o projeto demonstre organização, contratos HTTP previsíveis, persistência configurável e comportamento testado. O escopo não inclui uma V2 nem a reescrita do README, reservada para a próxima etapa.

## Estado atual

- `TarefaController` chama `TarefaRepository` diretamente e recebe/devolve a entidade JPA.
- A busca por ID retorna `Optional<Tarefa>` como resposta HTTP.
- A atualização lança `RuntimeException` quando não encontra a tarefa; a exclusão delega a `deleteById`, sem padronizar o caso ausente.
- Não há validação de payload nem tratamento global de erros.
- URL e usuário MySQL são fixos; Hibernate atualiza o schema automaticamente.
- Existe somente um teste que verifica a inicialização do contexto.
- O Maven Wrapper foi removido do último commit e não há workflow de CI.

## Decisões

### Estrutura da aplicação

Manter Java 17, Spring Boot 3 e a divisão simples `Controller → Service → Repository`. O controller traduz HTTP e DTOs; o service aplica as operações do CRUD; o repository permanece responsável por persistência. Não adicionar camadas ou abstrações sem necessidade do fluxo atual.

### Contrato HTTP

Adicionar `CriarTarefaRequest`, `AtualizarTarefaRequest` e `TarefaResponse`, seguindo o idioma usado pelo código existente. A entidade JPA não será entrada ou saída da API. Os campos de criação e atualização seguem o contrato existente da collection Postman: `nome`, `responsavel` e `dataEntrega` são obrigatórios; a data usa ISO `yyyy-MM-dd`. Nome e responsável não podem ser vazios e terão limite explícito de tamanho compatível com colunas de banco.

Os endpoints retornam `201 Created` na criação, `200 OK` em consultas, listagem e atualização, `204 No Content` na exclusão bem-sucedida, `404 Not Found` para recurso inexistente e `400 Bad Request` para payload inválido ou ilegível. PUT continua substituindo os três campos, como já documentado na collection.

### Documentação da API

Adicionar Swagger UI e OpenAPI usando `springdoc-openapi` da linha 2.2.x, compatível com o Spring Boot 3.1.x usado pelo projeto. A interface fica disponível em `/swagger-ui.html` e o documento JSON em `/v3/api-docs`. Descrever a API de tarefas existente; não documentar recursos de usuários, pedidos, itens ou autenticação, que não existem neste projeto.

### Erros

Adicionar exceção específica para tarefa inexistente e um `@RestControllerAdvice` que produz `ProblemDetail` para validação, JSON/data malformados e recurso ausente. Mensagens não devem expor detalhes internos do banco ou stack trace.

### Persistência e execução local

Ler conexão de `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`. Usar Flyway com uma migração inicial compatível com a tabela e colunas geradas atualmente pelo Hibernate. Habilitar baseline na versão inicial para bancos existentes e validar o schema após a migração. O `compose.yml` disponibiliza MySQL para desenvolvimento local sem misturar credenciais de produção.

Restaurar Maven Wrapper com uma versão Maven fixada. Adicionar workflow GitHub Actions para usar Java 17, cache Maven e executar `./mvnw test` em push e pull request. Adicionar configuração Dependabot para dependências Maven e GitHub Actions. As opções de secret scanning, push protection e code scanning são configurações do repositório GitHub e não são alteradas por arquivos locais nesta etapa.

### Testes

Substituir o teste de contexto isolado por testes de comportamento do serviço e da API. Cobrir criação, listagem, busca, atualização, exclusão, validação e IDs inexistentes, incluindo os status e corpos relevantes. Manter testes determinísticos sem exigir um servidor MySQL ou Docker para `./mvnw test`.

## Critérios de aceite

1. A API mantém as rotas e os nomes de campo existentes, sem expor a entidade JPA; Swagger UI e OpenAPI descrevem essas rotas.
2. Controller, service e repository têm responsabilidades separadas.
3. Validação e semântica HTTP seguem as decisões acima.
4. Banco e credenciais são configuráveis por variáveis de ambiente e schema é controlado por Flyway.
5. Wrapper, Compose, CI e configuração Dependabot estão presentes e coerentes entre si.
6. Testes demonstram os fluxos e falhas de negócio descritos.
7. O README permanece sem alterações nesta etapa.
8. A execução e os testes são verificados com um JDK compatível, se o ambiente permitir; limitações do ambiente serão registradas com precisão.

## Fora do escopo desta etapa

- Reescrever o README.
- Adicionar autenticação, usuários, pedidos ou itens.
- Alterar o About ou as configurações de segurança diretamente no GitHub.
- Autenticação, paginação, novas funcionalidades ou uma nova versão da API.
