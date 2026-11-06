package br.com.fase.api.oportunidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "localidade")
public class Localidade {

    @Id
    @Column(name = "id_localidade")
    private Long id;

    @Column(name = "cidade", nullable = false)
    private String cidade;

    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    protected Localidade() {
    }

    public String getCidade() { return cidade; }
    public String getUf() { return uf; }
    public Long getId() { return id; }
}
