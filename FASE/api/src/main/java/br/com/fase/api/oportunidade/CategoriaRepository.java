package br.com.fase.api.oportunidade;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByTipoOrderByNomeAsc(String tipo);
}
