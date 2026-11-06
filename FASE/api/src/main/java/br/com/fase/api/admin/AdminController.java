package br.com.fase.api.admin;

import br.com.fase.api.oportunidade.EstadoRevisaoOportunidade;
import br.com.fase.api.oportunidade.NovaOportunidadeRequest;
import br.com.fase.api.oportunidade.OportunidadeAdminResponse;
import br.com.fase.api.oportunidade.RevisaoOportunidadeRequest;
import br.com.fase.api.oportunidade.TipoOportunidade;
import br.com.fase.api.organizacao.EstadoRevisaoOrganizacao;
import br.com.fase.api.organizacao.OrganizacaoAdminResponse;
import br.com.fase.api.organizacao.RevisaoOrganizacaoRequest;
import br.com.fase.api.seguranca.SessionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SessionService sessions;
    private final AdminService service;

    public AdminController(SessionService sessions, AdminService service) {
        this.sessions = sessions;
        this.service = service;
    }

    @GetMapping("/categorias")
    public List<CategoriaAdminResponse> categorias(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @org.springframework.web.bind.annotation.RequestParam TipoOportunidade tipo) {
        sessions.requireRole(authorization, "ADMIN");
        return service.categorias(tipo);
    }

    @GetMapping("/localidades")
    public List<LocalidadeAdminResponse> localidades(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        sessions.requireRole(authorization, "ADMIN");
        return service.localidades();
    }

    @GetMapping("/organizacoes/pendentes")
    public List<OrganizacaoAdminResponse> organizacoesPendentes(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        sessions.requireRole(authorization, "ADMIN");
        return service.organizacoesPendentes();
    }

    @PatchMapping("/organizacoes/{id}/status")
    public OrganizacaoAdminResponse revisarOrganizacao(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody RevisaoOrganizacaoRequest request) {
        sessions.requireRole(authorization, "ADMIN");
        return service.revisarOrganizacao(id, request.status(), request.motivo(), request.senhaProvisoria());
    }

    @PostMapping("/oportunidades")
    public ResponseEntity<OportunidadeAdminResponse> criarOportunidade(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody NovaOportunidadeRequest request) {
        sessions.requireRole(authorization, "ADMIN");
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criarOportunidade(request));
    }

    @GetMapping("/oportunidades/pendentes")
    public List<OportunidadeAdminResponse> oportunidadesPendentes(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        sessions.requireRole(authorization, "ADMIN");
        return service.oportunidadesPendentes();
    }

    @PatchMapping("/oportunidades/{id}/status")
    public OportunidadeAdminResponse revisarOportunidade(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable Long id,
            @Valid @RequestBody RevisaoOportunidadeRequest request) {
        sessions.requireRole(authorization, "ADMIN");
        return service.revisarOportunidade(id, request.status());
    }
}
