package br.com.fase.api.conta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "oportunidade_salva")
@IdClass(OportunidadeSalvaId.class)
public class OportunidadeSalva {

    @Id
    @Column(name = "id_usuario")
    private Long usuarioId;

    @Id
    @Column(name = "id_oportunidade")
    private Long oportunidadeId;

    @Column(name = "data_salvamento", insertable = false, updatable = false)
    private LocalDateTime dataSalvamento;

    protected OportunidadeSalva() {
    }

    public static OportunidadeSalva criar(Long usuarioId, Long oportunidadeId) {
        OportunidadeSalva saved = new OportunidadeSalva();
        saved.usuarioId = usuarioId;
        saved.oportunidadeId = oportunidadeId;
        return saved;
    }

    public Long getUsuarioId() { return usuarioId; }
    public Long getOportunidadeId() { return oportunidadeId; }
}
