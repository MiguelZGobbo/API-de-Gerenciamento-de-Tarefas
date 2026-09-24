package com.miguel.tarefas.dto;

import java.time.LocalDate;

public record TarefaResponse(Long id, String nome, String responsavel, LocalDate dataEntrega) {
}
