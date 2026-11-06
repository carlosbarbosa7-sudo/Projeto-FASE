package br.com.fase.api.config;

import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(MethodArgumentNotValidException exception) {
        List<String> problemas = exception.getBindingResult().getFieldErrors().stream()
                .map(this::descreverCampo)
                .distinct()
                .toList();
        String mensagem = problemas.isEmpty()
                ? "Confira os dados obrigatórios do formulário."
                : "Revise estes campos: " + String.join("; ", problemas) + ".";
        return ResponseEntity.badRequest().body(Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "mensagem", mensagem));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> tratarStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String mensagem = exception.getReason() == null ? "A solicitação não pôde ser concluída." : exception.getReason();
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "mensagem", mensagem));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> tratarConflito(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", HttpStatus.CONFLICT.value(),
                "mensagem", "Não foi possível gravar os dados. Confira se o e-mail ou CNPJ já está cadastrado."));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> tratarCorpoInvalido(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "mensagem", "Não consegui ler os dados enviados. Confira as opções selecionadas no formulário."));
    }

    private String descreverCampo(FieldError erro) {
        String nome = switch (erro.getField()) {
            case "razaoSocial" -> "Nome da organização";
            case "cnpj" -> "CNPJ";
            case "tipo" -> "Tipo de organização";
            case "responsavel" -> "Pessoa responsável";
            case "nome" -> "Nome";
            case "email" -> "E-mail";
            case "senha" -> "Senha";
            case "novaSenha" -> "Nova senha";
            case "senhaProvisoria" -> "Senha provisória da instituição";
            case "telefone" -> "Telefone";
            case "site" -> "Site institucional";
            case "cidade" -> "Cidade";
            case "atuacao" -> "O que pretende divulgar";
            case "confirmacao" -> "Confirmação de representação";
            case "dataNascimento" -> "Data de nascimento";
            case "uf" -> "Estado";
            default -> erro.getField();
        };

        String motivo = switch (erro.getCode() == null ? "" : erro.getCode()) {
            case "NotBlank", "NotNull" -> "é obrigatório";
            case "Email" -> "precisa ser um e-mail válido";
            case "Size" -> "não atende ao tamanho permitido";
            case "AssertTrue" -> "precisa ser confirmada";
            default -> "está inválido";
        };
        return nome + " " + motivo;
    }
}
