package br.com.fase.api.admin;

import br.com.fase.api.oportunidade.Categoria;
import br.com.fase.api.oportunidade.CategoriaRepository;
import br.com.fase.api.oportunidade.EstadoRevisaoOportunidade;
import br.com.fase.api.oportunidade.Localidade;
import br.com.fase.api.oportunidade.LocalidadeRepository;
import br.com.fase.api.oportunidade.NovaOportunidadeRequest;
import br.com.fase.api.oportunidade.Organizacao;
import br.com.fase.api.oportunidade.Oportunidade;
import br.com.fase.api.oportunidade.OportunidadeAdminResponse;
import br.com.fase.api.oportunidade.OportunidadeRepository;
import br.com.fase.api.oportunidade.TipoOportunidade;
import br.com.fase.api.conta.Usuario;
import br.com.fase.api.conta.UsuarioRepository;
import br.com.fase.api.organizacao.EstadoRevisaoOrganizacao;
import br.com.fase.api.organizacao.OrganizacaoAdminResponse;
import br.com.fase.api.organizacao.OrganizacaoRepository;
import br.com.fase.api.seguranca.SenhaService;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {

    private static final List<String> MODALIDADES = List.of("PRESENCIAL", "HIBRIDO", "REMOTO", "ONLINE");
    private static final List<String> TIPOS_VAGA = List.of("EMPREGO", "ESTAGIO", "APRENDIZ");

    private final OrganizacaoRepository organizacoes;
    private final CategoriaRepository categorias;
    private final LocalidadeRepository localidades;
    private final OportunidadeRepository oportunidades;
    private final UsuarioRepository usuarios;
    private final SenhaService senhas;

    public AdminService(OrganizacaoRepository organizacoes, CategoriaRepository categorias,
            LocalidadeRepository localidades, OportunidadeRepository oportunidades,
            UsuarioRepository usuarios, SenhaService senhas) {
        this.organizacoes = organizacoes;
        this.categorias = categorias;
        this.localidades = localidades;
        this.oportunidades = oportunidades;
        this.usuarios = usuarios;
        this.senhas = senhas;
    }

    @Transactional(readOnly = true)
    public List<OrganizacaoAdminResponse> organizacoesPendentes() {
        return organizacoesPorStatus("PENDENTE");
    }

    @Transactional(readOnly = true)
    public List<OrganizacaoAdminResponse> organizacoesPorStatus(String status) {
        return organizacoes.findByStatusAprovacaoOrderByIdAsc(status)
                .stream().map(OrganizacaoAdminResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoriaAdminResponse> categorias(TipoOportunidade tipo) {
        return categorias.findByTipoOrderByNomeAsc(tipo.name())
                .stream().map(CategoriaAdminResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<LocalidadeAdminResponse> localidades() {
        return localidades.findAllByOrderByCidadeAscUfAsc()
                .stream().map(LocalidadeAdminResponse::de).toList();
    }

    @Transactional
    public OrganizacaoAdminResponse revisarOrganizacao(
            Long id, EstadoRevisaoOrganizacao estado, String motivo, String senhaProvisoria) {
        Organizacao organizacao = organizacoes.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solicitação não encontrada."));
        if (!"PENDENTE".equals(organizacao.getStatusAprovacao())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta solicitação já foi analisada.");
        }
        if (estado == EstadoRevisaoOrganizacao.APROVADA) {
            if (senhaProvisoria == null || senhaProvisoria.length() < 12 || senhaProvisoria.length() > 128) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "A aprovação exige uma senha provisória com pelo menos 12 caracteres.");
            }
            if (usuarios.existsByEmailIgnoreCase(organizacao.getEmailSolicitante())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "O e-mail da instituição já está vinculado a outra conta.");
            }
            Usuario usuario = usuarios.save(Usuario.criar(
                    organizacao.getRazaoSocial(), organizacao.getEmailSolicitante(),
                    senhas.hash(senhaProvisoria), "INSTITUICAO"));
            organizacao.vincularUsuario(usuario);
        }
        organizacao.decidir(estado.name(), motivo == null || motivo.isBlank() ? null : motivo.trim());
        return OrganizacaoAdminResponse.de(organizacoes.save(organizacao));
    }

    @Transactional
    public OportunidadeAdminResponse criarOportunidade(NovaOportunidadeRequest request) {
        Organizacao organizacao = organizacoes.findByIdAndStatusAprovacao(request.idOrganizacao(), "APROVADA")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "A oportunidade só pode ser vinculada a uma organização aprovada."));
        Categoria categoria = categorias.findById(request.idCategoria())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoria não encontrada."));
        if (!request.tipo().name().equals(categoria.getTipo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A categoria escolhida não corresponde ao tipo da oportunidade.");
        }

        Localidade localidade = null;
        if (request.idLocalidade() != null) {
            localidade = localidades.findById(request.idLocalidade())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Localidade não encontrada."));
        }

        String modalidade = request.modalidade().trim().toUpperCase();
        if (!MODALIDADES.contains(modalidade)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Modalidade inválida.");
        }

        String tipoVaga = request.tipoVaga() == null ? null : request.tipoVaga().trim().toUpperCase();
        if (request.tipo() == TipoOportunidade.VAGA
                && (tipoVaga == null || !TIPOS_VAGA.contains(tipoVaga))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione emprego, estágio ou aprendizagem.");
        }
        if (request.tipo() == TipoOportunidade.CURSO
                && request.cargaHoraria() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a carga horária do curso.");
        }
        if (request.dataInicio() != null && request.dataFim() != null
                && request.dataFim().isBefore(request.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data final não pode preceder a data inicial.");
        }
        validarLink(request.linkExterno());

        Oportunidade nova = Oportunidade.criarPendente(
                organizacao, categoria, localidade, request.tipo(), request.titulo().trim(),
                request.descricao().trim(), request.requisitos() == null ? null : request.requisitos().trim(),
                modalidade, tipoVaga, request.remuneracao(), request.cargaHoraria(),
                request.gratuito(), request.valorCurso(), request.dataInicio(), request.dataFim(),
                request.linkExterno().trim());
        return OportunidadeAdminResponse.de(oportunidades.save(nova));
    }

    @Transactional(readOnly = true)
    public List<OportunidadeAdminResponse> oportunidadesPendentes() {
        return oportunidades.listarPorStatus("PENDENTE_REVISAO")
                .stream().map(OportunidadeAdminResponse::de).toList();
    }

    @Transactional
    public OportunidadeAdminResponse revisarOportunidade(Long id, EstadoRevisaoOportunidade estado) {
        Oportunidade oportunidade = oportunidades.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade não encontrada."));
        if (!"PENDENTE_REVISAO".equals(oportunidade.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta oportunidade já foi analisada.");
        }
        oportunidade.decidirRevisao(estado.name());
        return OportunidadeAdminResponse.de(oportunidades.save(oportunidade));
    }

    private void validarLink(String link) {
        try {
            URI uri = URI.create(link.trim());
            if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new IllegalArgumentException("link inválido");
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe um link válido começando com http:// ou https://.");
        }
    }
}

