package br.com.fase.api.seguranca;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(
        @NotBlank String usuario,
        @NotBlank String senha) {
}
