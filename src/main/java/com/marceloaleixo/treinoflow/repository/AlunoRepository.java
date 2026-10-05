package com.marceloaleixo.treinoflow.repository;
import com.marceloaleixo.treinoflow.entity.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    List<Aluno> findByPersonalIdOrderByNomeAsc(Long personalId);
    List<Aluno> findByPersonalIdAndStatusOrderByNomeAsc(Long personalId, String status);
    Page<Aluno> findByPersonalIdOrderByNomeAsc(Long personalId, Pageable pageable);

    @Query("SELECT a FROM Aluno a WHERE a.personal.id = :personalId " +
            "AND (:termo IS NULL OR :termo = '' OR " +
            "LOWER(a.nome) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(COALESCE(a.email, '')) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
            "LOWER(COALESCE(a.telefone, '')) LIKE LOWER(CONCAT('%', :termo, '%'))) " +
            "ORDER BY a.nome ASC")
    Page<Aluno> buscarPorPersonalETermo(@Param("personalId") Long personalId,
                                         @Param("termo") String termo,
                                         Pageable pageable);
    Optional<Aluno> findByIdAndPersonalId(Long id, Long personalId);
    @Query("SELECT a FROM Aluno a JOIN FETCH a.personal p WHERE a.tokenPortal = :tokenPortal")
    Optional<Aluno> findByTokenPortal(@Param("tokenPortal") String tokenPortal);
    long countByPersonalIdAndStatus(Long personalId, String status);
}
