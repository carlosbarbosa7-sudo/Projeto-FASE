package br.com.fase.api.conta;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CadastroJovemRequest(
        @NotBlank @Size(max = 160) String nome,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 12, max = 128) String senha,
        @NotNull @Past LocalDate dataNascimento,
        @Size(max = 30) String telefone,
        @Size(max = 120) String escolaridade,
        @Size(max = 120) String cidade,
        @Size(max = 120) String bairro,
        @Size(min = 2, max = 2) String uf) {
}
