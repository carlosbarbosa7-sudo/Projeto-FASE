package br.com.fase.api.oportunidade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "oportunidade")
public class Oportunidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_oportunidade")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoOportunidade tipo;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descricao", nullable = false)
    private String descricao;

    @Column(name = "requisitos")
    private String requisitos;

    @Column(name = "modalidade", nullable = false)
    private String modalidade;

    @Column(name = "tipo_vaga")
    private String tipoVaga;

    @Column(name = "remuneracao", precision = 10, scale = 2)
    private BigDecimal remuneracao;

    @Column(name = "carga_horaria")
    private Integer cargaHoraria;

    @Column(name = "gratuito")
    private Boolean gratuito;

    @Column(name = "valor_curso", precision = 10, scale = 2)
    private BigDecimal valorCurso;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(name = "link_externo", nullable = false, length = 2048)
    private String linkExterno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_organizacao", nullable = false)
    private Organizacao organizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_localidade")
    private Localidade localidade;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "data_revisao")
    private LocalDateTime dataRevisao;

    protected Oportunidade() {
    }

    public static Oportunidade criarPendente(
            Organizacao organizacao, Categoria categoria, Localidade localidade,
            TipoOportunidade tipo, String titulo, String descricao, String requisitos,
            String modalidade, String tipoVaga, BigDecimal remuneracao,
            Integer cargaHoraria, Boolean gratuito, BigDecimal valorCurso,
            LocalDate dataInicio, LocalDate dataFim, String linkExterno) {
        Oportunidade oportunidade = new Oportunidade();
        oportunidade.organizacao = organizacao;
        oportunidade.categoria = categoria;
        oportunidade.localidade = localidade;
        oportunidade.tipo = tipo;
        oportunidade.titulo = titulo;
        oportunidade.descricao = descricao;
        oportunidade.requisitos = requisitos;
        oportunidade.modalidade = modalidade;
        oportunidade.tipoVaga = tipoVaga;
        oportunidade.remuneracao = remuneracao;
        oportunidade.cargaHoraria = cargaHoraria;
        oportunidade.gratuito = gratuito;
        oportunidade.valorCurso = valorCurso;
        oportunidade.dataInicio = dataInicio;
        oportunidade.dataFim = dataFim;
        oportunidade.linkExterno = linkExterno;
        oportunidade.status = "PENDENTE_REVISAO";
        return oportunidade;
    }

    public void decidirRevisao(String status) {
        this.status = status;
        this.dataRevisao = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
    public TipoOportunidade getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getRequisitos() { return requisitos; }
    public String getModalidade() { return modalidade; }
    public String getTipoVaga() { return tipoVaga; }
    public BigDecimal getRemuneracao() { return remuneracao; }
    public Integer getCargaHoraria() { return cargaHoraria; }
    public Boolean getGratuito() { return gratuito; }
    public BigDecimal getValorCurso() { return valorCurso; }
    public LocalDate getDataInicio() { return dataInicio; }
    public LocalDate getDataFim() { return dataFim; }
    public String getLinkExterno() { return linkExterno; }
    public Categoria getCategoria() { return categoria; }
    public Organizacao getOrganizacao() { return organizacao; }
    public Localidade getLocalidade() { return localidade; }
}
