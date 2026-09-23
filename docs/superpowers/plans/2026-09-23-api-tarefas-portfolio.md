# API de tarefas para portfólio: plano de implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Preparar o CRUD existente para apresentação em portfólio, com camadas simples, contratos HTTP validados, configuração de banco reproduzível e testes relevantes.

**Architecture:** Manter Java 17 e Spring Boot 3. O controller recebe e retorna DTOs, chama um service que concentra as operações do CRUD e usa o repository apenas por meio desse service; a entidade JPA permanece interna. Flyway cria ou valida o schema, Compose fornece MySQL de desenvolvimento e GitHub Actions executa os testes pelo Maven Wrapper.

**Tech Stack:** Java 17, Spring Boot 3.1.5, Spring MVC, springdoc-openapi 2.2.0, Bean Validation, Spring Data JPA, Flyway, MySQL 8, Apache Maven 3.9.16 via Wrapper, GitHub Actions, JUnit 5 e MockMvc.

**Spec:** `docs/superpowers/specs/2026-09-23-api-tarefas-portfolio-design.md`

## Global Constraints

- Manter Java 17, Spring Boot 3 e a divisão simples `Controller → Service → Repository`.
- Preservar as rotas e os nomes de campo `nome`, `responsavel` e `dataEntrega`.
- Documentar as rotas reais em Swagger UI (`/swagger-ui.html`) e OpenAPI JSON (`/v3/api-docs`).
- A criação e a atualização exigem os três campos; a data usa ISO `yyyy-MM-dd`.
- Usar `201 Created`, `200 OK`, `204 No Content`, `404 Not Found` e `400 Bad Request` conforme definido na especificação.
- Não exigir MySQL ou Docker para executar `./mvnw test`.
- Manter o README sem alterações nesta etapa.
- Não alterar diretamente configurações remotas do GitHub.

## Review Focus

- Campo JSON ausente ou `null`: retornar 400 com erro de validação legível. Coberto nos testes de validação da Task 2.
- Nome ou responsável vazio, só com espaços ou maior que 255 caracteres: retornar 400. Coberto nos testes de validação da Task 2.
- Data inválida, incluindo data inexistente no calendário: retornar 400 sem stack trace. Coberto nos testes de payload inválido da Task 2.
- ID inexistente em GET, PUT ou DELETE: retornar 404 no mesmo formato ProblemDetail. Coberto nos testes de recurso ausente da Task 2.
- ID zero ou negativo: retornar 400 antes de consultar o repository. Coberto no teste de parâmetro inválido da Task 2.

---

### Task 1: Restaurar Maven Wrapper para habilitar testes reproduzíveis

**Files:**
- Create: `mvnw`
- Create: `mvnw.cmd`
- Create: `.mvn/wrapper/maven-wrapper.properties`
- Use `only-script` para não adicionar JAR nem fonte auxiliar ao repositório.

**Interfaces:**
- Wrapper fixa Maven 3.9.16 e funciona no Windows e em runners Linux.
- Os comandos das tarefas seguintes usam `./mvnw` em Unix e `./mvnw.cmd` no PowerShell.

- [x] **Step 1: Disponibilizar JDK 17 na sessão de desenvolvimento**

Verifique `java -version`. Se não houver JDK 17, baixe o ZIP Temurin 17 para Windows pelo endpoint oficial `https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse`, extraia em `%LOCALAPPDATA%\codex-tools\temurin-17`, defina `JAVA_HOME` e acrescente `%JAVA_HOME%\bin` ao `PATH` da sessão. Confira `java -version`. Não adicione o JDK ao repositório.

```powershell
$jdkZip = Join-Path $env:TEMP 'temurin-17.zip'
Invoke-WebRequest 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse' -OutFile $jdkZip
Expand-Archive $jdkZip -DestinationPath "$env:LOCALAPPDATA\codex-tools\temurin-17" -Force
$jdkHome = (Get-ChildItem "$env:LOCALAPPDATA\codex-tools\temurin-17" -Directory | Select-Object -First 1).FullName
$env:JAVA_HOME = $jdkHome
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
```

