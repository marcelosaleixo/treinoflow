package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Exercicio;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.GrupoMuscular;
import com.marceloaleixo.treinoflow.repository.ExercicioRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ExercicioService {

    @Autowired
    private ExercicioRepository exercicios;
    @Autowired
    private UsuarioPersonalRepository personais;
    @Autowired
    private ExercicioRepository exercicioRepository;

    @Transactional(readOnly = true)
    public List<Exercicio> listarDisponiveis(Long personalId) {
        return exercicios.findByPersonalIdOrPersonalIsNullOrderByNomeAsc(personalId);
    }

    @Transactional(readOnly = true)
    public List<GrupoMuscular> listarGruposMusculares(Long personalId) {
        return List.of(GrupoMuscular.values());
    }

    @Transactional(readOnly = true)
    public Page<Exercicio> listarPaginado(Long personalId, Pageable pageable) {
        return listarPaginado(personalId, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Exercicio> listarPaginado(Long personalId, String termo, Pageable pageable) {
        if (personalId == null || personalId <= 0) {
            throw new IllegalArgumentException("Personal inválido.");
        }
        String filtro = termo == null || termo.isBlank() ? null : termo.trim();
        return exercicios.buscarDisponiveisPorTermo(personalId, filtro, pageable);
    }

    public Exercicio salvar(Long personalId, Exercicio e) {
        UsuarioPersonal p = personais.findById(personalId).orElseThrow(() -> new IllegalArgumentException("Personal não encontrado."));
        if (e.getNome() == null || e.getNome().isBlank() || e.getNome().trim().length() > 120) {
            throw new IllegalArgumentException("Nome do exercício é obrigatório (até 120 caracteres).");
        }
        if (e.getId() != null && exercicios.findByIdAndPersonalId(e.getId(), personalId).isEmpty()) {
            throw new IllegalArgumentException("Exercício não encontrado ou não pertence a este personal.");
        }
        e.setPersonal(p);
        e.setNome(e.getNome().trim());
        return exercicios.save(e);
    }

    public void excluir(Long id, Long personalId) {
        Exercicio e = exercicios.findByIdAndPersonalId(id, personalId).orElseThrow(() -> new IllegalArgumentException("Exercício não encontrado ou não pertence a este personal."));
        exercicios.delete(e);
    }

    public Exercicio buscarDoPersonal(Long id, Long personalId) {
        return exercicioRepository.findByIdAndPersonalId(id, personalId)
                .orElseThrow(()
                        -> new IllegalArgumentException(
                        "Exercício não encontrado ou não pertence ao personal."
                )
                );
    }
}
