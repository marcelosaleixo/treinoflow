package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.TreinoAlunoView;
import com.marceloaleixo.treinoflow.entity.Treino;
import com.marceloaleixo.treinoflow.repository.TreinoExercicioRepository;
import com.marceloaleixo.treinoflow.repository.TreinoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
@Transactional
public class AcessoAlunoService {
    @Autowired
    private TreinoRepository treinoRepository;

    @Autowired
    private TreinoExercicioRepository treinoExercicioRepository;

    public TreinoAlunoView buscarTreinoLiberado(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Link de acesso inválido.");
        }
        Treino treino = treinoRepository.findByTokenAcesso(token.trim())
                .filter(t -> "LIBERADO".equals(t.getStatus()))
                .filter(t -> t.getAcessoExpiraEm() == null || t.getAcessoExpiraEm().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException("Treino não encontrado ou não está liberado."));

        treino.setTotalVisualizacoes(treino.getTotalVisualizacoes() + 1);
        treino.setDataUltimaVisualizacao(LocalDateTime.now());

        List<TreinoAlunoView.ExercicioView> exercicios = treinoExercicioRepository
                .findByTreinoIdOrderByOrdemAsc(treino.getId()).stream()
                .map(item -> new TreinoAlunoView.ExercicioView(
                        item.getOrdem(), item.getExercicio().getNome(),
                        item.getExercicio().getGrupoMuscular().getRotulo(), item.getExercicio().getDescricao(),
                        item.getExercicio().getUrlVideo(), item.getSeries(), item.getRepeticoes(),
                        item.getCarga(), item.getDescansoSegundos(), item.getObservacao()))
                .toList();

        return new TreinoAlunoView(treino.getNome(), treino.getDescricao(),
                treino.getAluno().getNome(), treino.getAluno().getPersonal().getNome(),
                treino.getDataLiberacao(), treino.getAcessoExpiraEm(), exercicios);
    }
}
