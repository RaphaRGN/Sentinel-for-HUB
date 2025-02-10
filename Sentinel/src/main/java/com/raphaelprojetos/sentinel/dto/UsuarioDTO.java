package com.raphaelprojetos.sentinel.dto;


public class UsuarioDTO {

    private Long id;
    private String nome;
    private String senha;
    private boolean admin;


    public UsuarioDTO(){

    }

    public UsuarioDTO(Long id, String nome, String senha, boolean admin){
            this.id = id;
            this.nome = nome;
            this.senha = senha;
            this.admin = admin;

    }

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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}


