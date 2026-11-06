package br.com.fase.api.oportunidade;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/oportunidades")
public class OportunidadeController {

    private final OportunidadeRepository repository;

    public OportunidadeController(OportunidadeRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<OportunidadeResponse> listar(
            @RequestParam(required = false) TipoOportunidade tipo,
            @RequestParam(required = false) String modalidade,
            @RequestParam(required = false) String busca) {
        String buscaNormalizada = busca == null || busca.isBlank() ? null : busca.trim();
        String modalidadeNormalizada = modalidade == null || modalidade.isBlank()
                ? null : modalidade.trim().toUpperCase();
        return repository.buscarPublicadas(tipo, modalidadeNormalizada, buscaNormalizada)
                .stream()
                .map(OportunidadeResponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    public OportunidadeResponse detalhar(@PathVariable Long id) {
        return repository.buscarPublicadaPorId(id)
                .map(OportunidadeResponse::de)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Oportunidade não encontrada."));
    }
}
