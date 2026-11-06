package br.com.fase.api.organizacao;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrganizacaoSolicitacaoRequest(
        @NotBlank @Size(max = 200) String razaoSocial,
        @NotBlank String cnpj,
        @NotNull TipoOrganizacao tipo,
        @NotBlank @Size(max = 160) String responsavel,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(max = 30) String telefone,
        @Size(max = 2048) String site,
        @NotBlank @Size(max = 120) String cidade,
        @NotBlank String uf,
        @NotBlank @Size(max = 120) String atuacao,
        @AssertTrue(message = "Confirme que pode representar a organização.") boolean confirmacao) {
}