- [x] **Step 2: Gerar os scripts oficiais do Maven Wrapper**

Baixe o Maven 3.9.16 oficial para `%TEMP%`, extraia-o fora do repositório e gere os scripts com Maven Wrapper Plugin 3.3.4:

```powershell
$mavenZip = Join-Path $env:TEMP 'apache-maven-3.9.16-bin.zip'
Invoke-WebRequest 'https://archive.apache.org/dist/maven/maven-3/3.9.16/binaries/apache-maven-3.9.16-bin.zip' -OutFile $mavenZip
Expand-Archive $mavenZip -DestinationPath $env:TEMP -Force
& "$env:TEMP\apache-maven-3.9.16\bin\mvn.cmd" 'org.apache.maven.plugins:maven-wrapper-plugin:3.3.4:wrapper' '-Dmaven=3.9.16' '-Dtype=only-script'
```

Mantenha `mvnw`, `mvnw.cmd` e `.mvn/wrapper/maven-wrapper.properties`; confirme que `distributionUrl` aponta para `apache-maven-3.9.16-bin.zip` e preserve os arquivos gerados.

- [x] **Step 3: Verificar a versão e registrar uma linha de base**

Execute `./mvnw --version` se houver shell POSIX e `./mvnw.cmd --version` no PowerShell; cada comando deve informar Maven 3.9.16. Execute também `./mvnw test` ou `./mvnw.cmd test` e registre falhas preexistentes antes das próximas tarefas.

- [x] **Step 4: Marcar o script Unix como executável**

Execute `git update-index --chmod=+x mvnw` e confirme o modo `100755` com `git ls-files --stage mvnw`.

- [x] **Step 5: Commit da Task 1**

```bash
git add mvnw mvnw.cmd .mvn/wrapper
git commit -m "build: restore Maven Wrapper"
```

### Task 2: Separar o CRUD e estabilizar o contrato HTTP

**Files:**
- Create: `src/main/java/com/miguel/tarefas/dto/CriarTarefaRequest.java`
- Create: `src/main/java/com/miguel/tarefas/dto/AtualizarTarefaRequest.java`
- Create: `src/main/java/com/miguel/tarefas/dto/TarefaResponse.java`
- Create: `src/main/java/com/miguel/tarefas/service/TarefaService.java`
- Create: `src/main/java/com/miguel/tarefas/exception/TarefaNaoEncontradaException.java`
- Create: `src/main/java/com/miguel/tarefas/exception/GlobalExceptionHandler.java`
- Modify: `src/main/java/com/miguel/tarefas/controller/TarefaController.java`
- Modify: `pom.xml` (adicionar `spring-boot-starter-validation` e `springdoc-openapi-starter-webmvc-ui`)
- Test: `src/test/java/com/miguel/tarefas/controller/TarefaControllerTest.java`
- Test: `src/test/java/com/miguel/tarefas/service/TarefaServiceTest.java`
- Remove: `src/test/java/com/miguel/tarefas/TarefasApplicationTests.java` quando os testes de comportamento substituírem o teste vazio de contexto

**Interfaces:**
- `TarefaService.criar(CriarTarefaRequest): TarefaResponse`
- `TarefaService.listarTodos(): List<TarefaResponse>`
- `TarefaService.buscarPorId(Long): TarefaResponse`
- `TarefaService.atualizar(Long, AtualizarTarefaRequest): TarefaResponse`
- `TarefaService.deletar(Long): void`
- Controller depende somente de `TarefaService`; DTOs não dependem de JPA.

- [x] **Step 1: Escrever teste MockMvc de criação com status 201**

Use `@WebMvcTest(TarefaController.class)`, `MockMvc` e `@MockBean TarefaRepository` para que o teste compile e exercite o controller atual antes da extração do service. O POST abaixo deve falhar inicialmente com 200, demonstrando a semântica atual incorreta.

