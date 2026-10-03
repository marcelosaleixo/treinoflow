package com.marceloaleixo.treinoflow.repository;
import com.marceloaleixo.treinoflow.entity.Exercicio;
import com.marceloaleixo.treinoflow.enums.GrupoMuscular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
@Repository
public interface ExercicioRepository extends JpaRepository<Exercicio, Long> {
    List<Exercicio> findByPersonalIdOrPersonalIsNullOrderByNomeAsc(Long personalId);
    Page<Exercicio> findByPersonalIdOrPersonalIsNullOrderByNomeAsc(Long personalId, Pageable pageable);

    @Query("SELECT e FROM Exercicio e WHERE (e.personal.id = :personalId OR e.personal IS NULL) " +
            "AND (:termo IS NULL OR :termo = '' OR " +
            "LOWER(e.nome) LIKE LOWER(CONCAT('%', :termo, '%'))) " +
            "AND (:grupo IS NULL OR e.grupoMuscular = :grupo) " +
            "ORDER BY e.nome ASC")
    Page<Exercicio> buscarDisponiveisPorFiltro(@Param("personalId") Long personalId,
                                                @Param("termo") String termo,
                                                @Param("grupo") GrupoMuscular grupo,
                                                Pageable pageable);
    Optional<Exercicio> findByIdAndPersonalId(Long id, Long personalId);

    @Query("SELECT COUNT(e) FROM Exercicio e WHERE e.personal.id = :personalId OR e.personal IS NULL")
    long contarDisponiveisParaPersonal(@Param("personalId") Long personalId);

    @Query("SELECT COUNT(e) > 0 FROM Exercicio e WHERE e.personal IS NULL AND LOWER(e.nome) = LOWER(:nome)")
    boolean existsGlobalByNome(@Param("nome") String nome);
}
