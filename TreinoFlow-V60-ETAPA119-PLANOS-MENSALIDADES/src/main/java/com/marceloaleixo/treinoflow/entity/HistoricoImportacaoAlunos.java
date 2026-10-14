package com.marceloaleixo.treinoflow.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historico_importacao_alunos",
       indexes = @Index(name = "idx_hist_importacao_personal_data", columnList = "personal_id,data_importacao"))
public class HistoricoImportacaoAlunos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "personal_id", nullable = false)
    private UsuarioPersonal personal;

    @Column(name = "nome_arquivo", nullable = false, length = 255)
    private String nomeArquivo;

    @Column(name = "data_importacao", nullable = false)
    private LocalDateTime dataImportacao;

    @Column(name = "linhas_analisadas", nullable = false)
    private int linhasAnalisadas;

    @Column(name = "importados", nullable = false)
    private int importados;

    @Column(name = "ignorados", nullable = false)
    private int ignorados;

    @Column(name = "rejeitados", nullable = false)
    private int rejeitados;

    @Column(name = "relatorio_csv", nullable = false, columnDefinition = "TEXT")
    private String relatorioCsv;

    @PrePersist
    void prePersist() {
        if (dataImportacao == null) dataImportacao = LocalDateTime.now();
        if (nomeArquivo == null || nomeArquivo.isBlank()) nomeArquivo = "importacao.csv";
        if (relatorioCsv == null) relatorioCsv = "";
    }

    public Long getId() { return id; }
    public UsuarioPersonal getPersonal() { return personal; }
    public void setPersonal(UsuarioPersonal personal) { this.personal = personal; }
    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }
    public LocalDateTime getDataImportacao() { return dataImportacao; }
    public int getLinhasAnalisadas() { return linhasAnalisadas; }
    public void setLinhasAnalisadas(int linhasAnalisadas) { this.linhasAnalisadas = linhasAnalisadas; }
    public int getImportados() { return importados; }
    public void setImportados(int importados) { this.importados = importados; }
    public int getIgnorados() { return ignorados; }
    public void setIgnorados(int ignorados) { this.ignorados = ignorados; }
    public int getRejeitados() { return rejeitados; }
    public void setRejeitados(int rejeitados) { this.rejeitados = rejeitados; }
    public String getRelatorioCsv() { return relatorioCsv; }
    public void setRelatorioCsv(String relatorioCsv) { this.relatorioCsv = relatorioCsv; }
}
