package br.com.fase.api.organizacao;

import br.com.fase.api.oportunidade.Organizacao;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrganizacaoService {

    private static final Pattern UF_VALIDA = Pattern.compile("[A-Za-z]{2}");
    private final OrganizacaoRepository repository;

    public OrganizacaoService(OrganizacaoRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrganizacaoSolicitacaoResponse solicitar(OrganizacaoSolicitacaoRequest request) {
        String cnpj = request.cnpj().replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (!cnpjValido(cnpj)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe um CNPJ válido com 14 caracteres; as duas últimas posições devem ser dígitos verificadores.");
        }
        if (repository.existsByCnpj(cnpj)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma solicitação para este CNPJ.");
        }

        String uf = request.uf().trim().toUpperCase();
        if (!UF_VALIDA.matcher(uf).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe a sigla do estado com duas letras.");
        }

        String site = request.site() == null || request.site().isBlank() ? null : request.site().trim();
        validarSite(site);

        Organizacao organizacao = Organizacao.criarSolicitacao(
                request.razaoSocial().trim(), cnpj, request.tipo().name(),
                request.responsavel().trim(), request.email().trim().toLowerCase(),
                request.telefone().trim(), site, request.cidade().trim(), uf,
                "Atuação informada: " + request.atuacao().trim());

        Organizacao salva = repository.save(organizacao);
        return new OrganizacaoSolicitacaoResponse(
                salva.getId(), "PENDENTE", "Solicitação recebida e aguardando análise.");
    }

    private void validarSite(String site) {
        if (site == null) return;
        try {
            URI uri = URI.create(site);
            if (uri.getHost() == null || !("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new IllegalArgumentException("URL precisa começar com http:// ou https://");
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe um endereço de site válido começando com http:// ou https://.");
        }
    }

    private boolean cnpjValido(String cnpj) {
        if (cnpj == null || !cnpj.matches("[A-Z0-9]{12}\\d{2}") || cnpj.chars().distinct().count() == 1) return false;
        int[] primeiroPeso = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] segundoPeso = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int primeiro = calcularDigito(cnpj.substring(0, 12), primeiroPeso);
        int segundo = calcularDigito(cnpj.substring(0, 12) + primeiro, segundoPeso);
        return cnpj.endsWith("" + primeiro + segundo);
    }

    private int calcularDigito(String base, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (base.charAt(i) - 48) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }
}
