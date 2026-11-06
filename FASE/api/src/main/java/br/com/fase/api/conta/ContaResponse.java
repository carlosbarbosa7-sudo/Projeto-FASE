package br.com.fase.api.conta;

import java.time.LocalDate;

public record ContaResponse(
        String nome, String email, String perfil, String organizacao,
        LocalDate dataNascimento, String telefone, String escolaridade,
        String cidade, String bairro, String uf, String statusOrganizacao) {
}
