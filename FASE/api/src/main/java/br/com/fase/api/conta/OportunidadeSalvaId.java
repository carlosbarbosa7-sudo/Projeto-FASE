package br.com.fase.api.conta;

import java.io.Serializable;
import java.util.Objects;

public class OportunidadeSalvaId implements Serializable {

    private Long usuarioId;
    private Long oportunidadeId;

    public OportunidadeSalvaId() {
    }

    public OportunidadeSalvaId(Long usuarioId, Long oportunidadeId) {
        this.usuarioId = usuarioId;
        this.oportunidadeId = oportunidadeId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof OportunidadeSalvaId that)) return false;
        return Objects.equals(usuarioId, that.usuarioId)
                && Objects.equals(oportunidadeId, that.oportunidadeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuarioId, oportunidadeId);
    }
}
