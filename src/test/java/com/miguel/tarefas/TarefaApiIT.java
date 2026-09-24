package com.miguel.tarefas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miguel.tarefas.dto.AtualizarTarefaRequest;
import com.miguel.tarefas.dto.CriarTarefaRequest;
import com.miguel.tarefas.dto.TarefaResponse;
import com.miguel.tarefas.repository.TarefaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TarefaApiIT {

    @Autowired
    private TestRestTemplate http;

    @Autowired
    private TarefaRepository tarefaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void flywayCriaSchemaEApiPersisteCicloDeVidaDaTarefa() throws Exception {
        Integer migrationCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM flyway_schema_history
                WHERE version = '1' AND type = 'SQL' AND success = 1
                """, Integer.class);
        assertEquals(1, migrationCount);

        tarefaRepository.deleteAll();

        ResponseEntity<TarefaResponse> created = http.postForEntity("/tarefas",
                new CriarTarefaRequest("Estudar Spring", "Miguel", LocalDate.parse("2026-10-10")),
                TarefaResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertNotNull(created.getBody());
        Long id = created.getBody().id();
        assertNotNull(id);
        assertEquals("/tarefas/" + id, created.getHeaders().getLocation().getPath());

        ResponseEntity<TarefaResponse> found = http.getForEntity("/tarefas/{id}", TarefaResponse.class, id);
        assertEquals(HttpStatus.OK, found.getStatusCode());
        assertNotNull(found.getBody());
        assertEquals("Estudar Spring", found.getBody().nome());

        ResponseEntity<TarefaResponse[]> listed = http.getForEntity("/tarefas", TarefaResponse[].class);
        assertEquals(HttpStatus.OK, listed.getStatusCode());
        assertNotNull(listed.getBody());
        assertEquals(1, listed.getBody().length);

        ResponseEntity<TarefaResponse> updated = http.exchange("/tarefas/{id}", HttpMethod.PUT,
                new HttpEntity<>(new AtualizarTarefaRequest("Revisar API", "Ana", LocalDate.parse("2026-11-05"))),
                TarefaResponse.class, id);
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertNotNull(updated.getBody());
        assertEquals(id, updated.getBody().id());
        assertEquals("Revisar API", updated.getBody().nome());
        assertEquals("Ana", updated.getBody().responsavel());

        ResponseEntity<Void> deleted = http.exchange("/tarefas/{id}", HttpMethod.DELETE,
                HttpEntity.EMPTY, Void.class, id);
        assertEquals(HttpStatus.NO_CONTENT, deleted.getStatusCode());

        ResponseEntity<String> missing = http.getForEntity("/tarefas/{id}", String.class, id);
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        JsonNode error = objectMapper.readTree(missing.getBody());
        assertEquals(404, error.path("status").asInt());
        assertTrue(error.path("detail").isTextual());

        ResponseEntity<String> openApi = http.getForEntity("/v3/api-docs", String.class);
        assertEquals(HttpStatus.OK, openApi.getStatusCode());
        assertTrue(objectMapper.readTree(openApi.getBody()).path("paths").has("/tarefas"));
    }
}
