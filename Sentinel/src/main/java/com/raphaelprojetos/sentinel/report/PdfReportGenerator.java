package com.raphaelprojetos.sentinel.report;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.raphaelprojetos.sentinel.dao.AlertaDAO;
import com.raphaelprojetos.sentinel.dto.AlertaDTO;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PdfReportGenerator {

    private final AlertaDAO alertaDAO;

    public PdfReportGenerator() {
        this.alertaDAO = new AlertaDAO();
    }

    public void gerarRelatorioPDF(int limiteDeAlertas) {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar relatório em PDF");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("relatorio_alertas.pdf")); // Nome padrão

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("Operação cancelada pelo usuário.");
            return;
        }

        File arquivoSelecionado = fileChooser.getSelectedFile();
        String caminhoArquivo = arquivoSelecionado.getAbsolutePath();


        if (!caminhoArquivo.endsWith(".pdf")) {
            caminhoArquivo += ".pdf";
        }


        List<AlertaDTO> alertas = alertaDAO.buscarUltimosAlertas(limiteDeAlertas);

        if (alertas.isEmpty()) {
            System.out.println("Nenhum alerta encontrado para gerar o relatório.");
            return;
        }

        try {

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(caminhoArquivo));
            document.open();


            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Últimos alertas da brigada", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(4); // 4 colunas
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            table.setWidths(new float[]{1f, 3f, 2f, 5f});

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            table.addCell(new Phrase("Código", headerFont));
            table.addCell(new Phrase("Título", headerFont));
            table.addCell(new Phrase("Data e Hora", headerFont));
            table.addCell(new Phrase("Descrição", headerFont));

            for (AlertaDTO alerta : alertas) {
                table.addCell(alerta.getCodigo());
                table.addCell(alerta.getTitulo());
                table.addCell(alerta.getTempoFormatado());
                table.addCell(alerta.getDescricao());
            }

            document.add(table);

            document.close();

            System.out.println("Relatório PDF gerado com sucesso: " + caminhoArquivo);
        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório em PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}