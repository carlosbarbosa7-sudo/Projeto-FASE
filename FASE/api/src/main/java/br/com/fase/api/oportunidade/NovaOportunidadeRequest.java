package br.com.fase.api.oportunidade;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record NovaOportunidadeRequest(
        @NotNull @Positive Long idOrganizacao,
        @NotNull @Positive Long idCategoria,
        Long idLocalidade,
        @NotNull TipoOportunidade tipo,
        @NotBlank @Size(max = 200) String titulo,
        @NotBlank String descricao,
        String requisitos,
        @NotBlank String modalidade,
        String tipoVaga,
        @DecimalMin("0.00") BigDecimal remuneracao,
        @Positive Integer cargaHoraria,
        Boolean gratuito,
        @DecimalMin("0.00") BigDecimal valorCurso,
        LocalDate dataInicio,
        LocalDate dataFim,
        @NotBlank @Size(max = 2048) String linkExterno) {
}
