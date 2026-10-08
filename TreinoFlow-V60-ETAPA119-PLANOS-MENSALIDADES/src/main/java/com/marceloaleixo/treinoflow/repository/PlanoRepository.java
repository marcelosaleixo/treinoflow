package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.Plano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanoRepository extends JpaRepository<Plano, Long> {
    boolean existsByNomeIgnoreCase(String nome);
    Optional<Plano> findFirstByPadraoTrue();
    Optional<Plano> findFirstByAtivoTrueOrderByIdAsc();
}
