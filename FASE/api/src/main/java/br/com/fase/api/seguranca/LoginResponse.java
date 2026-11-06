package br.com.fase.api.seguranca;

public record LoginResponse(String token, String usuario, String perfil) {
}
