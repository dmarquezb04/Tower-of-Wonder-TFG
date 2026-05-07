package com.tow.backend.metrics.util;

import com.tow.backend.metrics.entity.PageView;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Clase de utilidad para exportar métricas a formato Excel (.xlsx).
 * Separa la lógica de presentación de la lógica de negocio del servicio.
 */
public class MetricsExcelExporter {

    public static byte[] export(List<PageView> views, List<Map<String, Object>> statsByUrl, List<Map<String, Object>> statsByZona) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            // --- ESTILOS ---
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle rowStyle = createRowStyle(workbook, false);
            CellStyle alternateStyle = createRowStyle(workbook, true);
            CellStyle titleStyle = createTitleStyle(workbook);

            // --- HOJA 1: RESUMEN ---
            Sheet summarySheet = workbook.createSheet("Resumen Ejecutivo");
            createSummarySheet(summarySheet, views.size(), statsByUrl, statsByZona, headerStyle, rowStyle, alternateStyle, titleStyle);

            // --- HOJA 2: DETALLE ---
            Sheet detailSheet = workbook.createSheet("Listado Detallado");
            createDetailSheet(detailSheet, views, headerStyle, rowStyle, alternateStyle);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private static void createSummarySheet(Sheet sheet, int totalVisits, List<Map<String, Object>> statsByUrl, List<Map<String, Object>> statsByZona,
                                         CellStyle headerStyle, CellStyle rowStyle, CellStyle alternateStyle, CellStyle titleStyle) {
        
        // Título
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("INFORME RESUMIDO DE MÉTRICAS");
        titleCell.setCellStyle(titleStyle);

        // Totales
        sheet.createRow(2).createCell(0).setCellValue("Total de Visitas:");
        sheet.getRow(2).createCell(1).setCellValue(totalVisits);
        sheet.createRow(3).createCell(0).setCellValue("Fecha del Informe:");
        sheet.getRow(3).createCell(1).setCellValue(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        // Tabla Top Páginas
        int currentRespRow = 6;
        sheet.createRow(currentRespRow++).createCell(0).setCellValue("TOP PÁGINAS MÁS VISITADAS");
        Row hUrl = sheet.createRow(currentRespRow++);
        createCell(hUrl, 0, "URL", headerStyle);
        createCell(hUrl, 1, "Visitas", headerStyle);
        
        for (int i = 0; i < Math.min(statsByUrl.size(), 10); i++) {
            Row r = sheet.createRow(currentRespRow++);
            CellStyle style = (i % 2 == 0) ? rowStyle : alternateStyle;
            Map<String, Object> data = statsByUrl.get(i);
            createCell(r, 0, data.get("url").toString(), style);
            createCell(r, 1, data.get("visitas").toString(), style);
        }

        // Tabla Top Zonas
        currentRespRow += 2;
        sheet.createRow(currentRespRow++).createCell(0).setCellValue("TOP ZONAS / PAÍSES");
        Row hZona = sheet.createRow(currentRespRow++);
        createCell(hZona, 0, "Zona", headerStyle);
        createCell(hZona, 1, "Visitas", headerStyle);
        
        for (int i = 0; i < Math.min(statsByZona.size(), 10); i++) {
            Row r = sheet.createRow(currentRespRow++);
            CellStyle style = (i % 2 == 0) ? rowStyle : alternateStyle;
            Map<String, Object> data = statsByZona.get(i);
            createCell(r, 0, data.get("zona").toString(), style);
            createCell(r, 1, data.get("visitas").toString(), style);
        }
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private static void createDetailSheet(Sheet sheet, List<PageView> views, CellStyle headerStyle, CellStyle rowStyle, CellStyle alternateStyle) {
        sheet.createFreezePane(0, 1);

        String[] headers = { "ID", "Fecha de Visita", "URL Visitada", "Dirección IP", "Zona / País", "Navegador" };
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            createCell(headerRow, i, headers[i], headerStyle);
        }

        int rowIdx = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        for (PageView view : views) {
            Row row = sheet.createRow(rowIdx++);
            CellStyle currentStyle = (rowIdx % 2 == 0) ? alternateStyle : rowStyle;

            createCell(row, 0, view.getId().toString(), currentStyle);
            createCell(row, 1, view.getFecha().format(formatter), currentStyle);
            createCell(row, 2, view.getUrl(), currentStyle);
            createCell(row, 3, view.getIp(), currentStyle);
            createCell(row, 4, view.getZona(), currentStyle);
            createCell(row, 5, view.getNavegador(), currentStyle);
        }

        sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, views.size(), 0, headers.length - 1));
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
        }
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private static CellStyle createRowStyle(Workbook workbook, boolean alternate) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        if (alternate) {
            style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        return style;
    }

    private static CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFont(font);
        return style;
    }

    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
}
