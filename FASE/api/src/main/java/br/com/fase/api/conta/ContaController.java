package br.com.fase.api.conta;

import br.com.fase.api.oportunidade.OportunidadeRepository;
import br.com.fase.api.oportunidade.OportunidadeResponse;
import br.com.fase.api.seguranca.SessionService;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/me")
public class ContaController {

    private final SessionService sessions;
    private final AuthenticationService authentication;
    private final OportunidadeSalvaRepository salvas;
    private final OportunidadeRepository oportunidades;

    public ContaController(SessionService sessions, AuthenticationService authentication,
            OportunidadeSalvaRepository salvas, OportunidadeRepository oportunidades) {
        this.sessions = sessions;
        this.authentication = authentication;
        this.salvas = salvas;
        this.oportunidades = oportunidades;
    }

    @GetMapping
    public ContaResponse conta(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        return authentication.currentAccount(sessions.requireSession(authorization));
    }

    @GetMapping("/oportunidades-salvas")
    @Transactional(readOnly = true)
    public List<OportunidadeResponse> oportunidadesSalvas(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        SessionService.Session session = sessions.requireRole(authorization, "JOVEM");
        List<Long> ids = salvas.findByUsuarioIdOrderByDataSalvamentoDesc(session.usuarioId()).stream()
                .map(OportunidadeSalva::getOportunidadeId).toList();
        if (ids.isEmpty()) return List.of();
        return oportunidades.buscarPublicadasPorIds(ids).stream()
                .sorted(Comparator.comparingInt(item -> ids.indexOf(item.getId())))
                .map(OportunidadeResponse::de)
                .toList();
    }

    @PostMapping("/oportunidades-salvas/{id}")
    @Transactional
    public void salvarOportunidade(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        SessionService.Session session = sessions.requireRole(authorization, "JOVEM");
        if (oportunidades.buscarPublicadaPorId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Oportunidade publicada não encontrada.");
        }
        OportunidadeSalvaId key = new OportunidadeSalvaId(session.usuarioId(), id);
        if (!salvas.existsById(key)) salvas.save(OportunidadeSalva.criar(session.usuarioId(), id));
    }

    @DeleteMapping("/oportunidades-salvas/{id}")
    @Transactional
    public void removerOportunidadeSalva(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @PathVariable Long id) {
        SessionService.Session session = sessions.requireRole(authorization, "JOVEM");
        salvas.deleteById(new OportunidadeSalvaId(session.usuarioId(), id));
    }
}
