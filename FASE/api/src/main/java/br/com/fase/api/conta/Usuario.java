package br.com.fase.api.conta;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nome", nullable = false, length = 160)
    private String nome;

    @Column(name = "email", nullable = false, length = 254, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(name = "perfil", nullable = false)
    private String perfil;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    protected Usuario() {
    }

    public static Usuario criar(String nome, String email, String senhaHash, String perfil) {
        Usuario usuario = new Usuario();
        usuario.nome = nome;
        usuario.email = email;
        usuario.senhaHash = senhaHash;
        usuario.perfil = perfil;
        usuario.ativo = true;
        return usuario;
    }

    public void alterarSenha(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenhaHash() { return senhaHash; }
    public String getPerfil() { return perfil; }
    public Boolean getAtivo() { return ativo; }
}
