package dynamic.study.planner;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.*;
import org.jfree.data.time.*;
import org.jfree.data.time.Day;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProgressChartPanel {

    public static JPanel createTaskProgressChart(List<Task> tasks, int userId) {
        // Group tasks by subject and count completed vs pending
        Map<String, Long> completedBySubject = tasks.stream()
                .filter(Task::isCompleted)
                .collect(Collectors.groupingBy(Task::getSubject, Collectors.counting()));

        Map<String, Long> pendingBySubject = tasks.stream()
                .filter(task -> !task.isCompleted())
                .collect(Collectors.groupingBy(Task::getSubject, Collectors.counting()));

        // Create dataset for bar chart
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Add completed tasks
        completedBySubject.forEach((subject, count) ->
                dataset.addValue(count, "Completed", subject));

        // Add pending tasks
        pendingBySubject.forEach((subject, count) ->
                dataset.addValue(count, "Pending", subject));

        // Create the bar chart
        JFreeChart chart = ChartFactory.createBarChart(
                "All Time Schedule Overview", // Title
                "Subject",                    // X-axis Label
                "Number of Tasks",            // Y-axis Label
                dataset,                      // Dataset
                PlotOrientation.VERTICAL,     // Orientation
                true,                         // Show legend
                true,                         // Show tooltips
                false                         // URLs
        );

        // Customize the chart
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Return the chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        return chartPanel;
    }

    public static JPanel createSessionProgressChart(List<StudySession> sessions, int userId) {
        // Group sessions by date and count
        Map<LocalDate, Long> sessionsByDate = sessions.stream()
                .collect(Collectors.groupingBy(StudySession::getDate, Collectors.counting()));

        // Create dataset for time series chart
        TimeSeries series = new TimeSeries("Study Sessions");

        // Add data points
        sessionsByDate.forEach((date, count) ->
                series.add(new Day(date.getDayOfMonth(), date.getMonthValue(), date.getYear()), count));

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(series);

        // Create the line chart
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                "Upcoming Sessions All Time Overview", // Title
                "Date",                               // X-axis Label
                "Number of Sessions",                 // Y-axis Label
                dataset,                              // Dataset
                true,                                 // Show legend
                true,                                 // Show tooltips
                false                                 // URLs
        );

        // Customize the chart
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Return the chart panel
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        return chartPanel;
    }
}
