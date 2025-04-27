package dynamic.study.planner;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import java.io.File;
import java.io.IOException;

public class PDFGenerator {
    public static void main(String[] args) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            // ফন্ট সেটআপ (PDFBox 3.x)
            PDFont boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont normalFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // কন্টেন্ট স্ট্রিম
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(boldFont, 12);
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("বোল্ড টেক্সট");
                contentStream.endText();
            }

            // ইমেজ যোগ (যদি প্রয়োজন)
            PDImageXObject image = PDImageXObject.createFromFile("logo.png", document);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.drawImage(image, 100, 600, 100, 100);
            }

            document.save("output.pdf");
        }
    }
}
