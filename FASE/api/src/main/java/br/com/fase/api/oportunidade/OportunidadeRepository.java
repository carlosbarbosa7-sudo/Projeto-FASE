package br.com.fase.api.oportunidade;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OportunidadeRepository extends JpaRepository<Oportunidade, Long> {

    @Query("""
            select o from Oportunidade o
            join fetch o.categoria
            join fetch o.organizacao
            left join fetch o.localidade
            where o.status = :status
            order by o.id
            """)
    List<Oportunidade> listarPorStatus(@Param("status") String status);

    @Query("""
            select o from Oportunidade o
            join fetch o.categoria c
            join fetch o.organizacao org
            left join fetch o.localidade l
            where o.status = 'PUBLICADA'
              and (:tipo is null or o.tipo = :tipo)
              and (:modalidade is null or o.modalidade = :modalidade)
              and (:busca is null or
                   lower(o.titulo) like lower(concat('%', :busca, '%')) or
                   lower(o.descricao) like lower(concat('%', :busca, '%')) or
                   lower(coalesce(o.requisitos, '')) like lower(concat('%', :busca, '%')) or
                   lower(c.nome) like lower(concat('%', :busca, '%')) or
                   lower(org.razaoSocial) like lower(concat('%', :busca, '%')))
            order by o.dataInicio asc, o.id desc
            """)
    List<Oportunidade> buscarPublicadas(
            @Param("tipo") TipoOportunidade tipo,
            @Param("modalidade") String modalidade,
            @Param("busca") String busca);

    @Query("""
            select o from Oportunidade o
            join fetch o.categoria
            join fetch o.organizacao
            left join fetch o.localidade
            where o.id = :id and o.status = 'PUBLICADA'
            """)
    Optional<Oportunidade> buscarPublicadaPorId(@Param("id") Long id);

    @Query("""
            select o from Oportunidade o
            join fetch o.categoria
            join fetch o.organizacao
            left join fetch o.localidade
            where o.status = 'PUBLICADA' and o.id in :ids
            """)
    List<Oportunidade> buscarPublicadasPorIds(@Param("ids") List<Long> ids);
}
