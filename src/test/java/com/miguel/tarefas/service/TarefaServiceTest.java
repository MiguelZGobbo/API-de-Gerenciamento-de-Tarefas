package com.miguel.tarefas.service;

import com.miguel.tarefas.dto.AtualizarTarefaRequest;
import com.miguel.tarefas.dto.CriarTarefaRequest;
import com.miguel.tarefas.dto.TarefaResponse;
import com.miguel.tarefas.exception.TarefaNaoEncontradaException;
import com.miguel.tarefas.model.Tarefa;
import com.miguel.tarefas.repository.TarefaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @InjectMocks
    private TarefaService tarefaService;

    @Test
    void deveCriarTarefaEMapearResposta() {
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> {
            Tarefa tarefa = invocation.getArgument(0);
            tarefa.setId(1L);
            return tarefa;
        });

        TarefaResponse response = tarefaService.criar(
                new CriarTarefaRequest("Estudar Spring", "Miguel", LocalDate.parse("2026-10-10")));

        assertEquals(new TarefaResponse(1L, "Estudar Spring", "Miguel", LocalDate.parse("2026-10-10")), response);
    }

    @Test
    void deveListarTarefasEMapearCampos() {
        when(tarefaRepository.findAll()).thenReturn(List.of(novaTarefa(1L)));

        assertEquals(List.of(new TarefaResponse(1L, "Estudar Spring", "Miguel",
                LocalDate.parse("2026-10-10"))), tarefaService.listarTodos());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaTarefas() {
        when(tarefaRepository.findAll()).thenReturn(List.of());

        assertEquals(List.of(), tarefaService.listarTodos());
    }

    @Test
    void deveBuscarTarefaExistente() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(novaTarefa(1L)));

        assertEquals(new TarefaResponse(1L, "Estudar Spring", "Miguel", LocalDate.parse("2026-10-10")),
                tarefaService.buscarPorId(1L));
    }

    @Test
    void deveAtualizarCamposSemAlterarIdPersistido() {
        Tarefa persisted = novaTarefa(1L);
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(persisted));
        when(tarefaRepository.save(persisted)).thenReturn(persisted);

        TarefaResponse response = tarefaService.atualizar(1L,
                new AtualizarTarefaRequest("Revisar API", "Ana", LocalDate.parse("2026-11-05")));

        assertEquals(new TarefaResponse(1L, "Revisar API", "Ana", LocalDate.parse("2026-11-05")), response);
        verify(tarefaRepository).save(persisted);
    }

    @Test
    void deveExcluirTarefaExistente() {
        when(tarefaRepository.findById(1L)).thenReturn(Optional.of(novaTarefa(1L)));

        tarefaService.deletar(1L);

        verify(tarefaRepository).deleteById(1L);
    }

    @Test
    void deveLancarExcecaoAoBuscarIdAusente() {
        when(tarefaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(TarefaNaoEncontradaException.class, () -> tarefaService.buscarPorId(404L));
    }

    @Test
    void deveLancarExcecaoAoAtualizarIdAusenteSemSalvar() {
        when(tarefaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(TarefaNaoEncontradaException.class, () -> tarefaService.atualizar(404L,
                new AtualizarTarefaRequest("Revisar API", "Ana", LocalDate.parse("2026-11-05"))));
        verify(tarefaRepository, never()).save(any(Tarefa.class));
    }

    @Test
    void deveLancarExcecaoAoExcluirIdAusenteSemExcluir() {
        when(tarefaRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(TarefaNaoEncontradaException.class, () -> tarefaService.deletar(404L));
        verify(tarefaRepository, never()).deleteById(404L);
    }

    private static Tarefa novaTarefa(Long id) {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(id);
        tarefa.setNome("Estudar Spring");
        tarefa.setResponsavel("Miguel");
        tarefa.setDataEntrega(LocalDate.parse("2026-10-10"));
        return tarefa;
    }
}
