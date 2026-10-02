package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.Treino;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.TreinoRepository;
import com.marceloaleixo.treinoflow.repository.TreinoExercicioRepository;
import com.marceloaleixo.treinoflow.entity.TreinoExercicio;
import com.marceloaleixo.treinoflow.entity.EventoCompartilhamento;
import com.marceloaleixo.treinoflow.repository.EventoCompartilhamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TreinoService {

    @Autowired
    private TreinoRepository treinos;
    @Autowired
    private AlunoRepository alunos;
    @Autowired
    private TreinoExercicioRepository itensTreino;
    @Autowired
    private EventoCompartilhamentoRepository eventosCompartilhamento;

    @Transactional(readOnly = true)
    public List<Treino> listarDoAluno(Long alunoId, Long personalId) {
        validarAluno(alunoId, personalId);
        return treinos.findByAlunoIdOrderByDataCriacaoDesc(alunoId);
    }

    @Transactional(readOnly = true)
    public Page<Treino> listarPaginadoDoAluno(Long alunoId, Long personalId, Pageable pageable) {
        validarAluno(alunoId, personalId);
        return treinos.findByAlunoIdOrderByDataCriacaoDesc(alunoId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Treino> buscarPaginadoDoAluno(Long alunoId, Long personalId, String termo, Pageable pageable) {
        validarAluno(alunoId, personalId);
        if (termo == null || termo.isBlank()) {
            return treinos.findByAlunoIdOrderByDataCriacaoDesc(alunoId, pageable);
        }
        return treinos.findByAlunoIdAndNomeContainingIgnoreCaseOrderByDataCriacaoDesc(
                alunoId, termo.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Treino> buscarPaginadoDoAluno(Long alunoId, Long personalId, String termo, String status, Pageable pageable) {
        validarAluno(alunoId, personalId);
        String busca = termo == null ? "" : termo.trim();
        String filtroStatus = status == null ? "" : status.trim().toUpperCase();
        if (!filtroStatus.isEmpty() && !filtroStatus.equals("RASCUNHO") && !filtroStatus.equals("LIBERADO")) {
            filtroStatus = "";
        }
        if (filtroStatus.isEmpty()) {
            return buscarPaginadoDoAluno(alunoId, personalId, busca, pageable);
        }
        if (busca.isEmpty()) return treinos.findByAlunoIdAndStatusOrderByDataCriacaoDesc(alunoId, filtroStatus, pageable);
        return treinos.findByAlunoIdAndNomeContainingIgnoreCaseAndStatusOrderByDataCriacaoDesc(alunoId, busca, filtroStatus, pageable);
    }

    @Transactional(readOnly = true)
    public Treino buscarPorId(Long treinoId, Long personalId) {
        return treinos.findByIdAndAlunoPersonalId(treinoId, personalId).orElseThrow(() -> new IllegalArgumentException("Treino não encontrado para este personal."));
    }

    @Transactional(readOnly = true)
    public Treino buscarPorIdComAluno(Long id, Long personalId) {
        return treinos.buscarPorIdComAlunoEPersonal(id, personalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Treino não encontrado para este personal."));
    }

    public Treino salvar(Long alunoId, Long personalId, Treino treino) {
        Aluno aluno = validarAluno(alunoId, personalId);
        if (treino.getNome() == null || treino.getNome().isBlank() || treino.getNome().trim().length() > 80) {
            throw new IllegalArgumentException("Nome do treino é obrigatório (até 80 caracteres).");
        }
        if (treino.getId() != null) {
            Treino atual = buscarPorId(treino.getId(), personalId);
            treino.setDataCriacao(atual.getDataCriacao());
            treino.setDataLiberacao(atual.getDataLiberacao());
            treino.setTokenAcesso(atual.getTokenAcesso());
            treino.setAcessoExpiraEm(atual.getAcessoExpiraEm());
            treino.setTotalVisualizacoes(atual.getTotalVisualizacoes());
            treino.setDataUltimaVisualizacao(atual.getDataUltimaVisualizacao());
            if ("LIBERADO".equals(atual.getStatus())) {
                throw new IllegalStateException("Treino liberado não pode ser editado nesta etapa.");
            }
        } else {
            treino.setStatus("RASCUNHO");
        }
        treino.setAluno(aluno);
        treino.setNome(treino.getNome().trim());
        return treinos.save(treino);
    }

    /** Duplica um treino do mesmo aluno, sempre como rascunho sem link público. */
    public Treino duplicar(Long treinoId, Long personalId) {
        Treino origem = buscarPorIdComAluno(treinoId, personalId);

        Treino copia = new Treino();
        copia.setAluno(origem.getAluno());
        String nomeCopia = "Cópia - " + origem.getNome();
        copia.setNome(nomeCopia.length() > 80 ? nomeCopia.substring(0, 80) : nomeCopia);
        copia.setDescricao(origem.getDescricao());
        copia.setStatus("RASCUNHO");
        copia.setTokenAcesso(null);
        copia.setDataLiberacao(null);
        copia.setAcessoExpiraEm(null);
        copia.setTotalVisualizacoes(0);
        copia.setDataUltimaVisualizacao(null);
        copia = treinos.save(copia);

        List<TreinoExercicio> originais = itensTreino.findByTreinoIdOrderByOrdemAsc(origem.getId());
        for (TreinoExercicio original : originais) {
            TreinoExercicio novoItem = new TreinoExercicio();
            novoItem.setTreino(copia);
            novoItem.setExercicio(original.getExercicio());
            novoItem.setOrdem(original.getOrdem());
            novoItem.setSeries(original.getSeries());
            novoItem.setRepeticoes(original.getRepeticoes());
            novoItem.setCarga(original.getCarga());
            novoItem.setDescansoSegundos(original.getDescansoSegundos());
            novoItem.setObservacao(original.getObservacao());
            itensTreino.save(novoItem);
        }
        return copia;
    }

    public Treino liberar(Long treinoId, Long personalId) {
        return liberar(treinoId, personalId, 30);
    }

    public Treino liberar(Long treinoId, Long personalId, Integer validadeDias) {
        Treino t = buscarPorId(treinoId, personalId);
        if (validadeDias == null || validadeDias < 1 || validadeDias > 365) {
            throw new IllegalArgumentException("A validade deve ser entre 1 e 365 dias.");
        }
        if ("LIBERADO".equals(t.getStatus()) && t.getTokenAcesso() != null && t.getAcessoExpiraEm() != null && t.getAcessoExpiraEm().isAfter(LocalDateTime.now())) {
            return t;
        }
        if (t.getNome() == null || t.getNome().isBlank()) {
            throw new IllegalStateException("Informe o nome do treino antes de liberar.");
        }
        LocalDateTime agora = LocalDateTime.now();
        t.setTokenAcesso(UUID.randomUUID().toString());
        t.setDataLiberacao(agora);
        t.setAcessoExpiraEm(agora.plusDays(validadeDias));
        t.setDataRevogacao(null);
        t.setStatus("LIBERADO");
        Treino salvo = treinos.save(t);
        eventosCompartilhamento.save(new EventoCompartilhamento(salvo, "LIBERADO", agora, "Link gerado; validade de " + validadeDias + " dias."));
        return salvo;
    }

    public Treino revogarAcesso(Long treinoId, Long personalId) {
        Treino t = buscarPorId(treinoId, personalId);
        if (t.getTokenAcesso() == null || !"LIBERADO".equals(t.getStatus())) {
            throw new IllegalStateException("Este treino não possui um link ativo para revogar.");
        }
        LocalDateTime agora = LocalDateTime.now();
        t.setStatus("REVOGADO");
        t.setTokenAcesso(null);
        t.setDataRevogacao(agora);
        Treino salvo = treinos.save(t);
        eventosCompartilhamento.save(new EventoCompartilhamento(salvo, "REVOGADO", agora, "Acesso público revogado pelo personal."));
        return salvo;
    }

    @Transactional(readOnly = true)
    public List<EventoCompartilhamento> listarHistoricoCompartilhamento(Long treinoId, Long personalId) {
        buscarPorId(treinoId, personalId);
        return eventosCompartilhamento.findByTreinoIdOrderByDataEventoDesc(treinoId);
    }

    public void excluir(Long treinoId, Long personalId) {
        Treino t = buscarPorId(treinoId, personalId);
        if ("LIBERADO".equals(t.getStatus())) {
            throw new IllegalStateException("Revogue o treino antes de excluí-lo.");
        }
        if (eventosCompartilhamento.existsByTreinoId(treinoId)) {
            throw new IllegalStateException("Este treino possui histórico de compartilhamento e não pode ser excluído para preservar a auditoria.");
        }
        treinos.delete(t);
    }

    private Aluno validarAluno(Long alunoId, Long personalId) {
        return alunos.findByIdAndPersonalId(alunoId, personalId).orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal."));
    }
}
