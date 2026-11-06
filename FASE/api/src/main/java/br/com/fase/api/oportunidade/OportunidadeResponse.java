package br.com.fase.api.oportunidade;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OportunidadeResponse(
        Long id,
        TipoOportunidade tipo,
        String titulo,
        String descricao,
        String requisitos,
        String modalidade,
        String tipoVaga,
        BigDecimal remuneracao,
        Integer cargaHoraria,
        Boolean gratuito,
        BigDecimal valorCurso,
        LocalDate dataInicio,
        LocalDate dataFim,
        String linkExterno,
        String instituicao,
        String categoria,
        String cidade,
        String uf) {

    public static OportunidadeResponse de(Oportunidade oportunidade) {
        Localidade localidade = oportunidade.getLocalidade();
        return new OportunidadeResponse(
                oportunidade.getId(),
                oportunidade.getTipo(),
                oportunidade.getTitulo(),
                oportunidade.getDescricao(),
                oportunidade.getRequisitos(),
                oportunidade.getModalidade(),
                oportunidade.getTipoVaga(),
                oportunidade.getRemuneracao(),
                oportunidade.getCargaHoraria(),
                oportunidade.getGratuito(),
                oportunidade.getValorCurso(),
                oportunidade.getDataInicio(),
                oportunidade.getDataFim(),
                oportunidade.getLinkExterno(),
                oportunidade.getOrganizacao().getRazaoSocial(),
                oportunidade.getCategoria().getNome(),
                localidade == null ? null : localidade.getCidade(),
                localidade == null ? null : localidade.getUf());
    }
}
