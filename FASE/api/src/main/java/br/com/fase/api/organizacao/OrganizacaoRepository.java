package br.com.fase.api.organizacao;

import br.com.fase.api.oportunidade.Organizacao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizacaoRepository extends JpaRepository<Organizacao, Long> {
    boolean existsByCnpj(String cnpj);
    List<Organizacao> findByStatusAprovacaoOrderByIdAsc(String statusAprovacao);
    Optional<Organizacao> findByIdAndStatusAprovacao(Long id, String statusAprovacao);
    Optional<Organizacao> findByUsuarioId(Long usuarioId);
}
