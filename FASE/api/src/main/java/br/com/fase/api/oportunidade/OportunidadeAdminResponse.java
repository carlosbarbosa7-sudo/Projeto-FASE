package br.com.fase.api.oportunidade;

public record OportunidadeAdminResponse(Long id, String status, OportunidadeResponse oportunidade) {
    public static OportunidadeAdminResponse de(Oportunidade item) {
        return new OportunidadeAdminResponse(item.getId(), item.getStatus(), OportunidadeResponse.de(item));
    }
}
