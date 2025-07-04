package org.example.ai_api.Strategy.FileReader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.tools.PDFText2HTML;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

@Component
@Order(4)
public class PdfReader implements FileReaderStrategy{

    @Override
    public String read(InputStream inputStream) throws Exception {
        // 1. 将 PDF 转为 HTML
        PDDocument document = PDDocument.load(inputStream);
        ByteArrayOutputStream htmlOut = new ByteArrayOutputStream();

        // PDFText2HTML 需要 Writer
        Writer writer = new OutputStreamWriter(htmlOut, StandardCharsets.UTF_8);
        PDFText2HTML stripper = new PDFText2HTML();
        stripper.setAddMoreFormatting(true);
        stripper.writeText(document, writer);
        writer.flush();
        document.close();

        String html = htmlOut.toString(StandardCharsets.UTF_8.name());

        // 2. 将 HTML 转为 Markdown
        com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter converter =
                com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter.builder().build();
        String markdown = converter.convert(html);

        // 3. 按需处理: 去除多余空行，添加页面分隔符
        markdown = markdown.replaceAll("(?m)^[ \t]+$", "");
        markdown = markdown.replaceAll("(?m)^(?=#+ )", "\n");
        return markdown.trim();
    }

    @Override
    public Boolean support(String fileName) {
        return fileName.endsWith(".pdf");
    }
}
