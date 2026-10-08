package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Agendamento;
import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.AgendamentoRepository;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class AgendamentoService {
    private static final List<String> STATUS = List.of("AGENDADO", "CONFIRMADO", "REALIZADO", "CANCELADO", "FALTOU");
    private static final List<String> TIPOS = List.of("TREINO", "AVALIACAO", "CONSULTA", "OUTRO");

    private final AgendamentoRepository agendamentos;
    private final AlunoRepository alunos;
    private final UsuarioPersonalRepository personais;

    public AgendamentoService(AgendamentoRepository agendamentos,
                              AlunoRepository alunos,
                              UsuarioPersonalRepository personais) {
        this.agendamentos = agendamentos;
        this.alunos = alunos;
        this.personais = personais;
    }

    @Transactional(readOnly = true)
    public List<Agendamento> listarDoDia(Long personalId, LocalDate data) {
        validarPersonal(personalId);
        LocalDate dia = data == null ? LocalDate.now() : data;
        return agendamentos.buscarDoDia(personalId, dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
    }

    @Transactional(readOnly = true)
    public Agendamento buscar(Long id, Long personalId) {
        validarPersonal(personalId);
        return agendamentos.findByIdAndPersonalId(id, personalId)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado para este personal."));
    }

    public Agendamento salvar(Long personalId, Agendamento form) {
        validarPersonal(personalId);
        if (form.getAluno() == null || form.getAluno().getId() == null) {
            throw new IllegalArgumentException("Selecione um aluno.");
        }
        if (form.getInicio() == null || form.getFim() == null) {
            throw new IllegalArgumentException("Informe início e fim do agendamento.");
        }
        if (!form.getFim().isAfter(form.getInicio())) {
            throw new IllegalArgumentException("O horário final deve ser posterior ao horário inicial.");
        }
        if (form.getInicio().toLocalDate().isBefore(LocalDate.now().minusDays(365))) {
            throw new IllegalArgumentException("A data do agendamento está muito distante no passado.");
        }

        UsuarioPersonal personal = personais.findById(personalId)
                .orElseThrow(() -> new IllegalArgumentException("Personal não encontrado."));
        Aluno aluno = alunos.findByIdAndPersonalId(form.getAluno().getId(), personalId)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal."));

        Agendamento existente = null;
        if (form.getId() != null) {
            existente = buscar(form.getId(), personalId);
        }

        String status = normalizar(form.getStatus(), "AGENDADO");
        String tipo = normalizar(form.getTipo(), "TREINO");
        if (!STATUS.contains(status)) throw new IllegalArgumentException("Status do agendamento inválido.");
        if (!TIPOS.contains(tipo)) throw new IllegalArgumentException("Tipo do agendamento inválido.");

        if (!"CANCELADO".equals(status) && agendamentos.existeConflito(personalId, form.getInicio(), form.getFim(), form.getId())) {
            throw new IllegalArgumentException("Existe outro agendamento neste intervalo de horário.");
        }

        if (existente != null) {
            existente.setAluno(aluno);
            existente.setInicio(form.getInicio());
            existente.setFim(form.getFim());
            existente.setStatus(status);
            existente.setTipo(tipo);
            existente.setObservacoes(normalizarObservacao(form.getObservacoes()));
            return agendamentos.save(existente);
        }

        form.setPersonal(personal);
        form.setAluno(aluno);
        form.setStatus(status);
        form.setTipo(tipo);
        form.setObservacoes(normalizarObservacao(form.getObservacoes()));
        return agendamentos.save(form);
    }

    public void alterarStatus(Long id, Long personalId, String status) {
        Agendamento agendamento = buscar(id, personalId);
        String novoStatus = normalizar(status, null);
        if (!STATUS.contains(novoStatus)) throw new IllegalArgumentException("Status do agendamento inválido.");
        if (("REALIZADO".equals(novoStatus) || "FALTOU".equals(novoStatus))
                && (agendamento.getInicio() == null || agendamento.getInicio().isAfter(LocalDateTime.now()))) {
            throw new IllegalArgumentException("O agendamento precisa ter iniciado para registrar presença ou falta.");
        }
        if ("FALTOU".equals(novoStatus) && "CANCELADO".equals(agendamento.getStatus())) {
            throw new IllegalArgumentException("Um agendamento cancelado não pode ser marcado como falta.");
        }
        agendamento.setStatus(novoStatus);
        agendamentos.save(agendamento);
    }

    public void excluir(Long id, Long personalId) {
        agendamentos.delete(buscar(id, personalId));
    }

    public LocalDateTime inicioPadrao(LocalDate data) {
        return (data == null ? LocalDate.now() : data).atTime(LocalTime.of(8, 0));
    }

    public LocalDateTime fimPadrao(LocalDate data) {
        return (data == null ? LocalDate.now() : data).atTime(LocalTime.of(9, 0));
    }

    public List<String> statusDisponiveis() { return STATUS; }
    public List<String> tiposDisponiveis() { return TIPOS; }

    private String normalizar(String valor, String padrao) {
        if (valor == null || valor.isBlank()) return padrao;
        return valor.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizarObservacao(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private void validarPersonal(Long personalId) {
        if (personalId == null || personalId <= 0) throw new IllegalArgumentException("Personal inválido.");
    }
}
