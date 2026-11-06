package br.com.fase.api.conta;

import br.com.fase.api.organizacao.OrganizacaoRepository;
import br.com.fase.api.oportunidade.Organizacao;
import br.com.fase.api.seguranca.AlterarSenhaRequest;
import br.com.fase.api.seguranca.LoginResponse;
import br.com.fase.api.seguranca.SenhaService;
import br.com.fase.api.seguranca.SessionService;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthenticationService {

    private final UsuarioRepository usuarios;
    private final PerfilJovemRepository perfisJovens;
    private final OrganizacaoRepository organizacoes;
    private final SenhaService senhas;
    private final SessionService sessions;

    public AuthenticationService(
            UsuarioRepository usuarios,
            PerfilJovemRepository perfisJovens,
            OrganizacaoRepository organizacoes,
            SenhaService senhas,
            SessionService sessions) {
        this.usuarios = usuarios;
        this.perfisJovens = perfisJovens;
        this.organizacoes = organizacoes;
        this.senhas = senhas;
        this.sessions = sessions;
    }

    @Transactional
    public LoginResponse registerYouth(CadastroJovemRequest request) {
        String email = normalizeEmail(request.email());
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma conta com este e-mail.");
        }
        LocalDate birthDate = request.dataNascimento();
        if (birthDate.isAfter(LocalDate.now().minusYears(16))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "É necessário ter pelo menos 16 anos para criar uma conta.");
        }

        String uf = request.uf() == null || request.uf().isBlank()
                ? null : request.uf().trim().toUpperCase(Locale.ROOT);
        if (uf != null && !uf.matches("[A-Z]{2}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe a sigla do estado com duas letras.");
        }

        Usuario usuario = usuarios.save(Usuario.criar(
                request.nome().trim(), email, senhas.hash(request.senha()), "JOVEM"));
        perfisJovens.save(PerfilJovem.criar(
                usuario.getId(), birthDate, trimOrNull(request.telefone()),
                trimOrNull(request.escolaridade()), trimOrNull(request.cidade()),
                trimOrNull(request.bairro()), uf));
        return sessions.createSession(usuario.getEmail(), usuario.getPerfil(), usuario.getId(), null);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String usernameOrEmail, String password) {
        if (sessions.isAdminUsername(usernameOrEmail)) {
            return sessions.loginAdmin(usernameOrEmail, password);
        }

        String email = normalizeEmail(usernameOrEmail);
        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(this::invalidCredentials);
        if (!Boolean.TRUE.equals(usuario.getAtivo()) || !senhas.matches(password, usuario.getSenhaHash())) {
            throw invalidCredentials();
        }

        Long organizacaoId = null;
        if ("INSTITUICAO".equals(usuario.getPerfil())) {
            Organizacao organizacao = organizacoes.findByUsuarioId(usuario.getId())
                    .filter(item -> "APROVADA".equals(item.getStatusAprovacao()))
                    .orElseThrow(this::invalidCredentials);
            organizacaoId = organizacao.getId();
        } else if (!"JOVEM".equals(usuario.getPerfil())) {
            throw invalidCredentials();
        }
        return sessions.createSession(usuario.getEmail(), usuario.getPerfil(), usuario.getId(), organizacaoId);
    }

    @Transactional(readOnly = true)
    public ContaResponse currentAccount(SessionService.Session session) {
        if ("ADMIN".equals(session.perfil())) {
            return new ContaResponse(session.username(), session.username(), "ADMIN", null,
                    null, null, null, null, null, null, null);
        }
        Usuario usuario = usuarios.findById(session.usuarioId()).orElseThrow(this::invalidCredentials);
        if ("INSTITUICAO".equals(usuario.getPerfil())) {
            Organizacao organizacao = organizacoes.findByUsuarioId(usuario.getId())
                    .orElseThrow(this::invalidCredentials);
            return new ContaResponse(usuario.getNome(), usuario.getEmail(), usuario.getPerfil(),
                    organizacao.getRazaoSocial(), null, organizacao.getTelefone(), null,
                    organizacao.getCidade(), null, organizacao.getUf(), organizacao.getStatusAprovacao());
        }
        PerfilJovem perfil = perfisJovens.findById(usuario.getId()).orElseThrow(this::invalidCredentials);
        return new ContaResponse(usuario.getNome(), usuario.getEmail(), usuario.getPerfil(), null,
                perfil.getDataNascimento(), perfil.getTelefone(), perfil.getEscolaridade(),
                perfil.getCidade(), perfil.getBairro(), perfil.getUf(), null);
    }

    @Transactional
    public void changePassword(SessionService.Session session, AlterarSenhaRequest request) {
        if (session.usuarioId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A senha da conta administrativa local é configurada ao iniciar a API.");
        }
        Usuario usuario = usuarios.findById(session.usuarioId()).orElseThrow(this::invalidCredentials);
        if (!senhas.matches(request.senhaAtual(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A senha atual está incorreta.");
        }
        usuario.alterarSenha(senhas.hash(request.novaSenha()));
        usuarios.save(usuario);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) return "";
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String trimOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha incorretos.");
    }
}
