package br.com.fase.api.organizacao;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizacoes")
public class OrganizacaoController {

    private final OrganizacaoService service;

    public OrganizacaoController(OrganizacaoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OrganizacaoSolicitacaoResponse> solicitar(
            @Valid @RequestBody OrganizacaoSolicitacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.solicitar(request));
    }
}
