package dynamic.study.planner;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class StudySession {
    private int id;
    private LocalDate date;
    private String subject;
    private String courseCode;
    private LocalTime startTime;
    private LocalTime endTime;
    private String topics;

    // Default constructor
    public StudySession() {
    }

    // Constructor with parameters
    public StudySession(LocalDate date, String subject, String courseCode,
                        LocalTime startTime, LocalTime endTime, String topics) {
        this.date = date;
        this.subject = subject;
        this.courseCode = courseCode;
        this.startTime = startTime;
        this.endTime = endTime;
        this.topics = topics;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    // Utility method to get duration in hours
    public double getDurationInHours() {
        return java.time.Duration.between(startTime, endTime).toMinutes() / 60.0;
    }

    // Override toString for better display
    @Override
    public String toString() {
        return String.format("%s (%s) - %s to %s | Topics: %s",
                subject,
                courseCode != null ? courseCode : "N/A",
                startTime.toString(),
                endTime.toString(),
                topics.length() > 20 ? topics.substring(0, 20) + "..." : topics);
    }

    // Helper method to check if session is upcoming
    public boolean isUpcoming() {
        LocalDate today = LocalDate.now();
        return date.isAfter(today) || date.isEqual(today);
    }

    // Helper method to get days until session
    public long getDaysUntil() {
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }

    // Format for display in lists
    public String getDisplayString() {
        String dayInfo;
        long daysUntil = getDaysUntil();

        if (daysUntil == 0) {
            dayInfo = "Today";
        } else if (daysUntil == 1) {
            dayInfo = "Tomorrow";
        } else {
            dayInfo = "In " + daysUntil + " days";
        }

        return String.format("%s - %s (%s) %s-%s | %s",
                date.format(DateTimeFormatter.ofPattern("MMM dd")),
                subject,
                courseCode != null ? courseCode : "",
                startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                dayInfo);
    }
}
