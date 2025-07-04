package org.example.ai_api.Strategy.FileReader;

import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * DOCX文件读取器
 */
@Component
@Order(1)
public class DocxReader implements FileReaderStrategy{
    @Override
    public String read(InputStream inputStream) throws Exception {
        StringBuilder contentBuilder = new StringBuilder();
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
//            document.getParagraphs().forEach(p ->
//                    contentBuilder.append(p.getText()).append("\n"));
            // 按文档实际顺序遍历所有内容元素
            for (IBodyElement element : document.getBodyElements()) {
                processBodyElement(element, contentBuilder);
            }
            // 页眉页脚（这些通常在特定位置，单独处理）
            processHeadersAndFooters(document, contentBuilder);
        }
        return contentBuilder.toString().trim();
    }

    // 处理单个文档元素（按实际出现顺序）
    private static void processBodyElement(IBodyElement element, StringBuilder contentBuilder) {
        if (element instanceof XWPFParagraph) {
            XWPFParagraph para = (XWPFParagraph) element;
            contentBuilder.append(para.getText()).append("\n\n");  // 双换行分隔段落
        } else if (element instanceof XWPFTable) {
            XWPFTable table = (XWPFTable) element;
            processTable(table, contentBuilder);
            contentBuilder.append("\n\n");  // 表格后也添加空行
        }
    }

    // 处理表格（支持嵌套）
    private static void processTable(XWPFTable table, StringBuilder contentBuilder) {
        contentBuilder.append("[TABLE START]\n");
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                contentBuilder.append(cell.getText()).append("\t");
                // 处理单元格内的嵌套元素（重要！）
                for (IBodyElement cellElement : cell.getBodyElements()) {
                    if (cellElement instanceof XWPFParagraph) {
                        contentBuilder.append(((XWPFParagraph) cellElement).getText());
                    } else if (cellElement instanceof XWPFTable) {
                        processTable((XWPFTable) cellElement, contentBuilder);
                    }
                }
            }
            contentBuilder.append("\n");  // 行结束
        }
        contentBuilder.append("[TABLE END]");
    }

    // 处理页眉页脚
    private static void processHeadersAndFooters(XWPFDocument doc, StringBuilder contentBuilder) {
        contentBuilder.append("\n--- HEADERS ---\n");
        for (XWPFHeader header : doc.getHeaderList()) {
            for (IBodyElement element : header.getBodyElements()) {
                processBodyElement(element, contentBuilder);
            }
        }
        contentBuilder.append("\n--- FOOTERS ---\n");
        for (XWPFFooter footer : doc.getFooterList()) {
            for (IBodyElement element : footer.getBodyElements()) {
                processBodyElement(element, contentBuilder);
            }
        }
    }

    @Override
    public Boolean support(String fileName) {
        return fileName.endsWith(".docx");
    }
}
