package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.ExecucaoExercicio;
import com.marceloaleixo.treinoflow.entity.RegistroTreinoAluno;
import com.marceloaleixo.treinoflow.entity.Treino;
import com.marceloaleixo.treinoflow.entity.TreinoExercicio;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.ExecucaoExercicioRepository;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import com.marceloaleixo.treinoflow.repository.TreinoExercicioRepository;
import com.marceloaleixo.treinoflow.repository.TreinoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class PortalAlunoService {
    private final AlunoRepository alunos;
    private final TreinoRepository treinos;
    private final TreinoExercicioRepository itens;
    private final RegistroTreinoAlunoRepository registros;
    private final ExecucaoExercicioRepository execucoes;

    public PortalAlunoService(AlunoRepository alunos, TreinoRepository treinos, TreinoExercicioRepository itens, RegistroTreinoAlunoRepository registros, ExecucaoExercicioRepository execucoes) {
        this.alunos = alunos;
        this.treinos = treinos;
        this.itens = itens;
        this.registros = registros;
        this.execucoes = execucoes;
    }

    public Aluno buscarAlunoPorToken(String token) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException("Link do portal inválido.");
        return alunos.findByTokenPortal(token.trim())
                .filter(a -> "ATIVO".equalsIgnoreCase(a.getStatus()))
                .orElseThrow(() -> new IllegalArgumentException("Portal não encontrado ou aluno inativo."));
    }

    public String obterOuCriarToken(Long alunoId, Long personalId) {
        Aluno aluno = alunos.findByIdAndPersonalId(alunoId, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal."));
        if (aluno.getTokenPortal() == null || aluno.getTokenPortal().isBlank()) {
            aluno.setTokenPortal(UUID.randomUUID().toString().replace("-", ""));
            alunos.save(aluno);
        }
        return aluno.getTokenPortal();
    }

    @Transactional(readOnly = true)
    public List<Treino> listarTreinos(Aluno aluno) {
        return treinos.buscarLiberadosDoPortal(aluno.getId(), LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Treino buscarTreino(Aluno aluno, Long treinoId) {
        return treinos.buscarTreinoDoPortal(treinoId, aluno.getId(), LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Treino não encontrado ou expirado."));
    }

    @Transactional(readOnly = true)
    public List<TreinoExercicio> listarExercicios(Treino treino) {
        return itens.listarComExercicioPorTreino(treino.getId());
    }

    public RegistroTreinoAluno registrarConclusao(Aluno aluno, Long treinoId, Integer nota, String feedback, Map<String, String> parametros) {
        Treino treino = buscarTreino(aluno, treinoId);
        if (nota != null && (nota < 1 || nota > 5)) throw new IllegalArgumentException("A avaliação deve ser de 1 a 5.");
        LocalDate hoje = LocalDate.now();
        RegistroTreinoAluno registro = registros.findByTreinoIdAndDataExecucao(treino.getId(), hoje).orElseGet(RegistroTreinoAluno::new);
        registro.setTreino(treino);
        registro.setAluno(aluno);
        registro.setDataExecucao(hoje);
        registro.setConcluido(true);
        registro.setNota(nota);
        registro.setFeedback(feedback == null ? null : feedback.trim());
        registro = registros.save(registro);

        for (TreinoExercicio item : listarExercicios(treino)) {
            String carga = valor(parametros, "carga_" + item.getId());
            Integer repeticoes = inteiro(valor(parametros, "reps_" + item.getId()));
            String observacao = valor(parametros, "obs_" + item.getId());
            if ((carga == null || carga.isBlank()) && repeticoes == null && (observacao == null || observacao.isBlank())) continue;

            ExecucaoExercicio execucao = execucoes.findByRegistroIdAndTreinoExercicioId(registro.getId(), item.getId())
                    .orElseGet(ExecucaoExercicio::new);
            execucao.setRegistro(registro);
            execucao.setTreinoExercicio(item);
            execucao.setCargaRealizada(carga);
            execucao.setRepeticoesRealizadas(repeticoes);
            execucao.setObservacao(observacao);
            execucao.setConcluido(true);
            execucoes.save(execucao);
        }
        return registro;
    }

    private String valor(Map<String, String> parametros, String chave) {
        String valor = parametros == null ? null : parametros.get(chave);
        if (valor == null) return null;
        valor = valor.trim();
        return valor.isBlank() ? null : valor;
    }

    private Integer inteiro(String valor) {
        if (valor == null) return null;
        try { return Integer.valueOf(valor); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("Repetições realizadas devem ser números inteiros."); }
    }

    @Transactional(readOnly = true)
    public RegistroTreinoAluno ultimaExecucao(Treino treino) {
        return registros.findTopByTreinoIdOrderByDataExecucaoDesc(treino.getId()).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<RegistroTreinoAluno> historico(Aluno aluno) {
        return registros.buscarHistorico(aluno.getId());
    }
}
