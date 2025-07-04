package org.example.ai_api.Strategy.FileReader;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * PPTX文件读取器
 */
@Component
@Order(2)
public class PptxReader implements FileReaderStrategy{

    @Override
    public String read(InputStream inputStream) throws Exception {
        StringBuilder contentBuilder = new StringBuilder();
        try (XMLSlideShow ppt = new XMLSlideShow(inputStream)) {
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape textShape = (XSLFTextShape) shape;
                        contentBuilder.append(textShape.getText()).append("\n");
                    }
                }
            }
        }
        return contentBuilder.toString().trim();
    }

    @Override
    public Boolean support(String fileName) {
        return fileName.endsWith(".pptx");
    }
}
