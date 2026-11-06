package br.com.fase.api.organizacao;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RevisaoOrganizacaoRequest(
        @NotNull EstadoRevisaoOrganizacao status,
        @Size(max = 500) String motivo,
        @Size(min = 12, max = 128) String senhaProvisoria) {
}
