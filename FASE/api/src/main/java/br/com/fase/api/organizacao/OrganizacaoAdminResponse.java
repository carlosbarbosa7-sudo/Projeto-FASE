package br.com.fase.api.organizacao;

import br.com.fase.api.oportunidade.Organizacao;
import java.time.LocalDateTime;

public record OrganizacaoAdminResponse(
        Long id, String razaoSocial, String cnpj, String tipo,
        String responsavel, String email, String telefone, String site,
        String cidade, String uf, String descricao, String status,
        LocalDateTime analisadaEm, String motivoDecisao) {

    public static OrganizacaoAdminResponse de(Organizacao organizacao) {
        return new OrganizacaoAdminResponse(
                organizacao.getId(), organizacao.getRazaoSocial(), organizacao.getCnpj(),
                organizacao.getTipo(), organizacao.getResponsavelSolicitante(),
                organizacao.getEmailSolicitante(), organizacao.getTelefone(),
                organizacao.getSite(), organizacao.getCidade(), organizacao.getUf(),
                organizacao.getDescricao(), organizacao.getStatusAprovacao(),
                organizacao.getAnalisadaEm(), organizacao.getMotivoDecisao());
    }
}
