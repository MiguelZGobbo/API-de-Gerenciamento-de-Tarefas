package com.miguel.tarefas.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tarefa")
public class Tarefa {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", length = 255)
    private String nome;

    @Column(name = "data_entrega")
    private LocalDate dataEntrega;

    @Column(name = "responsavel", length = 255)
    private String responsavel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataEntrega() {
        return dataEntrega;
    }

    public void setDataEntrega(LocalDate dataEntrega) {
        this.dataEntrega = dataEntrega;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }
}

