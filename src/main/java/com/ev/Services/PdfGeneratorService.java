package com.ev.Services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfGeneratorService {

    public ByteArrayOutputStream generatePdfFromHtml(String html) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();

        builder.useFastMode(); // optional: improves performance
        builder.withHtmlContent(html, null);
        builder.toStream(baos);
        builder.run();

        return baos;
    }
}
