package dynamic.study.planner;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PDFExporter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static void exportComponentToPDF(Component component, String title) {
        try {
            // Create image from component
            BufferedImage img = new BufferedImage(
                    component.getWidth(),
                    component.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            component.paint(img.getGraphics());

            // Create PDF document
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(new PDRectangle(component.getWidth(), component.getHeight()));
            document.addPage(page);

            // Add image to PDF
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(
                    document,
                    ImageUtils.toByteArray(img, "png"),
                    "screenshot");

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, 0, 0);
            }

            // Save PDF
            String fileName = title + "_" + LocalDate.now().format(DATE_FORMATTER) + ".pdf";
            document.save(new File(fileName));
            document.close();

            JOptionPane.showMessageDialog(null,
                    "PDF exported successfully as " + fileName,
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error exporting PDF: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    // Helper class for image conversion
    private static class ImageUtils {
        public static byte[] toByteArray(BufferedImage bi, String format) throws IOException {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(bi, format, baos);
            return baos.toByteArray();
        }
    }
}
