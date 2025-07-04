package org.example.ai_api.Strategy.FileReader;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * XLSX文件读取器 - Markdown表格格式输出（优化版）
 */
@Component
@Order(3)
public class XlsxReader implements FileReaderStrategy {

    @Override
    public String read(InputStream inputStream) throws Exception {
        StringBuilder markdownBuilder = new StringBuilder();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            int numberOfSheets = workbook.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                String sheetName = sheet.getSheetName();
                markdownBuilder.append("## ").append(sheetName).append("\n\n");

                // 生成当前 sheet 的 Markdown 表格
                String tableMarkdown = buildSheetTableMarkdown(sheet);
                markdownBuilder.append(tableMarkdown).append("\n");
            }
        }
        return markdownBuilder.toString();
    }

    /**
     * 根据 Sheet 构建 Markdown 格式的表格字符串
     */
    private String buildSheetTableMarkdown(Sheet sheet) {
        StringBuilder builder = new StringBuilder();

        // 1. 确定最大列数（表格宽度）
        int maxColumns = 0;
        for (Row row : sheet) {
            int lastCellNum = row.getLastCellNum();
            if (lastCellNum > maxColumns) {
                maxColumns = lastCellNum;
            }
        }
        if (maxColumns == 0) {
            return "";
        }

        // 2. 存储每列的最大宽度（用于对齐）
        int[] colWidths = new int[maxColumns];
        List<List<String>> tableData = new ArrayList<>();

        // 3. 收集数据和计算列宽
        for (Row row : sheet) {
            List<String> rowData = new ArrayList<>();
            for (int colIdx = 0; colIdx < maxColumns; colIdx++) {
                Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String cellValue = formatCellValue(cell);
                rowData.add(cellValue);
                colWidths[colIdx] = Math.max(colWidths[colIdx], cellValue.length() + 2);
            }
            tableData.add(rowData);
        }

        // 4. 构建 Markdown 表格头
        builder.append("|");
        for (int i = 0; i < maxColumns; i++) {
            builder.append(padCenter(tableData.get(0).get(i), colWidths[i])).append("|");
        }
        builder.append("\n");

        // 5. 添加分隔线
        builder.append("|");
        for (int i = 0; i < maxColumns; i++) {
            builder.append(padCenter("", colWidths[i]).replace(' ', '-')).append("|");
        }
        builder.append("\n");

        // 6. 添加表格主体内容（从第二行开始）
        for (int r = 1; r < tableData.size(); r++) {
            builder.append("|");
            List<String> rowData = tableData.get(r);
            for (int c = 0; c < maxColumns; c++) {
                String value = rowData.get(c);
                boolean isNumeric = !value.isEmpty() && value.matches("^[0-9.,]+$");
                builder.append(isNumeric ? padLeft(value, colWidths[c]) : padRight(value, colWidths[c]))
                        .append("|");
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    private String formatCellValue(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        String text = formatter.formatCellValue(cell);
        return text.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\n", "<br>");
    }

    private String padRight(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        StringBuilder padded = new StringBuilder(s);
        while (padded.length() < width) padded.append(' ');
        return padded.toString();
    }

    private String padLeft(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        StringBuilder padded = new StringBuilder();
        while (padded.length() < width - s.length()) padded.append(' ');
        padded.append(s);
        return padded.toString();
    }

    private String padCenter(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s;
        StringBuilder padded = new StringBuilder();
        int totalPadding = width - s.length();
        int left = totalPadding / 2;
        int right = totalPadding - left;
        while (padded.length() < left) padded.append(' ');
        padded.append(s);
        while (padded.length() < left + s.length() + right) padded.append(' ');
        return padded.toString();
    }

    @Override
    public Boolean support(String fileName) {
        return fileName != null && fileName.toLowerCase().endsWith(".xlsx");
    }
}



