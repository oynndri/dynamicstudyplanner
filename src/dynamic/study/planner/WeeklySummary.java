package dynamic.study.planner;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class WeeklySummary {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final int userId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<Task> weeklyTasks;
    private final List<StudySession> weeklySessions;
    private final Map<String, Double> subjectsWithTime;
    private final Map<LocalDate, Double> studyTimePerDay;

    public WeeklySummary(int userId, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }

        this.userId = userId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.weeklyTasks = DatabaseHelper.getTasksByDateRange(userId, startDate, endDate);
        this.weeklySessions = DatabaseHelper.getSessionsByDateRange(userId, startDate, endDate);
        this.subjectsWithTime = DatabaseHelper.getStudyTimeBySubject(userId, startDate, endDate);
        this.studyTimePerDay = DatabaseHelper.getDailyStudyTime(userId, startDate, endDate);
    }

    public void exportToPDF() {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // First content stream for initial content
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                // Add title
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Weekly Summary - " + getDateRangeString());
                contentStream.endText();

                // Add summary text
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                String[] summaryLines = getSummaryText().split("\n");
                float yPosition = 700;

                for (String line : summaryLines) {
                    if (yPosition < 50) {
                        // Close current content stream
                        contentStream.close();

                        // Create new page
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);

                        // Create new content stream for new page
                        contentStream = new PDPageContentStream(document, page);

                        // Add continuation header
                        contentStream.beginText();
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                        contentStream.newLineAtOffset(50, 750);
                        contentStream.showText("Weekly Summary - " + getDateRangeString() + " (cont.)");
                        contentStream.endText();

                        yPosition = 700;
                    }

                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText(line);
                    contentStream.endText();
                    yPosition -= 15;
                }

                // Add charts
                PDImageXObject chartImage1 = LosslessFactory.createFromImage(document,
                        createChartImage(createStudyTimePieChart(), 400, 300));
                contentStream.drawImage(chartImage1, 50, yPosition - 350, 250, 200);

                PDImageXObject chartImage2 = LosslessFactory.createFromImage(document,
                        createChartImage(createDailyStudyTimeChart(), 400, 300));
                contentStream.drawImage(chartImage2, 310, yPosition - 350, 250, 200);

            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }

            // Save PDF
            String fileName = "WeeklySummary_" + LocalDate.now().format(FILE_DATE_FORMATTER) + ".pdf";
            document.save(fileName);

            JOptionPane.showMessageDialog(null,
                    "Weekly summary exported successfully as " + fileName,
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

    private BufferedImage createChartImage(JFreeChart chart, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        chart.draw(g2d, new Rectangle(width, height));
        g2d.dispose();
        return image;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(800, 600));
        panel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Weekly Summary - " + getDateRangeString());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Summary text
        JTextArea summaryText = new JTextArea(getSummaryText());
        summaryText.setEditable(false);
        summaryText.setFont(new Font("Arial", Font.PLAIN, 12));
        summaryText.setBackground(Color.WHITE);
        panel.add(new JScrollPane(summaryText), BorderLayout.CENTER);

        // Charts
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        chartsPanel.setBackground(Color.WHITE);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ChartPanel pieChartPanel = new ChartPanel(createStudyTimePieChart());
        pieChartPanel.setPreferredSize(new Dimension(350, 250));

        ChartPanel barChartPanel = new ChartPanel(createDailyStudyTimeChart());
        barChartPanel.setPreferredSize(new Dimension(350, 250));

        chartsPanel.add(pieChartPanel);
        chartsPanel.add(barChartPanel);

        panel.add(chartsPanel, BorderLayout.SOUTH);

        return panel;
    }

    public double getTotalStudyHours() {
        return subjectsWithTime.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public Map<String, Double> getSubjectsWithTime() {
        return new LinkedHashMap<>(subjectsWithTime);
    }

    public Set<String> getTopicsCovered() {
        return weeklyTasks.stream()
                .map(Task::getTopic)
                .filter(Objects::nonNull)
                .filter(topic -> !topic.isEmpty())
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Map<LocalDate, Long> getSessionsPerDay() {
        return weeklySessions.stream()
                .collect(Collectors.groupingBy(
                        StudySession::getDate,
                        TreeMap::new,
                        Collectors.counting()
                ));
    }

    public Map<LocalDate, Double> getStudyTimePerDay() {
        return new TreeMap<>(studyTimePerDay);
    }

    public JFreeChart createStudyTimePieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        getSubjectsWithTime().forEach((subject, hours) -> {
            if (hours > 0) {
                dataset.setValue(subject, hours);
            }
        });

        JFreeChart chart = ChartFactory.createPieChart(
                "Study Time Distribution",
                dataset,
                true, true, false);

        chart.getPlot().setBackgroundPaint(Color.WHITE);
        return chart;
    }

    public JFreeChart createDailyStudyTimeChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        getStudyTimePerDay().forEach((date, hours) ->
                dataset.addValue(hours, "Study Hours", date.format(DATE_FORMATTER)));

        JFreeChart chart = ChartFactory.createBarChart(
                "Daily Study Time",
                "Date",
                "Hours",
                dataset);

        chart.getPlot().setBackgroundPaint(Color.WHITE);
        return chart;
    }

    public String getSummaryText() {
        StringBuilder sb = new StringBuilder();
        sb.append("WEEKLY STUDY SUMMARY\n\n");
        sb.append(String.format("Period: %s\n", getDateRangeString()));
        sb.append(String.format("Total Study Hours: %.1f hours\n", getTotalStudyHours()));
        sb.append(String.format("Total Sessions: %d\n", weeklySessions.size()));
        sb.append(String.format("Total Tasks Completed: %d\n\n", weeklyTasks.stream().filter(Task::isCompleted).count()));

        sb.append("SUBJECTS STUDIED:\n");
        getSubjectsWithTime().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> sb.append(String.format("- %s: %.1f hours\n", entry.getKey(), entry.getValue())));

        sb.append("\nTOPICS COVERED:\n");
        getTopicsCovered().forEach(topic -> sb.append("- ").append(topic).append("\n"));

        sb.append("\nDAILY BREAKDOWN:\n");
        getStudyTimePerDay().forEach((date, hours) ->
                sb.append(String.format("- %s: %.1f hours\n", date.format(DATE_FORMATTER), hours)));

        return sb.toString();
    }

    private String getDateRangeString() {
        return startDate.format(DATE_FORMATTER) + " to " + endDate.format(DATE_FORMATTER);
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public List<Task> getWeeklyTasks() { return Collections.unmodifiableList(weeklyTasks); }
    public List<StudySession> getWeeklySessions() { return Collections.unmodifiableList(weeklySessions); }
}
