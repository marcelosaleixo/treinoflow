package com.marceloaleixo.treinoflow.service;

import com.marceloaleixo.treinoflow.dto.DesafioPerformanceView;
import com.marceloaleixo.treinoflow.dto.DesafiosPerformanceDashboardView;
import com.marceloaleixo.treinoflow.entity.DesafioPerformance;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.ResultadoCrm;
import com.marceloaleixo.treinoflow.enums.TipoDesafioPerformance;
import com.marceloaleixo.treinoflow.repository.AlunoRepository;
import com.marceloaleixo.treinoflow.repository.DesafioPerformanceRepository;
import com.marceloaleixo.treinoflow.repository.InteracaoCrmRepository;
import com.marceloaleixo.treinoflow.repository.RegistroTreinoAlunoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class DesafioPerformanceService {
    private final DesafioPerformanceRepository desafios;
    private final AlunoRepository alunos;
    private final RegistroTreinoAlunoRepository registros;
    private final InteracaoCrmRepository interacoes;
    private final MetaComercialService metaComercialService;
    private final UsuarioPersonalService usuarioPersonalService;
    private final GamificacaoService gamificacaoService;

    public DesafioPerformanceService(DesafioPerformanceRepository desafios,
                                     AlunoRepository alunos,
                                     RegistroTreinoAlunoRepository registros,
                                     InteracaoCrmRepository interacoes,
                                     MetaComercialService metaComercialService,
                                     UsuarioPersonalService usuarioPersonalService,
                                     GamificacaoService gamificacaoService) {
        this.desafios = desafios;
        this.alunos = alunos;
        this.registros = registros;
        this.interacoes = interacoes;
        this.metaComercialService = metaComercialService;
        this.usuarioPersonalService = usuarioPersonalService;
        this.gamificacaoService = gamificacaoService;
    }

    @Transactional
    public DesafiosPerformanceDashboardView dashboard(Long personalId) {
        LocalDate mes = LocalDate.now().withDayOfMonth(1);
        UsuarioPersonal personal = usuarioPersonalService.buscarPorId(personalId);
        garantirDesafios(personal, mes);

        var meta = metaComercialService.dashboard(personalId);
        long recuperacoes = meta.recuperacoes();
        long treinos = meta.treinosRealizados();
        long carteira = meta.alunosAtivos();

        List<DesafioPerformance> entidades = desafios.findByPersonalIdAndMesReferenciaOrderByIdAsc(personalId, mes);
        List<DesafioPerformanceView> views = new ArrayList<>();
        int concluidos = 0;
        int bonus = 0;
        int somaProgresso = 0;

        for (DesafioPerformance desafio : entidades) {
            long atual = switch (desafio.getTipo()) {
                case RECUPERADOR -> recuperacoes;
                case CONSISTENCIA -> treinos;
                case CARTEIRA -> carteira;
                case META_TRIPLA -> meta.progressoGeral();
            };
            int progresso = desafio.getMeta() <= 0 ? 100 : Math.min(100, (int) Math.round(atual * 100D / desafio.getMeta()));
            if (progresso >= 100 && desafio.getConcluidoEm() == null) {
                desafio.setConcluidoEm(LocalDateTime.now());
                desafios.save(desafio);
            }
            boolean concluido = desafio.getConcluidoEm() != null;
            if (concluido) {
                concluidos++;
                bonus += desafio.getBonusPontos();
            }
            somaProgresso += progresso;
            String status = concluido ? "CONCLUÍDO" : progresso >= 75 ? "QUASE LÁ" : progresso >= 40 ? "EM ANDAMENTO" : "COMECE AGORA";
            views.add(new DesafioPerformanceView(desafio.getId(), desafio.getTitulo(), desafio.getDescricao(),
                    desafio.getMeta(), atual, progresso, desafio.getBonusPontos(), concluido, status,
                    valorLabel(desafio.getTipo(), atual), valorLabel(desafio.getTipo(), desafio.getMeta())));
        }

        int progressoGeral = entidades.isEmpty() ? 0 : Math.round(somaProgresso / (float) entidades.size());
        var gamificacao = gamificacaoService.dashboard(personalId);
        return new DesafiosPerformanceDashboardView(label(mes), views, concluidos, entidades.size(), bonus,
                progressoGeral, gamificacao.nivel(), gamificacao.pontos());
    }

    private void garantirDesafios(UsuarioPersonal personal, LocalDate mes) {
        criarSeAusente(personal, mes, TipoDesafioPerformance.RECUPERADOR,
                "🔄 Recuperador do mês", "Recupere ou renove a meta de alunos definida para o mês.", 5, 50);
        criarSeAusente(personal, mes, TipoDesafioPerformance.CONSISTENCIA,
                "🏋️ Máquina de consistência", "Conclua a meta mensal de treinos acompanhados.", 80, 40);
        criarSeAusente(personal, mes, TipoDesafioPerformance.CARTEIRA,
                "👥 Carteira forte", "Alcance a meta de alunos ativos do mês.", 20, 30);
        criarSeAusente(personal, mes, TipoDesafioPerformance.META_TRIPLA,
                "🏆 Desafio triplo", "Leve os três indicadores principais do mês a 100%.", 100, 100);
    }

    private void criarSeAusente(UsuarioPersonal personal, LocalDate mes, TipoDesafioPerformance tipo,
                                String titulo, String descricao, int metaPadrao, int bonus) {
        if (desafios.findByPersonalIdAndMesReferenciaAndTipo(personal.getId(), mes, tipo).isPresent()) return;
        var meta = metaComercialService.dashboard(personal.getId());
        int metaReal = switch (tipo) {
            case RECUPERADOR -> meta.metaRecuperacoes();
            case CONSISTENCIA -> meta.metaTreinos();
            case CARTEIRA -> meta.metaAlunosAtivos();
            case META_TRIPLA -> metaPadrao;
        };
        DesafioPerformance d = new DesafioPerformance();
        d.setPersonal(personal);
        d.setMesReferencia(mes);
        d.setTipo(tipo);
        d.setTitulo(titulo);
        d.setDescricao(descricao);
        d.setMeta(metaReal);
        d.setBonusPontos(bonus);
        desafios.save(d);
    }

    private String valorLabel(TipoDesafioPerformance tipo, long valor) {
        return tipo == TipoDesafioPerformance.META_TRIPLA ? valor + "%" : String.valueOf(valor);
    }

    private String label(LocalDate mes) {
        return mes.getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")) + "/" + mes.getYear();
    }
}
