package com.miguel.tarefas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CriarTarefaRequest(
        @NotBlank @Size(max = 255) String nome,
        @NotBlank @Size(max = 255) String responsavel,
        @NotNull LocalDate dataEntrega
) {
}
