package com.marceloaleixo.treinoflow.repository;
import com.marceloaleixo.treinoflow.entity.MetaReceitaAcoes;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
public interface MetaReceitaAcoesRepository extends JpaRepository<MetaReceitaAcoes,Long>{
 Optional<MetaReceitaAcoes> findByPersonalIdAndMesReferencia(Long personalId, LocalDate mesReferencia);
}
