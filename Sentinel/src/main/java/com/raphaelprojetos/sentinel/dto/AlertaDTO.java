package com.raphaelprojetos.sentinel.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlertaDTO {

    private Long id;
    private String codigo;
    private String titulo;
    public LocalDateTime tempoFormatado;
    public String descricao;

    public AlertaDTO(){

    }


    public AlertaDTO(String codigo, String titulo, LocalDateTime tempo, String descricao ){


        this.codigo = codigo;
        this.titulo = titulo;
        this.tempoFormatado = tempo;
        this.descricao = descricao;

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTempoFormatado() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
        return this.tempoFormatado.format(formatter);
    }

    public void setTempoFormatado(String tempoFormatado) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH-mm-ss");
        this.tempoFormatado = LocalDateTime.parse(tempoFormatado, formatter);

    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }


}
