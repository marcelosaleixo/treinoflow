package com.marceloaleixo.treinoflow.repository;
import com.marceloaleixo.treinoflow.entity.ModeloTreino;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository public interface ModeloTreinoRepository extends JpaRepository<ModeloTreino,Long>{
 Page<ModeloTreino> findByPersonalIdOrderByNomeAsc(Long personalId, Pageable pageable);
 Page<ModeloTreino> findByPersonalIdAndNomeContainingIgnoreCaseOrderByNomeAsc(Long personalId, String nome, Pageable pageable);
 Optional<ModeloTreino> findByIdAndPersonalId(Long id, Long personalId);
 boolean existsByNomeIgnoreCaseAndPersonalId(String nome, Long personalId);
}
