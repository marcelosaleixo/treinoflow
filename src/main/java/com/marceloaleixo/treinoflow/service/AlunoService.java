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

@Service
@Transactional
public class AlunoService {
    @Autowired
    private AlunoRepository alunos;

    @Autowired
    private UsuarioPersonalRepository personais;
    @Transactional(readOnly=true) public List<Aluno> listarPorPersonal(Long personalId) { validarPersonalId(personalId); return alunos.findByPersonalIdOrderByNomeAsc(personalId); }
    @Transactional(readOnly=true)
    public Page<Aluno> listarPaginado(Long personalId, Pageable pageable) {
        return listarPaginado(personalId, null, pageable);
    }

    @Transactional(readOnly=true)
    public Page<Aluno> listarPaginado(Long personalId, String termo, Pageable pageable) {
        validarPersonalId(personalId);
        String filtro = termo == null || termo.isBlank() ? null : termo.trim();
        return alunos.buscarPorPersonalETermo(personalId, filtro, pageable);
    }
    @Transactional(readOnly=true) public Aluno buscarPorId(Long id, Long personalId) { return alunos.findByIdAndPersonalId(id, personalId).orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado para este personal.")); }
    public Aluno salvar(Long personalId, Aluno aluno) {
        validarPersonalId(personalId);
        validarTexto(aluno.getNome(), "Nome do aluno", 120);

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
    public void excluir(Long id, Long personalId) { alunos.delete(buscarPorId(id, personalId)); }
    private void validarPersonalId(Long id){ if(id==null || id<=0) throw new IllegalArgumentException("Personal inválido."); }
    private void validarTexto(String valor,String campo,int max){if(valor==null||valor.isBlank()||valor.trim().length()>max)throw new IllegalArgumentException(campo+" é obrigatório e deve ter até "+max+" caracteres.");}
}
