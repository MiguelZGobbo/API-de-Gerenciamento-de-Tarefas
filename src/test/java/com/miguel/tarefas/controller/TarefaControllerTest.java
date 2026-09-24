package com.miguel.tarefas.controller;

import com.miguel.tarefas.dto.TarefaResponse;
import com.miguel.tarefas.exception.TarefaNaoEncontradaException;
import com.miguel.tarefas.service.TarefaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.web.servlet.MockMvc;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TarefaController.class)
@Import({SpringDocConfiguration.class, SpringDocWebMvcConfiguration.class})
@EnableConfigurationProperties(SpringDocConfigProperties.class)
class TarefaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TarefaService tarefaService;

    @Test
    void deveCriarTarefaERetornar201ComLocation() throws Exception {
        when(tarefaService.criar(any())).thenReturn(novaResposta(1L));

        mockMvc.perform(post("/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Estudar Spring","responsavel":"Miguel","dataEntrega":"2026-10-10"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/tarefas/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Estudar Spring"));
    }

    @Test
    void deveListarTarefasERetornar200() throws Exception {
        when(tarefaService.listarTodos()).thenReturn(List.of(novaResposta(1L)));

        mockMvc.perform(get("/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Estudar Spring"));
    }

    @Test
    void deveBuscarTarefaERetornar200() throws Exception {
        when(tarefaService.buscarPorId(1L)).thenReturn(novaResposta(1L));

        mockMvc.perform(get("/tarefas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dataEntrega").value("2026-10-10"));
    }

    @Test
    void deveAtualizarTarefaERetornar200() throws Exception {
        when(tarefaService.atualizar(org.mockito.ArgumentMatchers.eq(1L), any()))
                .thenReturn(new TarefaResponse(1L, "Revisar API", "Ana", LocalDate.parse("2026-11-05")));

        mockMvc.perform(put("/tarefas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Revisar API","responsavel":"Ana","dataEntrega":"2026-11-05"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Revisar API"))
                .andExpect(jsonPath("$.responsavel").value("Ana"));
    }

    @Test
    void deveExcluirTarefaERetornar204SemCorpo() throws Exception {
        mockMvc.perform(delete("/tarefas/1"))
                .andExpect(status().isNoContent())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(""));

        verify(tarefaService).deletar(1L);
    }

    @Test
    void deveRetornar404AoBuscarTarefaAusente() throws Exception {
        when(tarefaService.buscarPorId(404L)).thenThrow(new TarefaNaoEncontradaException(404L));

        mockMvc.perform(get("/tarefas/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void deveRetornar404AoAtualizarTarefaAusente() throws Exception {
        when(tarefaService.atualizar(org.mockito.ArgumentMatchers.eq(404L), any()))
                .thenThrow(new TarefaNaoEncontradaException(404L));

        mockMvc.perform(put("/tarefas/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Revisar API","responsavel":"Ana","dataEntrega":"2026-11-05"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void deveRetornar404AoExcluirTarefaAusente() throws Exception {
        org.mockito.Mockito.doThrow(new TarefaNaoEncontradaException(404L))
                .when(tarefaService).deletar(404L);

        mockMvc.perform(delete("/tarefas/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @ParameterizedTest
    @MethodSource("payloadsInvalidos")
    void deveRejeitarCorpoInvalido(String corpo) throws Exception {
        mockMvc.perform(post("/tarefas").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "abc"})
    void deveRejeitarIdInvalidoSemConsultarService(String id) throws Exception {
        mockMvc.perform(get("/tarefas/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").isNotEmpty());

        verifyNoInteractions(tarefaService);
    }

    @Test
    void devePublicarDocumentacaoOpenApiDasTarefas() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").isNotEmpty())
                .andExpect(jsonPath("$.paths['/tarefas']").exists());
    }

    private static Stream<String> payloadsInvalidos() {
        String nomeLongo = "n".repeat(256);
        String responsavelLongo = "r".repeat(256);

        return Stream.of(
                """
                {"nome":" ","responsavel":"Miguel","dataEntrega":"2026-10-10"}
                """,
                """
                {"nome":"Estudar","responsavel":" ","dataEntrega":"2026-10-10"}
                """,
                "{\"nome\":\"" + nomeLongo + "\",\"responsavel\":\"Miguel\",\"dataEntrega\":\"2026-10-10\"}",
                "{\"nome\":\"Estudar\",\"responsavel\":\"" + responsavelLongo + "\",\"dataEntrega\":\"2026-10-10\"}",
                """
                {"responsavel":"Miguel","dataEntrega":"2026-10-10"}
                """,
                """
                {"nome":null,"responsavel":"Miguel","dataEntrega":"2026-10-10"}
                """,
                """
                {"nome":"Estudar","dataEntrega":"2026-10-10"}
                """,
                """
                {"nome":"Estudar","responsavel":null,"dataEntrega":"2026-10-10"}
                """,
                """
                {"nome":"Estudar","responsavel":"Miguel"}
                """,
                """
                {"nome":"Estudar","responsavel":"Miguel","dataEntrega":null}
                """,
                """
                {"nome":"Estudar","responsavel":"Miguel","dataEntrega":"2026-02-30"}
                """,
                "nao-e-json"
        );
    }

    private static TarefaResponse novaResposta(Long id) {
        return new TarefaResponse(id, "Estudar Spring", "Miguel", LocalDate.parse("2026-10-10"));
    }
}
