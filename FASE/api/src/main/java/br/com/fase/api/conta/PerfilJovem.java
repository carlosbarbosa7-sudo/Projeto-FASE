package br.com.fase.api.conta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "perfil_jovem")
public class PerfilJovem {

    @Id
    @Column(name = "id_usuario")
    private Long usuarioId;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "telefone", length = 30)
    private String telefone;

    @Column(name = "escolaridade", length = 120)
    private String escolaridade;

    @Column(name = "cidade", length = 120)
    private String cidade;

    @Column(name = "bairro", length = 120)
    private String bairro;

    @Column(name = "uf", length = 2)
    private String uf;

    protected PerfilJovem() {
    }

    public static PerfilJovem criar(Long usuarioId, LocalDate dataNascimento,
            String telefone, String escolaridade, String cidade, String bairro, String uf) {
        PerfilJovem perfil = new PerfilJovem();
        perfil.usuarioId = usuarioId;
        perfil.dataNascimento = dataNascimento;
        perfil.telefone = telefone;
        perfil.escolaridade = escolaridade;
        perfil.cidade = cidade;
        perfil.bairro = bairro;
        perfil.uf = uf;
        return perfil;
    }

    public Long getUsuarioId() { return usuarioId; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getTelefone() { return telefone; }
    public String getEscolaridade() { return escolaridade; }
    public String getCidade() { return cidade; }
    public String getBairro() { return bairro; }
    public String getUf() { return uf; }
}
