package com.marceloaleixo.treinoflow.repository;
import com.marceloaleixo.treinoflow.entity.Exercicio;
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
            "ORDER BY e.nome ASC")
    Page<Exercicio> buscarDisponiveisPorTermo(@Param("personalId") Long personalId,
                                               @Param("termo") String termo,
                                               Pageable pageable);
    Optional<Exercicio> findByIdAndPersonalId(Long id, Long personalId);

    @Query("SELECT COUNT(e) FROM Exercicio e WHERE e.personal.id = :personalId OR e.personal IS NULL")
    long contarDisponiveisParaPersonal(@Param("personalId") Long personalId);
}
