package br.com.fase.api.conta;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OportunidadeSalvaRepository extends JpaRepository<OportunidadeSalva, OportunidadeSalvaId> {
    List<OportunidadeSalva> findByUsuarioIdOrderByDataSalvamentoDesc(Long usuarioId);
}