```java
@Test
void deveCriarTarefaERetornar201ComLocation() throws Exception {
    Tarefa tarefa = new Tarefa();
    tarefa.setId(1L);
    tarefa.setNome("Estudar Spring");
    tarefa.setResponsavel("Miguel");
    tarefa.setDataEntrega(LocalDate.parse("2026-10-10"));
    when(tarefaRepository.save(any(Tarefa.class))).thenReturn(tarefa);

    mockMvc.perform(post("/tarefas")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"nome":"Estudar Spring","responsavel":"Miguel","dataEntrega":"2026-10-10"}
                            """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", endsWith("/tarefas/1")))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nome").value("Estudar Spring"));
}
```

Na mesma classe, adicione testes para `GET /tarefas` e `GET /tarefas/1` com 200, `PUT /tarefas/1` com 200 e `DELETE /tarefas/1` com 204 e corpo vazio. Com o controller atual, pelo menos o POST e o DELETE devem falhar nos status esperados.

Adicione também o teste da documentação OpenAPI antes de adicionar a dependência:

```java
@Test
void devePublicarDocumentacaoOpenApiDasTarefas() throws Exception {
    mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.openapi").isNotEmpty())
            .andExpect(jsonPath("$.paths['/tarefas']").exists());
}
```

- [x] **Step 2: Rodar os testes novos e confirmar falha na implementação atual**

Execute `./mvnw -Dtest=TarefaControllerTest test` (ou `./mvnw.cmd -Dtest=TarefaControllerTest test` no PowerShell). Esperado: o teste compila e falha no status 201 porque o controller atual responde 200.

- [x] **Step 3: Cobrir campos ausentes, inválidos e parâmetros de ID**

Adicione casos de `nome` vazio/maior que 255, `responsavel` em branco/maior que 255, ausência ou `null` de cada campo, data inválida (`2026-02-30`), corpo JSON malformado, ID não numérico, `0` e `-1`. Cada caso deve esperar 400 e objeto ProblemDetail com `status` e `detail`. Antes da extração, verifique que o repository não é chamado para ID não numérico; após o Step 7, adapte essa verificação para o service nos IDs não positivos.

```java
@ParameterizedTest
@MethodSource("payloadsInvalidos")
void deveRejeitarCorpoInvalido(String corpo) throws Exception {
    mockMvc.perform(post("/tarefas").contentType(MediaType.APPLICATION_JSON).content(corpo))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").isNotEmpty());
}

static Stream<String> payloadsInvalidos() {
    return Stream.of(
            """
            {"nome":" ","responsavel":"Miguel","dataEntrega":"2026-10-10"}
            """,
            """
            {"nome":"OK","responsavel":"Miguel","dataEntrega":"2026-02-30"}
            """,
            """
            {"nome":"OK","responsavel":"Miguel"}
            """
    );
}
```

- [x] **Step 4: Cobrir recurso ausente nas três operações por ID**

Antes da extração do service, configure `tarefaRepository.findById(404L)` para retornar vazio. Escreva expectativas 404 para GET, PUT e DELETE e ProblemDetail no corpo; contra o controller atual os testes devem falhar (GET/deletar respondem incorretamente e PUT tende a 500). Ao executar o Step 7, troque o mock do repository por `TarefaService` que lança `TarefaNaoEncontradaException` e mantenha os mesmos asserts. A resposta não pode conter nomes de classes, SQL ou stack trace.

- [x] **Step 5: Implementar DTOs validados e adicionar a dependência de validação**

Crie records Java com os campos `Long id`, `String nome`, `String responsavel` e `LocalDate dataEntrega` na resposta. Nos DTOs de entrada use `@NotBlank @Size(max = 255)` em `nome` e `responsavel`, além de `@NotNull` em `dataEntrega`. Adicione `spring-boot-starter-validation` sem versão manual, pois o parent Spring Boot gerencia a versão. Adicione `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0`, compatível com Spring Boot 3.1.x.

- [x] **Step 6: Escrever testes unitários do service e executá-los em vermelho**

Use JUnit 5 e Mockito para verificar criação e mapeamento dos campos, lista vazia/com itens, busca existente, atualização preservando o ID persistido, exclusão existente e `TarefaNaoEncontradaException` em GET/PUT/DELETE quando `findById` retorna vazio. Não inicialize Spring nesses testes.

