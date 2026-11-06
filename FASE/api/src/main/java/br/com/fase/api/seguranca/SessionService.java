package br.com.fase.api.seguranca;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class SessionService {

    private static final long SESSION_DURATION_SECONDS = 8 * 60 * 60;

    private final String adminUsername;
    private final String adminPassword;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public SessionService(
            @Value("${fase.admin.username:}") String adminUsername,
            @Value("${fase.admin.password:}") String adminPassword) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    public boolean isAdminUsername(String username) {
        return adminUsername != null && !adminUsername.isBlank()
                && equalsConstantTime(adminUsername, username);
    }

    public LoginResponse loginAdmin(String username, String password) {
        if (adminUsername == null || adminUsername.isBlank()
                || adminPassword == null || adminPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "A conta administrativa ainda não foi configurada no servidor.");
        }
        if (!equalsConstantTime(adminUsername, username) || !equalsConstantTime(adminPassword, password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha incorretos.");
        }
        return createSession(username, "ADMIN", null, null);
    }

    public LoginResponse createSession(String username, String perfil, Long usuarioId, Long organizacaoId) {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        sessions.put(token, new Session(username, perfil, usuarioId, organizacaoId,
                now.plusSeconds(SESSION_DURATION_SECONDS)));
        return new LoginResponse(token, username, perfil);
    }

    public Session requireSession(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Entre para continuar.");
        }
        String token = authorization.substring("Bearer ".length()).trim();
        Session session = sessions.get(token);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessão inválida ou expirada.");
        }
        if (!session.expiresAt().isAfter(Instant.now())) {
            sessions.remove(token, session);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sua sessão expirou. Entre novamente.");
        }
        return session;
    }

    public Session requireRole(String authorization, String role) {
        Session session = requireSession(authorization);
        if (!role.equals(session.perfil())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para esta ação.");
        }
        return session;
    }

    public void logout(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            sessions.remove(authorization.substring("Bearer ".length()).trim());
        }
    }

    private boolean equalsConstantTime(String expected, String supplied) {
        return supplied != null && MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                supplied.getBytes(StandardCharsets.UTF_8));
    }

    public record Session(String username, String perfil, Long usuarioId, Long organizacaoId, Instant expiresAt) {
    }
}
