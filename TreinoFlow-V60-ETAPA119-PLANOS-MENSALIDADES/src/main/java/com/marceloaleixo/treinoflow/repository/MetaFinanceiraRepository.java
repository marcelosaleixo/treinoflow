package com.marceloaleixo.treinoflow.repository;
import com.marceloaleixo.treinoflow.entity.MetaFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
public interface MetaFinanceiraRepository extends JpaRepository<MetaFinanceira,Long>{
 Optional<MetaFinanceira> findByPersonalIdAndMesReferencia(Long personalId, LocalDate mesReferencia);
}
