package com.marceloaleixo.treinoflow.config;

import com.marceloaleixo.treinoflow.entity.Exercicio;
import com.marceloaleixo.treinoflow.entity.Plano;
import com.marceloaleixo.treinoflow.entity.UsuarioPersonal;
import com.marceloaleixo.treinoflow.enums.GrupoMuscular;
import com.marceloaleixo.treinoflow.repository.ExercicioRepository;
import com.marceloaleixo.treinoflow.repository.PlanoRepository;
import com.marceloaleixo.treinoflow.repository.UsuarioPersonalRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.CommandLineRunner;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(1)
public class TreinoFlowDataInitializer implements CommandLineRunner {

    private final PlanoRepository planos;
    private final UsuarioPersonalRepository personais;
    private final ExercicioRepository exercicios;

    public TreinoFlowDataInitializer(PlanoRepository planos,
                                     UsuarioPersonalRepository personais,
                                     ExercicioRepository exercicios) {
        this.planos = planos;
        this.personais = personais;
        this.exercicios = exercicios;
    }

    @Override
    @Transactional
    public void run(String... args) {
        garantirPlanos();
        garantirPlanoPadrao();
        atribuirPlanoAQuemNaoPossui();
        garantirExerciciosGlobais();
    }

    private void garantirPlanos() {
        if (planos.count() > 0) return;

        criarPlano("TreinoFlow Start", "Para personal trainers que estão começando.", "29.90", 10, true);
        criarPlano("TreinoFlow Pro", "Gestão completa para uma carteira profissional maior.", "59.90", 30, false);
        criarPlano("TreinoFlow Premium", "Para personal trainers com uma carteira de alunos maior.", "99.90", 100, false);
    }

    private void criarPlano(String nome, String descricao, String valor, int limite, boolean padrao) {
        Plano plano = new Plano();
        plano.setNome(nome);
        plano.setDescricao(descricao);
        plano.setValorMensal(new BigDecimal(valor));
        plano.setLimiteAlunos(limite);
        plano.setAtivo(true);
        plano.setPadrao(padrao);
        planos.save(plano);
    }

    private void garantirPlanoPadrao() {
        if (planos.findFirstByPadraoTrue().filter(Plano::isAtivo).isPresent()) return;

        Plano padrao = planos.findFirstByAtivoTrueOrderByIdAsc().orElse(null);
        if (padrao == null) return;

        planos.findAll().forEach(plano -> {
            boolean deveSerPadrao = plano.getId().equals(padrao.getId());
            if (plano.isPadrao() != deveSerPadrao) {
                plano.setPadrao(deveSerPadrao);
                planos.save(plano);
            }
        });
    }

    private void atribuirPlanoAQuemNaoPossui() {
        Plano padrao = planos.findFirstByPadraoTrue().orElse(null);
        if (padrao == null || !padrao.isAtivo()) return;

        for (UsuarioPersonal personal : personais.findByPerfilAndPlanoIsNull("PERSONAL")) {
            personal.setPlano(padrao);
            personais.save(personal);
        }
    }

    private void garantirExerciciosGlobais() {
        List<DadosExercicio> catalogo = List.of(
                e("Supino reto com barra", GrupoMuscular.PEITO),
                e("Supino inclinado com halteres", GrupoMuscular.PEITO),
                e("Crucifixo com halteres", GrupoMuscular.PEITO),
                e("Crossover na polia", GrupoMuscular.PEITO),
                e("Flexão de braços", GrupoMuscular.PEITO),
                e("Puxada frontal", GrupoMuscular.COSTAS),
                e("Remada baixa", GrupoMuscular.COSTAS),
                e("Remada curvada com barra", GrupoMuscular.COSTAS),
                e("Remada unilateral com halter", GrupoMuscular.COSTAS),
                e("Barra fixa", GrupoMuscular.COSTAS),
                e("Pulldown na polia", GrupoMuscular.COSTAS),
                e("Desenvolvimento com halteres", GrupoMuscular.OMBROS),
                e("Elevação lateral", GrupoMuscular.OMBROS),
                e("Elevação frontal", GrupoMuscular.OMBROS),
                e("Crucifixo inverso", GrupoMuscular.OMBROS),
                e("Encolhimento com halteres", GrupoMuscular.TRAPEZIO),
                e("Rosca direta com barra", GrupoMuscular.BICEPS),
                e("Rosca alternada com halteres", GrupoMuscular.BICEPS),
                e("Rosca martelo", GrupoMuscular.BICEPS),
                e("Rosca Scott", GrupoMuscular.BICEPS),
                e("Tríceps na polia com corda", GrupoMuscular.TRICEPS),
                e("Tríceps testa", GrupoMuscular.TRICEPS),
                e("Tríceps francês", GrupoMuscular.TRICEPS),
                e("Tríceps banco", GrupoMuscular.TRICEPS),
                e("Agachamento livre", GrupoMuscular.QUADRICEPS),
                e("Agachamento no Smith", GrupoMuscular.QUADRICEPS),
                e("Leg press 45 graus", GrupoMuscular.QUADRICEPS),
                e("Cadeira extensora", GrupoMuscular.QUADRICEPS),
                e("Afundo com halteres", GrupoMuscular.QUADRICEPS),
                e("Passada", GrupoMuscular.QUADRICEPS),
                e("Stiff com barra", GrupoMuscular.POSTERIORES_COXA),
                e("Mesa flexora", GrupoMuscular.POSTERIORES_COXA),
                e("Cadeira flexora", GrupoMuscular.POSTERIORES_COXA),
                e("Levantamento terra romeno", GrupoMuscular.POSTERIORES_COXA),
                e("Elevação pélvica", GrupoMuscular.GLUTEOS),
                e("Coice na polia", GrupoMuscular.GLUTEOS),
                e("Abdução de quadril na polia", GrupoMuscular.ABDUTORES),
                e("Cadeira abdutora", GrupoMuscular.ABDUTORES),
                e("Cadeira adutora", GrupoMuscular.ADUTORES),
                e("Panturrilha em pé", GrupoMuscular.PANTURRILHAS),
                e("Panturrilha sentada", GrupoMuscular.PANTURRILHAS),
                e("Abdominal supra", GrupoMuscular.ABDOMEN),
                e("Abdominal infra", GrupoMuscular.ABDOMEN),
                e("Prancha", GrupoMuscular.ABDOMEN),
                e("Prancha lateral", GrupoMuscular.ABDOMEN),
                e("Bird dog", GrupoMuscular.CORPO_TODO),
                e("Dead bug", GrupoMuscular.ABDOMEN),
                e("Extensão lombar no banco", GrupoMuscular.LOMBAR),
                e("Agachamento goblet", GrupoMuscular.QUADRICEPS),
                e("Kettlebell swing", GrupoMuscular.CORPO_TODO)
        );

        for (DadosExercicio dados : catalogo) {
            if (exercicios.existsGlobalByNome(dados.nome())) continue;
            Exercicio exercicio = new Exercicio();
            exercicio.setNome(dados.nome());
            exercicio.setGrupoMuscular(dados.grupo());
            exercicio.setDescricao("Exercício global disponível na biblioteca do TreinoFlow.");
            exercicios.save(exercicio);
        }
    }

    private DadosExercicio e(String nome, GrupoMuscular grupo) {
        return new DadosExercicio(nome, grupo);
    }

    private record DadosExercicio(String nome, GrupoMuscular grupo) {}
}
