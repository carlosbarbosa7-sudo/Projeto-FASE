package br.com.fase.api.seguranca;

import br.com.fase.api.conta.AuthenticationService;
import br.com.fase.api.conta.CadastroJovemRequest;
import br.com.fase.api.conta.ContaResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authentication;
    private final SessionService sessions;

    public AuthController(AuthenticationService authentication, SessionService sessions) {
        this.authentication = authentication;
        this.sessions = sessions;
    }

    @PostMapping("/register/youth")
    public LoginResponse registerYouth(@Valid @RequestBody CadastroJovemRequest request) {
        return authentication.registerYouth(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody AdminLoginRequest request) {
        return authentication.login(request.usuario(), request.senha());
    }

    @GetMapping("/session")
    public Map<String, String> session(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        SessionService.Session session = sessions.requireSession(authorization);
        return Map.of("usuario", session.username(), "perfil", session.perfil());
    }

    @PostMapping("/change-password")
    public void changePassword(
            @RequestHeader(name = "Authorization", required = false) String authorization,
            @Valid @RequestBody AlterarSenhaRequest request) {
        authentication.changePassword(sessions.requireSession(authorization), request);
    }

    @PostMapping("/logout")
    public void logout(
            @RequestHeader(name = "Authorization", required = false) String authorization) {
        sessions.logout(authorization);
    }
}
