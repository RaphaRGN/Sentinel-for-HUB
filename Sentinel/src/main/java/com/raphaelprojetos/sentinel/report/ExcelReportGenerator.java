package com.raphaelprojetos.sentinel.report;

import com.raphaelprojetos.sentinel.dao.AlertaDAO;
import com.raphaelprojetos.sentinel.dto.AlertaDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class ExcelReportGenerator {


    private final AlertaDAO alertaDAO;

    public ExcelReportGenerator() {
        this.alertaDAO = new AlertaDAO();
    }

    public void gerarRelatorioExcel(int limiteDeAlertas) {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar relatório em Excel");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File("relatorio_alertas.xlsx")); // Nome padrão

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection != JFileChooser.APPROVE_OPTION) {
            System.out.println("Operação cancelada pelo usuário.");
            return;
        }

        File arquivoSelecionado = fileChooser.getSelectedFile();
        String caminhoArquivo = arquivoSelecionado.getAbsolutePath();

        if (!caminhoArquivo.endsWith(".xlsx")) {
            caminhoArquivo += ".xlsx";
        }

        List<AlertaDTO> alertas = alertaDAO.buscarUltimosAlertas(limiteDeAlertas);

        if (alertas.isEmpty()) {
            System.out.println("Nenhum alerta encontrado para gerar o relatório.");
            return;
        }

        try (Workbook workbook = new XSSFWorkbook() {
        }) {
            Sheet sheet = workbook.createSheet("Alertas Recentes");

            Row headerRow = sheet.createRow(0);
            String[] colunas = {"Código", "Título", "Data e Hora", "Descrição"};
            for (int i = 0; i < colunas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(colunas[i]);
                cell.setCellStyle(criarEstiloCabecalho(workbook));
            }

            int rowNum = 1;
            for (AlertaDTO alerta : alertas) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(alerta.getCodigo());
                row.createCell(1).setCellValue(alerta.getTitulo());
                row.createCell(2).setCellValue(alerta.getTempoFormatado());
                row.createCell(3).setCellValue(alerta.getDescricao());
            }

            for (int i = 0; i < colunas.length; i++) {
                sheet.autoSizeColumn(i);
            }


            try (FileOutputStream fileOut = new FileOutputStream(caminhoArquivo)) {
                workbook.write(fileOut);
                System.out.println("Relatório Excel gerado com sucesso: " + caminhoArquivo);
            }

        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório em Excel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private CellStyle criarEstiloCabecalho(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}