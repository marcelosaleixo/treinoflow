package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.entity.Aluno;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDate;

@Service
@Transactional
public class AlunoService {
    @Autowired
    private AlunoRepository alunos;

    @Autowired
    private UsuarioPersonalRepository personais;
    @Transactional(readOnly=true) public List<Aluno> listarPorPersonal(Long personalId) { validarPersonalId(personalId); return alunos.findByPersonalIdOrderByNomeAsc(personalId); }
    @Transactional(readOnly=true) public List<Aluno> listarAtivosPorPersonal(Long personalId) { validarPersonalId(personalId); return alunos.findByPersonalIdAndStatusOrderByNomeAsc(personalId, "ATIVO"); }
    @Transactional(readOnly=true)
    public Page<Aluno> listarPaginado(Long personalId, Pageable pageable) {
        return listarPaginado(personalId, null, pageable);
    }

    @Transactional(readOnly=true)
    public Page<Aluno> listarPaginado(Long personalId, String termo, Pageable pageable) {
        validarPersonalId(personalId);
        String filtro = termo == null || termo.isBlank() ? null : termo.trim();
        String filtroCpf = filtro == null ? null : filtro.replaceAll("\\D", "");
        if (filtroCpf != null && filtroCpf.isBlank()) filtroCpf = null;
        return alunos.buscarPorPersonalETermo(personalId, filtro, filtroCpf, pageable);
    }
    @Transactional(readOnly=true)
    public Page<Aluno> listarPaginado(Long personalId, String termo, String status, String objetivo, Pageable pageable) {
        validarPersonalId(personalId);
        String filtro = termo == null || termo.isBlank() ? null : termo.trim();
        String filtroCpf = filtro == null ? null : filtro.replaceAll("\\D", "");
        if (filtroCpf != null && filtroCpf.isBlank()) filtroCpf = null;
        String filtroStatus = status == null ? null : status.trim().toUpperCase(java.util.Locale.ROOT);
        if (filtroStatus == null || !java.util.List.of("ATIVO", "INATIVO").contains(filtroStatus)) filtroStatus = null;
        String filtroObjetivo = objetivo == null || objetivo.isBlank() ? null : objetivo.trim();
        return alunos.buscarPorPersonalETermoEFiltros(personalId, filtro, filtroCpf, filtroStatus, filtroObjetivo, pageable);
    }

    @Transactional(readOnly=true)
    public List<Aluno> listarParaExportacao(Long personalId, String termo, String status, String objetivo, Pageable pageable) {
        java.util.List<Aluno> resultado = new java.util.ArrayList<>();
        Page<Aluno> pagina = listarPaginado(personalId, termo, status, objetivo, pageable);
        resultado.addAll(pagina.getContent());
        for (int numero = 1; numero < pagina.getTotalPages(); numero++) {
            resultado.addAll(listarPaginado(personalId, termo, status, objetivo,
                    org.springframework.data.domain.PageRequest.of(numero, pageable.getPageSize(), pageable.getSort())).getContent());
        }
        return resultado;
    }

