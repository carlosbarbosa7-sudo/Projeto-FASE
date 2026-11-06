package br.com.fase.api.oportunidade;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocalidadeRepository extends JpaRepository<Localidade, Long> {
    List<Localidade> findAllByOrderByCidadeAscUfAsc();
}