```java
@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {
    @Mock TarefaRepository tarefaRepository;
    @InjectMocks TarefaService tarefaService;

    @Test
    void deveLancarExcecaoAoBuscarIdAusente() {
        when(tarefaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(TarefaNaoEncontradaException.class, () -> tarefaService.buscarPorId(404L));
    }
}
```

- [x] **Step 7: Implementar service, exceção e controller por construtor**

Altere o teste MockMvc para mockar `TarefaService` em vez de `TarefaRepository` e mantenha os asserts de contrato. Injete `TarefaRepository` por construtor em `TarefaService`; converta DTO para `Tarefa` ao gravar e entidade para `TarefaResponse` ao responder. No PUT, busque a tarefa existente e altere apenas `nome`, `responsavel` e `dataEntrega`, sem copiar ID do corpo. Injete apenas `TarefaService` no controller. Use `@Valid` nos corpos e `@Validated`/`@Positive` no ID. POST retorna `ResponseEntity.created(URI.create("/tarefas/" + resposta.id())).body(resposta)`; DELETE retorna `ResponseEntity.noContent().build()`.

- [x] **Step 8: Implementar respostas ProblemDetail e repetir os testes**

No `@RestControllerAdvice`, trate `TarefaNaoEncontradaException` como 404, `MethodArgumentNotValidException` como 400 com erros de campo determinísticos, `ConstraintViolationException` como 400 para ID zero/negativo, `MethodArgumentTypeMismatchException` como 400 para ID não numérico e `HttpMessageNotReadableException` como 400 com detalhe genérico. Reexecute os testes do controller e service; ambos devem passar.

- [x] **Step 9: Executar a suíte e revisar o diff da Task 2**

Execute `./mvnw test`. Confirme que a entidade JPA não aparece nos parâmetros/retornos do controller, que os campos da collection foram preservados, que `/v3/api-docs` lista as rotas de tarefas e que os cinco pontos de Review Focus têm testes. Remova o teste de contexto vazio somente após os testes de comportamento passarem.

- [x] **Step 10: Commit da Task 2**

```bash
git add pom.xml src/main/java src/test/java
git commit -m "feat(api): add service layer and validated task contract"
```

### Task 3: Externalizar o banco e adicionar migração e Compose

**Files:**
- Modify: `src/main/java/com/miguel/tarefas/model/Tarefa.java` apenas para explicitar nomes da tabela/colunas se necessário para alinhar a migração
- Modify: `src/main/resources/application.properties`
- Create: `src/main/resources/db/migration/V1__create_tarefa.sql`
- Create: `compose.yml`
- Modify: `.gitignore` para ignorar `.env` mantendo `.env.example` versionável
- Create: `.env.example` com credenciais exclusivamente locais de desenvolvimento
- Modify: `pom.xml` para incluir Flyway com versão gerenciada pelo Spring Boot

**Interfaces:**
- A aplicação lê `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`.
- Banco local da Compose: `tarefasdb`, usuário `tarefas`, porta `3306`.
- URL de desenvolvimento `jdbc:mysql://localhost:3306/tarefasdb?useSSL=false&allowPublicKeyRetrieval=true`; `DB_USERNAME` e `DB_PASSWORD` são obrigatórias e seguem as variáveis locais de `.env.example`.
- Schema: tabela `tarefa` com `id BIGINT AUTO_INCREMENT`, `nome VARCHAR(255)`, `responsavel VARCHAR(255)` e `data_entrega DATE`, compatível com o mapeamento atual.

- [ ] **Step 1: Adicionar Flyway e configurações externas**

Configure `spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/tarefasdb?useSSL=false&allowPublicKeyRetrieval=true}`, `spring.datasource.username=${DB_USERNAME}` e `spring.datasource.password=${DB_PASSWORD}` sem defaults de credenciais na aplicação. Desative `spring.jpa.show-sql`, remova dialeto explícito desnecessário, defina `spring.jpa.hibernate.ddl-auto=validate`, habilite Flyway e configure baseline-on-migrate na versão `1` para reconhecer bancos existentes que já têm a tabela. O `.env.example` contém apenas credenciais locais de demonstração.