    @Transactional(readOnly=true) public Aluno buscarPorId(Long id, Long personalId) { return alunos.findByIdAndPersonalId(id, personalId).orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal.")); }
    public Aluno salvar(Long personalId, Aluno aluno) {
        validarPersonalId(personalId);
        validarTexto(aluno.getNome(), "Nome do aluno", 120);
        if (aluno.getDataNascimento() != null && aluno.getDataNascimento().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de nascimento não pode ser futura.");
        }
        if (aluno.getDataInicio() != null && aluno.getDataInicio().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de início não pode ser futura.");
        }
        // CPF opcional; quando informado, persiste somente os 11 dígitos e valida os dígitos verificadores.
        String cpf = aluno.getCpf();
        if (cpf != null && !cpf.isBlank()) {
            cpf = cpf.replaceAll("\\D", "");
            if (!cpfValido(cpf)) {
                throw new IllegalArgumentException("CPF inválido. Confira os 11 dígitos informados.");
            }
            aluno.setCpf(cpf);
        } else {
            aluno.setCpf(null);
        }
        String sexo = aluno.getSexo();
        if (sexo != null && !sexo.isBlank()) {
            sexo = sexo.trim().toUpperCase(java.util.Locale.ROOT);
            if (!List.of("FEMININO", "MASCULINO", "NAO_INFORMADO").contains(sexo)) {
                throw new IllegalArgumentException("Selecione uma opção válida para o sexo do aluno.");
            }
            aluno.setSexo(sexo);
        } else {
            aluno.setSexo(null);
        }

        // Bloqueia a linha do personal durante a validação e gravação. Isso evita
        // que duas requisições simultâneas ultrapassem o limite do plano.
        UsuarioPersonal personal = personais.findByIdParaAtualizacao(personalId)
                .orElseThrow(() -> new IllegalArgumentException("Personal não encontrado."));

        Aluno existente = null;
        if (aluno.getId() != null) {
            existente = buscarPorId(aluno.getId(), personalId);
            aluno.setDataCriacao(existente.getDataCriacao());
        }

        if (aluno.getStatus() == null || aluno.getStatus().isBlank()) {
            aluno.setStatus("ATIVO");
        }
        String status = aluno.getStatus().trim().toUpperCase(java.util.Locale.ROOT);
        if (!List.of("ATIVO", "INATIVO").contains(status)) {
            throw new IllegalArgumentException("Status do aluno inválido.");
        }

        boolean novoAlunoAtivo = "ATIVO".equals(status)
                && (existente == null || !"ATIVO".equalsIgnoreCase(existente.getStatus()));
        if (novoAlunoAtivo) {
            validarLimiteDoPlano(personal);
        }

        aluno.setPersonal(personal);
        aluno.setNome(aluno.getNome().trim());
        aluno.setStatus(status);
        return alunos.save(aluno);
    }

    private void validarLimiteDoPlano(UsuarioPersonal personal) {
        if (personal.getPlano() == null) {
            throw new IllegalArgumentException(
                    "Seu usuário não possui um plano associado. Entre em contato com o administrador.");
        }

        Integer limite = personal.getPlano().getLimiteAlunos();
        if (limite == null || limite < 0) {
            throw new IllegalArgumentException("O limite de alunos do seu plano está configurado incorretamente.");
        }

        long ativos = alunos.countByPersonalIdAndStatus(personal.getId(), "ATIVO");
        if (ativos >= limite) {
            throw new IllegalArgumentException(
                    "Limite de alunos do seu plano atingido (" + limite
                            + "). Inative um aluno ou faça upgrade do plano para cadastrar mais.");
        }
    }
    /**
     * Inativa o aluno em vez de removê-lo fisicamente.
     * Avaliações físicas e outros registros históricos dependem do aluno por FK;
     * preservá-los evita perda de histórico e violações de integridade referencial.
     */
    public void excluir(Long id, Long personalId) {
        Aluno aluno = buscarPorId(id, personalId);
        aluno.setStatus("INATIVO");
        alunos.save(aluno);
    }
    private boolean cpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}") || cpf.chars().distinct().count() == 1) return false;
        int soma = 0;
        for (int i = 0; i < 9; i++) soma += (cpf.charAt(i) - '0') * (10 - i);
        int resto = (soma * 10) % 11;
        int digito1 = resto == 10 ? 0 : resto;
        if (digito1 != cpf.charAt(9) - '0') return false;
        soma = 0;
        for (int i = 0; i < 10; i++) soma += (cpf.charAt(i) - '0') * (11 - i);
        resto = (soma * 10) % 11;
        int digito2 = resto == 10 ? 0 : resto;
        return digito2 == cpf.charAt(10) - '0';
    }

    private void validarPersonalId(Long id){ if(id==null || id<=0) throw new IllegalArgumentException("Personal inválido."); }
    private void validarTexto(String valor,String campo,int max){if(valor==null||valor.isBlank()||valor.trim().length()>max)throw new IllegalArgumentException(campo+" é obrigatório e deve ter até "+max+" caracteres.");}
}
