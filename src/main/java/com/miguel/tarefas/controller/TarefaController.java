package com.miguel.tarefas.controller;

import com.miguel.tarefas.dto.AtualizarTarefaRequest;
import com.miguel.tarefas.dto.CriarTarefaRequest;
import com.miguel.tarefas.dto.TarefaResponse;
import com.miguel.tarefas.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tarefas")
@Validated
@Tag(name = "Tarefas", description = "Operações de gerenciamento de tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @PostMapping
    @Operation(summary = "Criar uma tarefa")
    public ResponseEntity<TarefaResponse> criar(@Valid @RequestBody CriarTarefaRequest request) {
        TarefaResponse response = tarefaService.criar(request);
        return ResponseEntity.created(URI.create("/tarefas/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar tarefas")
    public List<TarefaResponse> listar() {
        return tarefaService.listarTodos();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tarefa por ID")
    public TarefaResponse buscarPorId(@PathVariable @Positive Long id) {
        return tarefaService.buscarPorId(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma tarefa")
    public TarefaResponse atualizar(@PathVariable @Positive Long id,
                                    @Valid @RequestBody AtualizarTarefaRequest request) {
        return tarefaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma tarefa")
    public ResponseEntity<Void> deletar(@PathVariable @Positive Long id) {
        tarefaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
