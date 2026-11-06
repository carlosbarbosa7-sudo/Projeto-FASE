package br.com.fase.api.admin;

import br.com.fase.api.oportunidade.Localidade;

public record LocalidadeAdminResponse(Long id, String cidade, String uf) {
    public static LocalidadeAdminResponse de(Localidade localidade) {
        return new LocalidadeAdminResponse(localidade.getId(), localidade.getCidade(), localidade.getUf());
    }
}
