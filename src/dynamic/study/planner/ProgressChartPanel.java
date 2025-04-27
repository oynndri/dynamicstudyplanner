package dynamic.study.planner;

import org.jfree.chart.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Day;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ProgressChartPanel {

    public static JPanel createTaskProgressChart(List<Task> tasks, int userId) {
        // Get task statistics
        Map<String, Long> stats = DatabaseHelper.getTaskStatsBySubject(userId);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        stats.forEach((key, value) -> {
            if (key.endsWith("_completed")) {
                String subject = key.replace("_completed", "");
                dataset.addValue(value, "Completed", subject);
            } else if (key.endsWith("_pending")) {
                String subject = key.replace("_pending", "");
                dataset.addValue(value, "Pending", subject);
            }
        });

        JFreeChart chart = ChartFactory.createBarChart(
                "All Time Task Overview",
                "Subject",
                "Number of Tasks",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Customize chart appearance
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.getRenderer().setSeriesPaint(0, new Color(76, 175, 80));  // Green for completed
        plot.getRenderer().setSeriesPaint(1, new Color(244, 67, 54));  // Red for pending

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 400));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return chartPanel;
    }

    public static JPanel createSessionProgressChart(List<StudySession> sessions, int userId) {
        // Get session counts by date
        Map<LocalDate, Long> sessionCounts = DatabaseHelper.getSessionCountByDate(userId);

        TimeSeries series = new TimeSeries("Study Sessions");
        sessionCounts.forEach((date, count) ->
                series.add(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), count));

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Upcoming Sessions Overview",
                "Date",
                "Number of Sessions",
                dataset,
                true,
                true,
                false
        );

        // Customize chart appearance
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.getRenderer().setSeriesPaint(0, new Color(40, 53, 147)); // Use primary color

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 400));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return chartPanel;
    }
}
