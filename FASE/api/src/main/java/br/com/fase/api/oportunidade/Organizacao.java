package br.com.fase.api.oportunidade;

import br.com.fase.api.conta.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizacao")
public class Organizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_organizacao")
    private Long id;

    @Column(name = "razao_social", nullable = false)
    private String razaoSocial;

    @Column(name = "cnpj", nullable = false, length = 14)
    private String cnpj;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    @Column(name = "responsavel_solicitante", nullable = false)
    private String responsavelSolicitante;

    @Column(name = "email_solicitante", nullable = false)
    private String emailSolicitante;

    @Column(name = "telefone", nullable = false)
    private String telefone;

    @Column(name = "site", length = 2048)
    private String site;

    @Column(name = "cidade", nullable = false)
    private String cidade;

    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Column(name = "descricao", length = 1000)
    private String descricao;

    @Column(name = "status_aprovacao", nullable = false)
    private String statusAprovacao;

    @Column(name = "verificada", nullable = false)
    private Boolean verificada;

    @Column(name = "analisada_em")
    private LocalDateTime analisadaEm;

    @Column(name = "motivo_decisao", length = 500)
    private String motivoDecisao;

    @Column(name = "id_usuario")
    private Long usuarioId;

    protected Organizacao() {
    }

    public static Organizacao criarSolicitacao(
            String razaoSocial, String cnpj, String tipo, String responsavel,
            String email, String telefone, String site, String cidade, String uf,
            String descricao) {
        Organizacao organizacao = new Organizacao();
        organizacao.razaoSocial = razaoSocial;
        organizacao.cnpj = cnpj;
        organizacao.tipo = tipo;
        organizacao.responsavelSolicitante = responsavel;
        organizacao.emailSolicitante = email;
        organizacao.telefone = telefone;
        organizacao.site = site;
        organizacao.cidade = cidade;
        organizacao.uf = uf;
        organizacao.descricao = descricao;
        organizacao.statusAprovacao = "PENDENTE";
        organizacao.verificada = false;
        return organizacao;
    }

    public void decidir(String status, String motivo) {
        this.statusAprovacao = status;
        this.motivoDecisao = motivo;
        this.analisadaEm = LocalDateTime.now();
        this.verificada = "APROVADA".equals(status);
    }

    public void vincularUsuario(Usuario usuario) {
        this.usuarioId = usuario.getId();
    }

    public Long getId() { return id; }
    public String getRazaoSocial() { return razaoSocial; }
    public String getCnpj() { return cnpj; }
    public String getTipo() { return tipo; }
    public String getResponsavelSolicitante() { return responsavelSolicitante; }
    public String getEmailSolicitante() { return emailSolicitante; }
    public String getTelefone() { return telefone; }
    public String getSite() { return site; }
    public String getCidade() { return cidade; }
    public String getUf() { return uf; }
    public String getDescricao() { return descricao; }
    public String getStatusAprovacao() { return statusAprovacao; }
    public LocalDateTime getAnalisadaEm() { return analisadaEm; }
    public String getMotivoDecisao() { return motivoDecisao; }
    public Long getUsuarioId() { return usuarioId; }
}
