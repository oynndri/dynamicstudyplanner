package dynamic.study.planner;

import java.time.LocalDate;
import java.time.LocalTime;

public class Task {
    private int id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String subject;
    private String topic;
    private String priority; // High, Medium, Low
    private boolean completed;

    // Constructors
    public Task() {}

    public Task(LocalDate date, LocalTime startTime, LocalTime endTime,
                String subject, String topic, String priority) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.subject = subject;
        this.topic = topic;
        this.priority = priority;
        this.completed = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