- [ ] **Step 2: Criar migração V1 compatível com o schema existente**

Escreva `CREATE TABLE tarefa` com chave primária bigint auto incremental e colunas com os nomes/tipos derivados do mapeamento atual do Hibernate. Não adicione uma restrição NOT NULL às colunas existentes sem comprovar que isso é compatível com bancos já criados.

- [ ] **Step 3: Adicionar Compose e configuração de ambiente local**

Configure MySQL 8 com database `tarefasdb`, usuário `tarefas`, senha interpolada de `DB_PASSWORD` com fallback local `tarefas_dev_password`, porta `3306:3306`, volume nomeado e healthcheck `mysqladmin ping`. Adicione `.env` ao `.gitignore` e versione `.env.example` com os mesmos valores locais, sem credenciais reais. Para Compose, quem executa copia `.env.example` para `.env`; a aplicação local recebe `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` no terminal.

- [ ] **Step 4: Validar configuração estática e suíte sem banco**

Execute `docker compose config` quando Docker estiver disponível e `./mvnw test` sem iniciar MySQL. Confira que a propriedade `DB_URL` não contém usuário/senha e que a migração corresponde a `@Table`/`@Column` da entidade.

- [ ] **Step 5: Verificar inicialização com MySQL local se Docker estiver disponível**

Execute `docker compose up -d`, inicie a API com as variáveis do `.env.example` e confirme que Flyway aplica V1 numa base vazia. Faça uma segunda inicialização e confirme que não há alteração automática do schema. Se houver um schema legado, confirme que o baseline o reconhece e `ddl-auto=validate` inicia sem mudar os dados.

- [ ] **Step 6: Commit da Task 3**

```bash
git add pom.xml src/main/java/com/miguel/tarefas/model/Tarefa.java src/main/resources/application.properties src/main/resources/db/migration/V1__create_tarefa.sql compose.yml .gitignore .env.example
git commit -m "build(db): add Flyway migrations and local MySQL setup"
```

### Task 4: Automatizar verificações e atualizações de dependências

**Files:**
- Create: `.github/workflows/ci.yml`
- Create: `.github/dependabot.yml`

**Interfaces:**
- CI instala Java 17, ativa cache Maven e executa `./mvnw --batch-mode test` em push e pull request.

- [ ] **Step 1: Criar workflow de CI**

Crie workflow para `push` e `pull_request`, com `actions/checkout`, `actions/setup-java` usando Temurin 17 e `cache: maven`; execute `./mvnw --batch-mode test`. Não exponha segredos nem dependa de MySQL/Docker.

- [ ] **Step 2: Configurar Dependabot para Maven e GitHub Actions**

Crie `.github/dependabot.yml` com `version: 2`, diretório `/`, ecossistema `maven` e `github-actions`, ambos em atualizações semanais. Não configure limites ou regras adicionais sem necessidade.

- [ ] **Step 3: Executar os mesmos comandos do CI e revisar o diff final**

Execute `./mvnw.cmd --version`, `./mvnw.cmd test` e `docker compose config` quando disponíveis. Confira workflows e YAML, `git diff --check`, `git status` e confirme que `README.md` não foi alterado.

- [ ] **Step 4: Commit da Task 4**

```bash
git add .github/workflows/ci.yml .github/dependabot.yml
git commit -m "ci: add GitHub Actions and Dependabot"
```

## Verificação final

Execute `./mvnw.cmd --batch-mode test` no Windows e `docker compose config` quando os binários estiverem disponíveis. Confirme pelos testes MockMvc os status, validações e erros; confirme que `TarefaController` só depende de `TarefaService`; confirme migração Flyway e `ddl-auto=validate`; revise `git diff HEAD~4..HEAD` e `git status --short`. Registre qualquer comando bloqueado por falta de JDK ou Docker sem declarar sucesso não verificado.
