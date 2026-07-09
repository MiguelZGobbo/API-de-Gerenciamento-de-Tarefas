# 📋 API de Gerenciamento de Tarefas

API REST para gerenciamento de tarefas desenvolvida com **Java 17**, **Spring Boot 3**, **Spring Data JPA** e **MySQL**. Projeto acadêmico com CRUD completo de tarefas.

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1.5-6DB33F?logo=spring)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![Maven](https://img.shields.io/badge/Maven-3-C71A36?logo=apache-maven)

---

## 📌 Endpoints

| Método | Rota                  | Descrição             |
|--------|-----------------------|-----------------------|
| POST   | `/tarefas`            | Criar nova tarefa     |
| GET    | `/tarefas`            | Listar todas tarefas  |
| GET    | `/tarefas/{id}`       | Buscar tarefa por ID  |
| PUT    | `/tarefas/{id}`       | Atualizar tarefa      |
| DELETE | `/tarefas/{id}`       | Deletar tarefa        |

### Exemplos

**Criar tarefa**
```bash
curl -X POST http://localhost:8080/tarefas \
  -H "Content-Type: application/json" \
  -d '{"nome": "Estudar Spring", "responsavel": "Miguel", "dataEntrega": "2025-12-12"}'
```
```json
{
  "id": 1,
  "nome": "Estudar Spring",
  "dataEntrega": "2025-12-12",
  "responsavel": "Miguel"
}
```

**Listar tarefas**
```bash
curl http://localhost:8080/tarefas
```

**Atualizar tarefa**
```bash
curl -X PUT http://localhost:8080/tarefas/1 \
  -H "Content-Type: application/json" \
  -d '{"nome": "Estudar Spring Boot", "responsavel": "Miguel", "dataEntrega": "2025-12-20"}'
```

**Deletar tarefa**
```bash
curl -X DELETE http://localhost:8080/tarefas/1
```

---

## 🧱 Modelo

```json
{
  "id": 1,
  "nome": "Desenvolvimento da API",
  "dataEntrega": "2025-12-12",
  "responsavel": "Miguel"
}
```

---

## 🚀 Como executar

### Pré-requisitos

- Java 17+
- Maven 3+
- MySQL 8.0+

### Passos

1. **Clone o repositório**
   ```bash
   git clone https://github.com/MiguelZGobbo/trabalho-api-spring.git
   cd trabalho-api-spring
   ```

2. **Configure o banco de dados**

   Crie um banco MySQL chamado `tarefasdb` e ajuste as credenciais em `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/tarefasdb?useSSL=false&allowPublicKeyRetrieval=true
   spring.datasource.username=root
   spring.datasource.password=sua-senha
   ```

3. **Execute a aplicação**
   ```bash
   ./mvnw spring-boot:run
   ```

   A API estará disponível em `http://localhost:8080`.

---

## 🧪 Testes

```bash
./mvnw test
```

---

## 📬 Postman

A collection do Postman está incluída no repositório: [`API Tarefas.postman_collection.json`](./API%20Tarefas.postman_collection.json). Importe no Postman para testar todos os endpoints.

---

## 🛠️ Tecnologias

- **Spring Boot 3.1.5** — Web, Data JPA, Test
- **Java 17**
- **MySQL 8.0**
- **Maven**

---

## 👤 Autor

**Miguel Zago Gobbo**

[![GitHub](https://img.shields.io/badge/GitHub-MiguelZGobbo-181717?logo=github)](https://github.com/MiguelZGobbo)
