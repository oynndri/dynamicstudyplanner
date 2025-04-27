package dynamic.study.planner;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseHelper {
    private static final Logger logger = Logger.getLogger(DatabaseHelper.class.getName());

    // MySQL configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/studyplanner";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    // Initialize database and tables
    public static void initialize() {
        createTaskTable();
        createStudySessionsTable();
    }

    private static void createTaskTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS tasks (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "date DATE NOT NULL, " +
                "start_time TIME NOT NULL, " +
                "end_time TIME NOT NULL, " +
                "subject VARCHAR(100) NOT NULL, " +
                "topic VARCHAR(100) NOT NULL, " +
                "priority ENUM('High','Medium','Low') NOT NULL, " +
                "completed BOOLEAN NOT NULL DEFAULT FALSE, " +
                "user_id INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";

        String[] indexSQLs = {
                "CREATE INDEX IF NOT EXISTS idx_date ON tasks(date)",
                "CREATE INDEX IF NOT EXISTS idx_priority ON tasks(priority)",
                "CREATE INDEX IF NOT EXISTS idx_completed ON tasks(completed)",
                "CREATE INDEX IF NOT EXISTS idx_subject ON tasks(subject)",
                "CREATE INDEX IF NOT EXISTS idx_user_id ON tasks(user_id)"
        };

        executeTableCreation(createTableSQL, indexSQLs, "tasks");
    }

    private static void createStudySessionsTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS study_sessions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "date DATE NOT NULL, " +
                "subject VARCHAR(100) NOT NULL, " +
                "course_code VARCHAR(50), " +
                "start_time TIME NOT NULL, " +
                "end_time TIME NOT NULL, " +
                "topics TEXT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP)";

        String[] indexSQLs = {
                "CREATE INDEX IF NOT EXISTS idx_session_date ON study_sessions(date)",
                "CREATE INDEX IF NOT EXISTS idx_session_subject ON study_sessions(subject)",
                "CREATE INDEX IF NOT EXISTS idx_session_course ON study_sessions(course_code)",
                "CREATE INDEX IF NOT EXISTS idx_session_user ON study_sessions(user_id)"
        };

        executeTableCreation(createTableSQL, indexSQLs, "study_sessions");
    }

    private static void executeTableCreation(String createTableSQL, String[] indexSQLs, String tableName) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create table
            stmt.execute(createTableSQL);

            // Create indexes
            for (String sql : indexSQLs) {
                try {
                    stmt.execute(sql);
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Failed to create index for " + tableName + ": " + sql, e);
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, tableName + " table creation failed", e);
            throw new RuntimeException(tableName + " table creation failed", e);
        }
    }

    // Get database connection
    private static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get database connection", e);
            throw e;
        }
    }

    /* Task Methods */
    public static boolean saveTask(Task task, int userId) {
        String sql = "INSERT INTO tasks (date, start_time, end_time, subject, topic, priority, completed, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDate(1, java.sql.Date.valueOf(task.getDate()));
            pstmt.setTime(2, Time.valueOf(task.getStartTime()));
            pstmt.setTime(3, Time.valueOf(task.getEndTime()));
            pstmt.setString(4, task.getSubject());
            pstmt.setString(5, task.getTopic());
            pstmt.setString(6, task.getPriority());
            pstmt.setBoolean(7, task.isCompleted());
            pstmt.setInt(8, userId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                logger.log(Level.WARNING, "No rows affected when saving task");
                return false;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save task", e);
            return false;
        }
    }

    public static List<Task> getAllTasks(int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY " +
                "CASE priority WHEN 'High' THEN 1 WHEN 'Medium' THEN 2 ELSE 3 END, " +
                "date, start_time";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get all tasks", e);
        }
        return tasks;
    }

    public static List<Task> getTasksByDate(LocalDate date, int userId) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE date = ? AND user_id = ? ORDER BY start_time";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(date));
            pstmt.setInt(2, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get tasks by date", e);
        }
        return tasks;
    }

    public static boolean updateTaskStatus(int taskId, boolean completed, int userId) {
        String sql = "UPDATE tasks SET completed = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, completed);
            pstmt.setInt(2, taskId);
            pstmt.setInt(3, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to update task status", e);
            return false;
        }
    }

    public static double getCompletionPercentage(int userId) {
        String sql = "SELECT " +
                "COUNT(*) AS total, " +
                "SUM(CASE WHEN completed = TRUE THEN 1 ELSE 0 END) AS completed " +
                "FROM tasks WHERE user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int completed = rs.getInt("completed");
                    if (total > 0) {
                        return (completed * 100.0) / total;
                    }
                }
                return 0.0;
            }

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get completion percentage", e);
            return 0.0;
        }
    }

    /* New methods for Weekly Summary */
    public static List<Task> getTasksByDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND date BETWEEN ? AND ? " +
                "ORDER BY date, start_time";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get tasks by date range", e);
        }
        return tasks;
    }

    public static List<StudySession> getSessionsByDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        List<StudySession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM study_sessions WHERE user_id = ? AND date BETWEEN ? AND ? " +
                "ORDER BY date, start_time";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToStudySession(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get study sessions by date range", e);
        }
        return sessions;
    }

    public static Map<String, Double> getStudyTimeBySubject(int userId, LocalDate startDate, LocalDate endDate) {
        Map<String, Double> subjectTimeMap = new HashMap<>();
        String sql = "SELECT subject, SUM(TIME_TO_SEC(TIMEDIFF(end_time, start_time))/3600.0) as total_hours " +
                "FROM study_sessions " +
                "WHERE user_id = ? AND date BETWEEN ? AND ? " +
                "GROUP BY subject";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    subjectTimeMap.put(
                            rs.getString("subject"),
                            rs.getDouble("total_hours")
                    );
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get study time by subject", e);
        }
        return subjectTimeMap;
    }

    public static Map<LocalDate, Double> getDailyStudyTime(int userId, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Double> dailyTimeMap = new TreeMap<>();
        // নোট: এখানে closing parenthesis যোগ করা হয়েছে
        String sql = "SELECT date, SUM(TIME_TO_SEC(TIMEDIFF(end_time, start_time))/3600.0) as daily_hours " +
                "FROM study_sessions " +
                "WHERE user_id = ? AND date BETWEEN ? AND ? " +
                "GROUP BY date";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setDate(2, java.sql.Date.valueOf(startDate));
            pstmt.setDate(3, java.sql.Date.valueOf(endDate));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    dailyTimeMap.put(
                            rs.getDate("date").toLocalDate(),
                            rs.getDouble("daily_hours")
                    );
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get daily study time", e);
        }
        return dailyTimeMap;
    }
    /* Methods for Progress Overview */
    public static Map<String, Long> getTaskStatsBySubject(int userId) {
        Map<String, Long> stats = new HashMap<>();
        String sql = "SELECT subject, " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN completed = TRUE THEN 1 ELSE 0 END) as completed " +
                "FROM tasks WHERE user_id = ? GROUP BY subject";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String subject = rs.getString("subject");
                    long completed = rs.getLong("completed");
                    long pending = rs.getLong("total") - completed;

                    stats.put(subject + "_completed", completed);
                    stats.put(subject + "_pending", pending);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get task stats by subject", e);
        }
        return stats;
    }

    public static Map<LocalDate, Long> getSessionCountByDate(int userId) {
        Map<LocalDate, Long> sessionCounts = new TreeMap<>();
        String sql = "SELECT date, COUNT(*) as session_count " +
                "FROM study_sessions WHERE user_id = ? " +
                "GROUP BY date ORDER BY date";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("date").toLocalDate();
                    long count = rs.getLong("session_count");
                    sessionCounts.put(date, count);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get session counts by date", e);
        }
        return sessionCounts;
    }

    /* Study Session Methods */
    public static boolean saveStudySession(StudySession session, int userId) {
        String sql = "INSERT INTO study_sessions (date, subject, course_code, start_time, end_time, topics, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDate(1, java.sql.Date.valueOf(session.getDate()));
            pstmt.setString(2, session.getSubject());
            pstmt.setString(3, session.getCourseCode());
            pstmt.setTime(4, Time.valueOf(session.getStartTime()));
            pstmt.setTime(5, Time.valueOf(session.getEndTime()));
            pstmt.setString(6, session.getTopics());
            pstmt.setInt(7, userId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                logger.log(Level.WARNING, "No rows affected when saving study session");
                return false;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    session.setId(generatedKeys.getInt(1));
                }
            }
            return true;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to save study session", e);
            return false;
        }
    }

    public static List<StudySession> getAllStudySessions(int userId) {
        List<StudySession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM study_sessions WHERE user_id = ? ORDER BY date, start_time";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToStudySession(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get all study sessions", e);
        }
        return sessions;
    }

    public static List<StudySession> getUpcomingStudySessions(int userId) {
        List<StudySession> sessions = new ArrayList<>();
        String sql = "SELECT * FROM study_sessions WHERE date >= CURDATE() AND user_id = ? ORDER BY date, start_time";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    sessions.add(mapResultSetToStudySession(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to get upcoming study sessions", e);
        }
        return sessions;
    }

    public static boolean deleteStudySession(int sessionId, int userId) {
        String sql = "DELETE FROM study_sessions WHERE id = ? AND user_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sessionId);
            pstmt.setInt(2, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to delete study session", e);
            return false;
        }
    }

    /* Helper methods */
    private static Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setDate(rs.getDate("date").toLocalDate());
        task.setStartTime(rs.getTime("start_time").toLocalTime());
        task.setEndTime(rs.getTime("end_time").toLocalTime());
        task.setSubject(rs.getString("subject"));
        task.setTopic(rs.getString("topic"));
        task.setPriority(rs.getString("priority"));
        task.setCompleted(rs.getBoolean("completed"));
        return task;
    }

    private static StudySession mapResultSetToStudySession(ResultSet rs) throws SQLException {
        StudySession session = new StudySession();
        session.setId(rs.getInt("id"));
        session.setDate(rs.getDate("date").toLocalDate());
        session.setSubject(rs.getString("subject"));
        session.setCourseCode(rs.getString("course_code"));
        session.setStartTime(rs.getTime("start_time").toLocalTime());
        session.setEndTime(rs.getTime("end_time").toLocalTime());
        session.setTopics(rs.getString("topics"));
        return session;
    }

    // Close all resources
    public static void closeAll(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to close resource", e);
                }
            }
        }
    }

    // Add these methods to DatabaseHelper class
    public static Map<String, String> getUserData(int userId) {
        Map<String, String> userData = new HashMap<>();
        String query = "SELECT first_name, last_name, username, email, password, mobile_number, gender, institute, registration_date FROM signup WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                userData.put("first_name", rs.getString("first_name"));
                userData.put("last_name", rs.getString("last_name"));
                userData.put("username", rs.getString("username"));
                userData.put("email", rs.getString("email"));
                userData.put("password", rs.getString("password"));
                userData.put("mobile_number", rs.getString("mobile_number"));
                userData.put("gender", rs.getString("gender"));
                userData.put("institute", rs.getString("institute"));
                userData.put("registration_date", rs.getString("registration_date"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userData;
    }

    public static boolean updateUserData(int userId, Map<String, String> userData) {
        String query = "UPDATE signup SET first_name = ?, last_name = ?, username = ?, email = ?, password = ?, mobile_number = ?, gender = ?, institute = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, userData.get("first_name"));
            stmt.setString(2, userData.get("last_name"));
            stmt.setString(3, userData.get("username"));
            stmt.setString(4, userData.get("email"));
            stmt.setString(5, userData.get("password"));
            stmt.setString(6, userData.get("mobile_number"));
            stmt.setString(7, userData.get("gender"));
            stmt.setString(8, userData.get("institute"));
            stmt.setInt(9, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
