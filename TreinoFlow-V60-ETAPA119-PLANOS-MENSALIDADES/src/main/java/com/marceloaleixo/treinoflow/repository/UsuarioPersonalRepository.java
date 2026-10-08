package com.marceloaleixo.treinoflow.repository;

import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioPersonalRepository extends JpaRepository<UsuarioPersonal, Long> {

    Optional<UsuarioPersonal> findByEmail(String email);

    /**
     * Serializa alterações de cadastro por personal para impedir ultrapassar o
     * limite em requisições simultâneas.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from UsuarioPersonal p where p.id = :id")
    Optional<UsuarioPersonal> findByIdParaAtualizacao(@org.springframework.data.repository.query.Param("id") Long id);

    boolean existsByEmail(String email);

    List<UsuarioPersonal> findByPerfilAndAtivoTrue(String perfil);

    long countByPerfil(String perfil);

    java.util.List<UsuarioPersonal> findByPerfilAndPlanoIsNull(String perfil);

    /**
     * Carrega o plano na mesma consulta para telas administrativas. Necessário
     * porque spring.jpa.open-in-view=false.
     */
    @EntityGraph(attributePaths = "plano")
    @Query("select p from UsuarioPersonal p")
    List<UsuarioPersonal> findAllComPlano();

    @EntityGraph(attributePaths = "plano")
    @Query("select p from UsuarioPersonal p where p.id = :id")
    Optional<UsuarioPersonal> findByIdComPlano(@org.springframework.data.repository.query.Param("id") Long id);
}
