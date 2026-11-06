package br.com.fase.api.oportunidade;

import jakarta.validation.constraints.NotNull;

public record RevisaoOportunidadeRequest(
        @NotNull EstadoRevisaoOportunidade status) {
}
