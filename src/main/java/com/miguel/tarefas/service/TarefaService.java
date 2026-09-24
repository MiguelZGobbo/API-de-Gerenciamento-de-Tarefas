package com.miguel.tarefas.service;

import com.miguel.tarefas.dto.AtualizarTarefaRequest;
import com.miguel.tarefas.dto.CriarTarefaRequest;
import com.miguel.tarefas.dto.TarefaResponse;
import com.miguel.tarefas.exception.TarefaNaoEncontradaException;
import com.miguel.tarefas.model.Tarefa;
import com.miguel.tarefas.repository.TarefaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaService(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    public TarefaResponse criar(CriarTarefaRequest request) {
        Tarefa tarefa = new Tarefa();
        tarefa.setNome(request.nome());
        tarefa.setResponsavel(request.responsavel());
        tarefa.setDataEntrega(request.dataEntrega());
        return paraResposta(tarefaRepository.save(tarefa));
    }

    @Transactional(readOnly = true)
    public List<TarefaResponse> listarTodos() {
        return tarefaRepository.findAll().stream().map(this::paraResposta).toList();
    }

    @Transactional(readOnly = true)
    public TarefaResponse buscarPorId(Long id) {
        return paraResposta(buscarEntidade(id));
    }

    public TarefaResponse atualizar(Long id, AtualizarTarefaRequest request) {
        Tarefa tarefa = buscarEntidade(id);
        tarefa.setNome(request.nome());
        tarefa.setResponsavel(request.responsavel());
        tarefa.setDataEntrega(request.dataEntrega());
        return paraResposta(tarefaRepository.save(tarefa));
    }

    public void deletar(Long id) {
        buscarEntidade(id);
        tarefaRepository.deleteById(id);
    }

    private Tarefa buscarEntidade(Long id) {
        return tarefaRepository.findById(id).orElseThrow(() -> new TarefaNaoEncontradaException(id));
    }

    private TarefaResponse paraResposta(Tarefa tarefa) {
        return new TarefaResponse(tarefa.getId(), tarefa.getNome(), tarefa.getResponsavel(), tarefa.getDataEntrega());
    }
}
