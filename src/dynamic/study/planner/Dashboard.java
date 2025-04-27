package dynamic.study.planner;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.table.*;
import org.jfree.chart.*;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Day;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class Dashboard extends JFrame {
    private int userId;
    private JPanel sidebar;
    private JPanel contentPanel;
    private boolean sidebarVisible = true;
    private final int SIDEBAR_WIDTH = 280;
    private final int COLLAPSED_WIDTH = 70;
    public Color primaryColor = new Color(40, 53, 147);
    public Color secondaryColor = new Color(255, 255, 255);
    private Color accentColor = new Color(255, 152, 0);
    private List<Task> tasks;
    private List<StudySession> studySessions;
    private JButton maximizeButton;

    public Dashboard(String username, int userId) {
        this.userId = userId;
        DatabaseHelper.initialize();
        tasks = DatabaseHelper.getAllTasks(userId);
        studySessions = DatabaseHelper.getAllStudySessions(userId);

        initializeUI(username);
    }

    private void initializeUI(String username) {
        setTitle("Dynamic Study Planner - Dashboard (" + userId + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1366, 768);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

        setupWindowListeners();
        setupMainPanel(username);
    }

    private void setupWindowListeners() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                handleWindowResize();
            }
        });
    }

    private void handleWindowResize() {
        boolean isMaximized = (getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;

        if (isMaximized) {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            Rectangle bounds = gd.getDefaultConfiguration().getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());

            bounds.x += insets.left;
            bounds.y += insets.top;
            bounds.width -= (insets.left + insets.right);
            bounds.height -= (insets.top + insets.bottom);

            setBounds(bounds);
            setShape(null);
            if (maximizeButton != null) {
                maximizeButton.setText("❐");
            }
        } else {
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
            if (maximizeButton != null) {
                maximizeButton.setText("□");
            }
        }
    }

    private void setupMainPanel(String username) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel titleBar = createTitleBar();
        mainPanel.add(titleBar, BorderLayout.NORTH);

        JPanel contentContainer = new JPanel(new BorderLayout());
        sidebar = createSidebar(username);
        contentContainer.add(sidebar, BorderLayout.WEST);

        contentPanel = createContentPanel(username);
        contentContainer.add(contentPanel, BorderLayout.CENTER);

        mainPanel.add(contentContainer, BorderLayout.CENTER);
        setContentPane(mainPanel);
        setDropShadow(this);
    }

    // Add this method to Dashboard class
    public void updateUsername(String newUsername) {
        // Update welcome message if it exists
        Component[] components = contentPanel.getComponents();
        if (components.length > 0 && components[0] instanceof JPanel) {
            JPanel headerPanel = (JPanel) components[0];
            Component[] headerComponents = headerPanel.getComponents();
            if (headerComponents.length > 0 && headerComponents[0] instanceof JLabel) {
                JLabel welcomeLabel = (JLabel) headerComponents[0];
                welcomeLabel.setText("Welcome back, " + newUsername + "!");
            }
        }
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(primaryColor);
        titleBar.setPreferredSize(new Dimension(getWidth(), 50));
        titleBar.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 10));

        JPanel logoPanel = createLogoPanel();
        titleBar.add(logoPanel, BorderLayout.WEST);

        JPanel controlPanel = createControlPanel();
        titleBar.add(controlPanel, BorderLayout.EAST);

        addTitleBarMouseListeners(titleBar);

        return titleBar;
    }

    private JPanel createLogoPanel() {
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoPanel.setBackground(primaryColor);

        try {
            ImageIcon logoIcon = new ImageIcon(ClassLoader.getSystemResource("icon/abc.png"));
            if (logoIcon.getImage() != null) {
                Image scaledLogo = logoIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
                JLabel logoLabel = new JLabel(new ImageIcon(scaledLogo));
                logoPanel.add(logoLabel);
            }
        } catch (Exception e) {
            JLabel logoPlaceholder = new JLabel("DSP");
            logoPlaceholder.setForeground(secondaryColor);
            logoPlaceholder.setFont(new Font("Arial", Font.BOLD, 18));
            logoPanel.add(logoPlaceholder);
        }

        JLabel titleLabel = new JLabel("DYNAMIC STUDY PLANNER");
        titleLabel.setForeground(secondaryColor);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        logoPanel.add(titleLabel);

        return logoPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controlPanel.setBackground(primaryColor);
        controlPanel.setOpaque(false);

        JButton minimizeButton = createControlButton("−");
        minimizeButton.addActionListener(e -> setState(JFrame.ICONIFIED));

        maximizeButton = createControlButton("□");
        maximizeButton.addActionListener(e -> toggleMaximize());

        JButton closeButton = createControlButton("×");
        closeButton.setBackground(new Color(255, 69, 58));
        closeButton.addActionListener(e -> System.exit(0));

        controlPanel.add(minimizeButton);
        controlPanel.add(maximizeButton);
        controlPanel.add(closeButton);

        return controlPanel;
    }

    private void addTitleBarMouseListeners(JPanel titleBar) {
        MouseAdapter ma = new MouseAdapter() {
            private Point initialClick;

            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int thisX = getLocation().x;
                int thisY = getLocation().y;

                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                setLocation(thisX + xMoved, thisY + yMoved);
            }
        };

        titleBar.addMouseListener(ma);
        titleBar.addMouseMotionListener(ma);
    }

    private void toggleMaximize() {
        if ((getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
            setSize(1366, 768);
            setLocationRelativeTo(null);
            maximizeButton.setText("□");
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            maximizeButton.setText("❐");
        }
    }

    private JButton createControlButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(secondaryColor);
        button.setBackground(new Color(60, 60, 60));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(30, 30));
        button.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(80, 80, 80));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(60, 60, 60));
            }
        });

        return button;
    }

    private void setDropShadow(JFrame frame) {
        if ((frame.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0) {
            return;
        }

        try {
            frame.setOpacity(0.0f);
            javax.swing.Timer timer = new javax.swing.Timer(10, new ActionListener() {
                float opacity = 0.0f;
                final float targetOpacity = 0.95f;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (opacity < targetOpacity) {
                        opacity = Math.min(opacity + 0.05f, targetOpacity);
                        frame.setOpacity(opacity);
                    } else {
                        ((javax.swing.Timer) e.getSource()).stop();
                    }
                }
            });
            timer.start();
        } catch (Exception e) {
            System.err.println("Shadow effect not supported");
        }
    }

    private JPanel createSidebar(String username) {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(245, 245, 245));
        sidebar.setPreferredSize(new Dimension(SIDEBAR_WIDTH, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 15, 20, 15)
        ));

        addUserProfileSection(sidebar, username);
        addMenuItems(sidebar);
        addLogoutButton(sidebar);

        return sidebar;
    }

    private void addUserProfileSection(JPanel sidebar, String username) {
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel avatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(primaryColor);
                g2.fillOval(0, 0, getWidth(), getHeight());

                g2.setColor(secondaryColor);
                g2.setFont(new Font("Arial", Font.BOLD, 24));
                String initials = username.length() > 0 ? username.substring(0, 1).toUpperCase() : "U";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(initials)) / 2;
                int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(initials, x, y);
            }
        };
        avatarPanel.setPreferredSize(new Dimension(80, 80));
        avatarPanel.setMaximumSize(new Dimension(80, 80));

        JLabel userName = new JLabel(username);
        userName.setFont(new Font("Arial", Font.BOLD, 16));
        userName.setAlignmentX(Component.LEFT_ALIGNMENT);
        userName.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 0));

        userPanel.add(avatarPanel);
        userPanel.add(userName);
        sidebar.add(userPanel);

        JLabel menuTitle = new JLabel("MAIN MENU");
        menuTitle.setFont(new Font("Arial", Font.BOLD, 12));
        menuTitle.setForeground(new Color(150, 150, 150));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        menuTitle.setBorder(BorderFactory.createEmptyBorder(20, 5, 10, 0));
        sidebar.add(menuTitle);
    }

    private void addMenuItems(JPanel sidebar) {
        String[] menuItems = {"Today's Schedule",
                "Study Sessions", "Progress Overview", "Weekly Summary",
                "Import/Export", "Settings"};
        String[] menuIcons = {"📅", "⏱", "📊", "📈", "🔄", "⚙"};

        for (int i = 0; i < menuItems.length; i++) {
            final String menuItem = menuItems[i];
            final String menuIcon = menuIcons[i];

            JButton menuButton = new JButton(menuItem);
            menuButton.setIcon(new TextIcon(menuIcon, 20));
            menuButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            menuButton.setHorizontalAlignment(SwingConstants.LEFT);
            menuButton.setFont(new Font("Arial", Font.PLAIN, 14));
            menuButton.setForeground(new Color(80, 80, 80));
            menuButton.setBackground(new Color(245, 245, 245));
            menuButton.setBorderPainted(false);
            menuButton.setFocusPainted(false);
            menuButton.setContentAreaFilled(false);
            menuButton.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

            menuButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    menuButton.setForeground(primaryColor);
                    menuButton.setBackground(new Color(230, 230, 230));
                    menuButton.setContentAreaFilled(true);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    menuButton.setForeground(new Color(80, 80, 80));
                    menuButton.setBackground(new Color(245, 245, 245));
                    menuButton.setContentAreaFilled(false);
                }
            });

            menuButton.addActionListener(e -> updateContentPanel(menuItem));
            sidebar.add(menuButton);
        }
    }

    private void addLogoutButton(JPanel sidebar) {
        sidebar.add(Box.createVerticalGlue());

        JButton logoutButton = new JButton("Logout");
        logoutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutButton.setHorizontalAlignment(SwingConstants.LEFT);
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutButton.setForeground(new Color(200, 50, 50));
        logoutButton.setBackground(new Color(245, 245, 245));
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    Dashboard.this,
                    "Are you sure you want to logout?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                new Login().setVisible(true);
                Dashboard.this.dispose();
            }
        });
        sidebar.add(logoutButton);
    }

    private JPanel createContentPanel(String username) {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(secondaryColor);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        addContentHeader(contentPanel, username);
        addDashboardCards(contentPanel);

        return contentPanel;
    }

    private void addContentHeader(JPanel contentPanel, String username) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(secondaryColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel welcomeLabel = new JLabel("Welcome back, " + username + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(60, 60, 60));

        JLabel dateLabel = new JLabel("Today is " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setForeground(new Color(120, 120, 120));

        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        contentPanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void addDashboardCards(JPanel contentPanel) {
        JPanel cardsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardsPanel.setBackground(secondaryColor);

        JPanel card1 = createDashboardCard("Today's Tasks", getTaskCountText(), "📋", accentColor);
        JPanel card2 = createDashboardCard("Study Progress", getProgressText(), "📊", primaryColor);
        JPanel card3 = createDashboardCard("Upcoming Sessions", getUpcomingSessionsText(), "⏰", new Color(76, 175, 80));
        JPanel card4 = createDashboardCard("Quick Actions", "Add new task", "⚡", new Color(156, 39, 176));

        cardsPanel.add(card1);
        cardsPanel.add(card2);
        cardsPanel.add(card3);
        cardsPanel.add(card4);

        contentPanel.add(cardsPanel, BorderLayout.CENTER);
    }

    private String getTaskCountText() {
        long todayTasks = tasks.stream()
                .filter(task -> task.getDate().equals(LocalDate.now()))
                .count();
        return todayTasks + " tasks for today";
    }

    private String getProgressText() {
        if (tasks.isEmpty()) return "No tasks yet";

        long completed = tasks.stream().filter(Task::isCompleted).count();
        int percentage = (int) ((completed * 100) / tasks.size());
        return percentage + "% completed";
    }

    private String getUpcomingSessionsText() {
        long upcoming = studySessions.stream()
                .filter(session -> session.getDate().isAfter(LocalDate.now().minusDays(1)))
                .count();
        return upcoming + " upcoming sessions";
    }

    private JPanel createDashboardCard(String title, String subtitle, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(secondaryColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        if (title.equals("Today's Tasks")) {
            setupTaskListCard(card);
        } else if (title.equals("Study Progress")) {
            setupProgressChartCard(card);
        } else if (title.equals("Upcoming Sessions")) {
            setupSessionListCard(card);
        } else {
            setupBasicCard(card, title, subtitle, icon, color);
        }

        addCardHoverEffect(card, color);
        return card;
    }

    private void setupTaskListCard(JPanel card) {
        DefaultListModel<String> model = new DefaultListModel<>();
        tasks.stream()
                .filter(task -> task.getDate().equals(LocalDate.now()))
                .sorted((t1, t2) -> {
                    if (t1.getPriority().equals(t2.getPriority())) {
                        return t1.getStartTime().compareTo(t2.getStartTime());
                    }
                    return t1.getPriority().equals("High") ? -1 :
                            t2.getPriority().equals("High") ? 1 :
                                    t1.getPriority().equals("Medium") ? -1 : 1;
                })
                .forEach(task -> model.addElement(
                        task.getSubject() + " - " + task.getTopic() +
                                " (" + task.getStartTime() + " to " + task.getEndTime() + ")"
                ));

        JList<String> taskList = new JList<>(model);
        taskList.setBackground(secondaryColor);
        card.add(new JScrollPane(taskList), BorderLayout.CENTER);
    }

    private void setupProgressChartCard(JPanel card) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        long completed = tasks.stream().filter(Task::isCompleted).count();
        long pending = tasks.size() - completed;
        if (!tasks.isEmpty()) {
            dataset.setValue("Completed", completed);
            dataset.setValue("Pending", pending);
        } else {
            dataset.setValue("No Tasks", 1);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                null, dataset, true, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Completed", Color.GREEN);
        plot.setSectionPaint("Pending", Color.RED);
        plot.setSectionPaint("No Tasks", Color.LIGHT_GRAY);
        plot.setBackgroundPaint(secondaryColor);
        plot.setOutlineVisible(false);
        plot.setLabelGenerator(new org.jfree.chart.labels.StandardPieSectionLabelGenerator("{0}: {2}"));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBackground(secondaryColor);
        card.add(chartPanel, BorderLayout.CENTER);
    }

    private void setupSessionListCard(JPanel card) {
        DefaultListModel<String> model = new DefaultListModel<>();
        studySessions.stream()
                .filter(session -> session.getDate().isAfter(LocalDate.now().minusDays(1)))
                .sorted(Comparator.comparing(StudySession::getDate)
                        .thenComparing(StudySession::getStartTime))
                .forEach(session -> {
                    long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), session.getDate());
                    String daysText = daysUntil == 0 ? "Today" :
                            daysUntil == 1 ? "Tomorrow" :
                                    "In " + daysUntil + " days";

                    model.addElement(session.getSubject() + " - " +
                            session.getDate().format(DateTimeFormatter.ofPattern("MMM dd")) +
                            " (" + daysText + ")");
                });

        if (model.isEmpty()) {
            model.addElement("No upcoming sessions");
        }

        JList<String> sessionList = new JList<>(model);
        sessionList.setBackground(secondaryColor);
        card.add(new JScrollPane(sessionList), BorderLayout.CENTER);
    }

    private void setupBasicCard(JPanel card, String title, String subtitle, String icon, Color color) {
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        iconLabel.setForeground(color);
        iconLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(secondaryColor);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(60, 60, 60));

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(120, 120, 120));

        textPanel.add(titleLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(subtitleLabel);

        card.add(textPanel, BorderLayout.WEST);
        card.add(iconLabel, BorderLayout.EAST);
    }

    private void addCardHoverEffect(JPanel card, Color color) {
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(color, 2),
                        BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230)),
                        BorderFactory.createEmptyBorder(20, 20, 20, 20)
                ));
            }
        });
    }

    public void updateContentPanel(String menuItem) {
        contentPanel.removeAll();

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(secondaryColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel(menuItem);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(60, 60, 60));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        switch (menuItem) {
            case "Today's Schedule":
                createScheduleForm();
                break;
            case "Study Sessions":
                createStudySessionsPanel();
                break;
            case "Progress Overview":
                createProgressOverviewPanel();
                break;
            case "Weekly Summary":
                createWeeklySummaryPanel();
                break;
            case "Import/Export":
                createImportExportPanel();
                break;
            case "Settings":
                UserSettings userSettings = new UserSettings(userId, this);
                contentPanel.add(userSettings.getPanel(), BorderLayout.CENTER);
                break;
            default:
                createDefaultContentPanel(menuItem);
                break;

        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void createImportExportPanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(secondaryColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel exportPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        exportPanel.setBackground(secondaryColor);
        exportPanel.setBorder(BorderFactory.createTitledBorder("Export Options"));

        JButton exportScheduleBtn = createExportButton("Download today's schedule PDF");
        exportScheduleBtn.addActionListener(e -> exportTodaySchedule());

        JButton exportProgressBtn = createExportButton("Download progress overview PDF");
        exportProgressBtn.addActionListener(e -> exportProgressOverview());

        JButton exportSummaryBtn = createExportButton("Download Weekly summary PDF");
        exportSummaryBtn.addActionListener(e -> exportWeeklySummary());

        exportPanel.add(exportScheduleBtn);
        exportPanel.add(exportProgressBtn);
        exportPanel.add(exportSummaryBtn);

        mainPanel.add(exportPanel, gbc);
        contentPanel.add(mainPanel, BorderLayout.CENTER);
    }

    private void exportTodaySchedule() {
        try (PDDocument document = new PDDocument()) {
            // Create initial page
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // First content stream (not in try-with-resources)
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            try {
                // Add title
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Today's Schedule - " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
                contentStream.endText();

                // Add tasks
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                float yPosition = 700;

                DefaultListModel<String> model = new DefaultListModel<>();
                tasks.stream()
                        .filter(task -> task.getDate().equals(LocalDate.now()))
                        .sorted((t1, t2) -> t1.getStartTime().compareTo(t2.getStartTime()))
                        .forEach(task -> model.addElement(
                                task.getSubject() + " - " + task.getTopic() +
                                        " (" + task.getStartTime() + " to " + task.getEndTime() + ")" +
                                        (task.isCompleted() ? " [COMPLETED]" : "")
                        ));

                for (int i = 0; i < model.size(); i++) {
                    if (yPosition < 50) {
                        // Close current content stream
                        contentStream.close();

                        // Create new page
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);

                        // Create new content stream
                        contentStream = new PDPageContentStream(document, page);

                        // Add continuation header
                        contentStream.beginText();
                        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                        contentStream.newLineAtOffset(50, 750);
                        contentStream.showText("Today's Schedule - " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) + " (cont.)");
                        contentStream.endText();

                        yPosition = 700;
                    }

                    String task = model.getElementAt(i);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, yPosition);
                    contentStream.showText(task);
                    contentStream.endText();
                    yPosition -= 20;
                }
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }

            // Save PDF
            String fileName = "TodaySchedule_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            document.save(fileName);

            JOptionPane.showMessageDialog(this,
                    "Today's schedule exported successfully to:\n" + new File(fileName).getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error exporting PDF: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    private void exportProgressOverview() {
        try {
            // Create a temporary panel with progress overview
            JPanel progressPanel = new JPanel(new BorderLayout());
            progressPanel.setPreferredSize(new Dimension(800, 600));

            JLabel titleLabel = new JLabel("Progress Overview");
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            progressPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
            chartsPanel.add(createTaskProgressChart());
            chartsPanel.add(createSessionProgressChart());
            progressPanel.add(chartsPanel, BorderLayout.CENTER);

            // Create PDF document
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(new PDRectangle(progressPanel.getWidth(), progressPanel.getHeight()));
            document.addPage(page);

            // Create image from panel
            BufferedImage image = new BufferedImage(
                    progressPanel.getWidth(),
                    progressPanel.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            progressPanel.paint(g2d);
            g2d.dispose();

            PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, 0, 0);
            }

            // Save PDF
            String fileName = "ProgressOverview_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            document.save(fileName);
            document.close();

            JOptionPane.showMessageDialog(this,
                    "Progress overview exported successfully to:\n" + new File(fileName).getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error exporting PDF: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void exportWeeklySummary() {
        try {
            // Create a temporary panel with weekly summary
            WeeklySummary summary = new WeeklySummary(userId,
                    LocalDate.now().minusDays(6),
                    LocalDate.now());

            JPanel summaryPanel = new JPanel(new BorderLayout());
            summaryPanel.setPreferredSize(new Dimension(800, 600));

            JLabel titleLabel = new JLabel("Weekly Summary - " +
                    summary.getStartDate().format(DateTimeFormatter.ofPattern("MMM dd")) + " to " +
                    summary.getEndDate().format(DateTimeFormatter.ofPattern("MMM dd")));
            titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
            titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            summaryPanel.add(titleLabel, BorderLayout.NORTH);

            JTextArea summaryText = new JTextArea(summary.getSummaryText());
            summaryText.setEditable(false);
            summaryText.setFont(new Font("Arial", Font.PLAIN, 12));

            JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
            chartsPanel.add(new ChartPanel(summary.createStudyTimePieChart()));
            chartsPanel.add(new ChartPanel(summary.createDailyStudyTimeChart()));

            summaryPanel.add(new JScrollPane(summaryText), BorderLayout.CENTER);
            summaryPanel.add(chartsPanel, BorderLayout.SOUTH);

            // Create PDF document
            PDDocument document = new PDDocument();
            PDPage page = new PDPage(new PDRectangle(summaryPanel.getWidth(), summaryPanel.getHeight()));
            document.addPage(page);

            // Create image from panel
            BufferedImage image = new BufferedImage(
                    summaryPanel.getWidth(),
                    summaryPanel.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            summaryPanel.paint(g2d);
            g2d.dispose();

            PDImageXObject pdImage = LosslessFactory.createFromImage(document, image);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.drawImage(pdImage, 0, 0);
            }

            // Save PDF
            String fileName = "WeeklySummary_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            document.save(fileName);
            document.close();

            JOptionPane.showMessageDialog(this,
                    "Weekly summary exported successfully to:\n" + new File(fileName).getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error exporting PDF: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private JButton createExportButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setBackground(primaryColor);
        button.setForeground(secondaryColor);
        button.setPreferredSize(new Dimension(300, 50));
        return button;
    }

    private void createDefaultContentPanel(String menuItem) {
        JTextArea contentText = new JTextArea("This is the " + menuItem + " section.\n\n" +
                "Here you can manage your " + menuItem.toLowerCase() + " and track your progress.");
        contentText.setEditable(false);
        contentText.setFont(new Font("Arial", Font.PLAIN, 16));
        contentText.setLineWrap(true);
        contentText.setWrapStyleWord(true);
        contentText.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        contentPanel.add(new JScrollPane(contentText), BorderLayout.CENTER);
    }

    private void createProgressOverviewPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(secondaryColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel taskChartPanel = createTaskProgressChart();
        mainPanel.add(taskChartPanel);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel sessionChartPanel = createSessionProgressChart();
        mainPanel.add(sessionChartPanel);

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createTaskProgressChart() {
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

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(secondaryColor);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.getRenderer().setSeriesPaint(0, new Color(76, 175, 80));
        plot.getRenderer().setSeriesPaint(1, new Color(244, 67, 54));

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 400));
        return chartPanel;
    }

    private JPanel createSessionProgressChart() {
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

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(secondaryColor);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.getRenderer().setSeriesPaint(0, primaryColor);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(700, 400));
        return chartPanel;
    }

    private void createStudySessionsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(secondaryColor);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setBackground(secondaryColor);

        JButton addSessionBtn = new JButton("Add Upcoming Session");
        addSessionBtn.setBackground(primaryColor);
        addSessionBtn.setForeground(secondaryColor);
        addSessionBtn.addActionListener(e -> showAddSessionDialog());

        JButton createRoutineBtn = new JButton("Create Routine");
        createRoutineBtn.setBackground(primaryColor);
        createRoutineBtn.setForeground(secondaryColor);
        createRoutineBtn.addActionListener(e -> showRoutine());

        buttonPanel.add(addSessionBtn);
        buttonPanel.add(createRoutineBtn);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        studySessions = DatabaseHelper.getAllStudySessions(userId);
        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"ID", "Date", "Subject", "Course Code", "Time", "Topics"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (StudySession session : studySessions) {
            model.addRow(new Object[]{
                    session.getId(),
                    session.getDate().toString(),
                    session.getSubject(),
                    session.getCourseCode(),
                    session.getStartTime() + " - " + session.getEndTime(),
                    session.getTopics()
            });
        }

        JTable sessionTable = new JTable(model);
        sessionTable.setRowHeight(30);
        JScrollPane tableScroll = new JScrollPane(sessionTable);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        contentPanel.add(mainPanel, BorderLayout.CENTER);
    }

    private void createWeeklySummaryPanel() {
        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(secondaryColor);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create date holder class
        class DateHolder {
            LocalDate start;
            LocalDate end;
        }

        // Initialize date holder with current week
        DateHolder dateHolder = new DateHolder();
        dateHolder.start = LocalDate.now().minusDays(6);
        dateHolder.end = LocalDate.now();

        // Create components that need to be updated
        JLabel dateLabel = new JLabel();
        JTextArea summaryText = new JTextArea();
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        chartsPanel.setBackground(secondaryColor);

        // Create navigation buttons
        JButton prevBtn = new JButton("← Previous Week");
        JButton nextBtn = new JButton("Next Week →");

        // Method to update all components
        Runnable updateView = () -> {
            WeeklySummary summary = new WeeklySummary(userId, dateHolder.start, dateHolder.end);

            // Update date label
            dateLabel.setText(String.format("%s to %s",
                    dateHolder.start.format(DateTimeFormatter.ofPattern("MMM dd")),
                    dateHolder.end.format(DateTimeFormatter.ofPattern("MMM dd"))));

            // Update summary text
            summaryText.setText(summary.getSummaryText());
            summaryText.setEditable(false);
            summaryText.setFont(new Font("Arial", Font.PLAIN, 14));
            summaryText.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

            // Update charts
            chartsPanel.removeAll();
            chartsPanel.add(new ChartPanel(summary.createStudyTimePieChart()));
            chartsPanel.add(new ChartPanel(summary.createDailyStudyTimeChart()));

            // Update next button state
            nextBtn.setEnabled(!dateHolder.end.isAfter(LocalDate.now()));
        };

        // Set up button actions
        prevBtn.addActionListener(e -> {
            dateHolder.start = dateHolder.start.minusDays(7);
            dateHolder.end = dateHolder.end.minusDays(7);
            updateView.run();
        });

        nextBtn.addActionListener(e -> {
            if (dateHolder.end.isBefore(LocalDate.now())) {
                dateHolder.start = dateHolder.start.plusDays(7);
                dateHolder.end = dateHolder.end.plusDays(7);
                updateView.run();
            }
        });

        // Navigation panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        navPanel.setBackground(secondaryColor);
        navPanel.add(prevBtn);
        navPanel.add(dateLabel);
        navPanel.add(nextBtn);

        // Initial UI update
        updateView.run();

        // Add all components to main panel
        mainPanel.add(navPanel);
        mainPanel.add(summaryText);
        mainPanel.add(chartsPanel);

        // Add to content panel
        contentPanel.add(new JScrollPane(mainPanel), BorderLayout.CENTER);
    }

    private void showAddSessionDialog() {
        JDialog dialog = new JDialog(this, "Add New Study Session", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Date
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Date:"), gbc);

        gbc.gridx = 1;
        JTextField dateField = new JTextField(LocalDate.now().toString(), 20);
        dialog.add(dateField, gbc);

        // Subject
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Subject:"), gbc);

        gbc.gridx = 1;
        JTextField subjectField = new JTextField(20);
        dialog.add(subjectField, gbc);

        // Course Code
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Course Code:"), gbc);

        gbc.gridx = 1;
        JTextField courseCodeField = new JTextField(20);
        dialog.add(courseCodeField, gbc);

        // Start Time
        gbc.gridx = 0; gbc.gridy = 3;
        dialog.add(new JLabel("Start Time:"), gbc);

        gbc.gridx = 1;
        JTextField startTimeField = new JTextField("09:00", 20);
        dialog.add(startTimeField, gbc);

        // End Time
        gbc.gridx = 0; gbc.gridy = 4;
        dialog.add(new JLabel("End Time:"), gbc);

        gbc.gridx = 1;
        JTextField endTimeField = new JTextField("11:00", 20);
        dialog.add(endTimeField, gbc);

        // Topics
        gbc.gridx = 0; gbc.gridy = 5;
        dialog.add(new JLabel("Topics:"), gbc);

        gbc.gridx = 1;
        JTextArea topicsArea = new JTextArea(3, 20);
        topicsArea.setLineWrap(true);
        topicsArea.setWrapStyleWord(true);
        dialog.add(new JScrollPane(topicsArea), gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(secondaryColor);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(primaryColor);
        saveBtn.setForeground(secondaryColor);
        saveBtn.addActionListener(e -> saveStudySession(dialog));

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        dialog.add(buttonPanel, gbc);

        dialog.setVisible(true);
    }

    private void saveStudySession(JDialog dialog) {
        try {
            Component[] components = dialog.getContentPane().getComponents();

            JTextField dateField = (JTextField) components[1];
            JTextField subjectField = (JTextField) components[3];
            JTextField courseCodeField = (JTextField) components[5];
            JTextField startTimeField = (JTextField) components[7];
            JTextField endTimeField = (JTextField) components[9];
            JTextArea topicsArea = (JTextArea) ((JScrollPane) components[11]).getViewport().getView();

            StudySession session = new StudySession(
                    LocalDate.parse(dateField.getText()),
                    subjectField.getText(),
                    courseCodeField.getText(),
                    LocalTime.parse(startTimeField.getText()),
                    LocalTime.parse(endTimeField.getText()),
                    topicsArea.getText()
            );

            DatabaseHelper.saveStudySession(session, userId);
            studySessions = DatabaseHelper.getAllStudySessions(userId);

            dialog.dispose();
            updateContentPanel("Study Sessions");

            contentPanel.removeAll();
            contentPanel.add(createContentPanel(""));
            contentPanel.revalidate();
            contentPanel.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(dialog, "Invalid input format!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRoutine() {
        studySessions = DatabaseHelper.getAllStudySessions(userId);
        if (studySessions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No study sessions found!", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Study Routine", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        Map<LocalDate, List<StudySession>> sessionsByDate = studySessions.stream()
                .collect(Collectors.groupingBy(StudySession::getDate));

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"Time", "Subject", "Course Code", "Topics"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        for (LocalDate date : sessionsByDate.keySet()) {
            model.addRow(new Object[]{date.toString(), "", "", ""});

            for (StudySession session : sessionsByDate.get(date)) {
                model.addRow(new Object[]{
                        session.getStartTime() + " - " + session.getEndTime(),
                        session.getSubject(),
                        session.getCourseCode(),
                        session.getTopics()
                });
            }

            model.addRow(new Object[]{"", "", "", ""});
        }

        JTable routineTable = new JTable(model);
        routineTable.setRowHeight(30);

        routineTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (value instanceof String && ((String) value).matches("\\d{4}-\\d{2}-\\d{2}")) {
                    c.setBackground(new Color(220, 230, 240));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (value instanceof String && ((String) value).isEmpty()) {
                    c.setBackground(secondaryColor);
                } else {
                    c.setBackground(secondaryColor);
                }

                return c;
            }
        });

        dialog.add(new JScrollPane(routineTable), BorderLayout.CENTER);

        JButton printBtn = new JButton("Print Routine");
        printBtn.setBackground(primaryColor);
        printBtn.setForeground(secondaryColor);
        printBtn.addActionListener(e -> {
            try {
                routineTable.print();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error printing routine", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(printBtn);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void createScheduleForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(secondaryColor);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Date Field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Date:"), gbc);

        gbc.gridx = 1;
        JTextField dateField = new JTextField(LocalDate.now().toString(), 20);
        formPanel.add(dateField, gbc);

        // Start Time
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Start Time:"), gbc);

        gbc.gridx = 1;
        JTextField startTimeField = new JTextField("09:00", 20);
        formPanel.add(startTimeField, gbc);

        // End Time
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("End Time:"), gbc);

        gbc.gridx = 1;
        JTextField endTimeField = new JTextField("11:00", 20);
        formPanel.add(endTimeField, gbc);

        // Subject
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Subject:"), gbc);

        gbc.gridx = 1;
        JTextField subjectField = new JTextField(20);
        formPanel.add(subjectField, gbc);

        // Topic
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Topic:"), gbc);

        gbc.gridx = 1;
        JTextField topicField = new JTextField(20);
        formPanel.add(topicField, gbc);

        // Priority
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Priority:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> priorityCombo = new JComboBox<>(new String[]{"High", "Medium", "Low"});
        formPanel.add(priorityCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(secondaryColor);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> updateContentPanel("Today's Schedule"));

        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(primaryColor);
        saveBtn.setForeground(secondaryColor);
        saveBtn.addActionListener(e -> saveTask());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);

        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(buttonPanel, gbc);

        TaskTableModel model = new TaskTableModel();
        JTable taskTable = new JTable(model);
        taskTable.setRowHeight(30);
        taskTable.getColumnModel().getColumn(6).setCellRenderer(new CheckboxRenderer());
        taskTable.getColumnModel().getColumn(6).setCellEditor(new CheckboxEditor(new JCheckBox()));

        JScrollPane tableScroll = new JScrollPane(taskTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(formPanel, BorderLayout.NORTH);
        mainPanel.add(tableScroll, BorderLayout.CENTER);

        contentPanel.add(mainPanel, BorderLayout.CENTER);
    }

    private void saveTask() {
        try {
            Component[] components = contentPanel.getComponents();
            JPanel mainPanel = (JPanel) components[1];
            JPanel formPanel = (JPanel) mainPanel.getComponent(0);

            JTextField dateField = (JTextField) formPanel.getComponent(1);
            JTextField startTimeField = (JTextField) formPanel.getComponent(3);
            JTextField endTimeField = (JTextField) formPanel.getComponent(5);
            JTextField subjectField = (JTextField) formPanel.getComponent(7);
            JTextField topicField = (JTextField) formPanel.getComponent(9);
            JComboBox<String> priorityCombo = (JComboBox<String>) formPanel.getComponent(11);

            Task task = new Task(
                    LocalDate.parse(dateField.getText()),
                    LocalTime.parse(startTimeField.getText()),
                    LocalTime.parse(endTimeField.getText()),
                    subjectField.getText(),
                    topicField.getText(),
                    (String) priorityCombo.getSelectedItem()
            );

            DatabaseHelper.saveTask(task, userId);
            tasks = DatabaseHelper.getAllTasks(userId);
            updateContentPanel("Today's Schedule");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input format!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    class TaskTableModel extends AbstractTableModel {
        private String[] columnNames = {"ID", "Date", "Start Time", "End Time", "Subject", "Topic", "Completed", "Priority"};

        @Override
        public int getRowCount() { return tasks.size(); }
        @Override
        public int getColumnCount() { return columnNames.length; }
        @Override
        public String getColumnName(int col) { return columnNames[col]; }
        @Override
        public Class<?> getColumnClass(int col) {
            return col == 6 ? Boolean.class : String.class;
        }
        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 6;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Task task = tasks.get(row);
            switch(col) {
                case 0: return task.getId();
                case 1: return task.getDate().toString();
                case 2: return task.getStartTime().toString();
                case 3: return task.getEndTime().toString();
                case 4: return task.getSubject();
                case 5: return task.getTopic();
                case 6: return task.isCompleted();
                case 7: return task.getPriority();
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col == 6) {
                Task task = tasks.get(row);
                task.setCompleted((Boolean) value);
                DatabaseHelper.updateTaskStatus(task.getId(), task.isCompleted(), userId);
                fireTableCellUpdated(row, col);

                contentPanel.removeAll();
                contentPanel.add(createContentPanel(""));
                contentPanel.revalidate();
                contentPanel.repaint();
            }
        }
    }

    class CheckboxRenderer extends JCheckBox implements TableCellRenderer {
        public CheckboxRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
            setOpaque(false);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setSelected((value != null && (Boolean) value));
            return this;
        }
    }

    class CheckboxEditor extends DefaultCellEditor {
        public CheckboxEditor(JCheckBox checkBox) {
            super(checkBox);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            JCheckBox editor = (JCheckBox) super.getTableCellEditorComponent(table, value, isSelected, row, column);
            editor.setSelected((value != null && (Boolean) value));
            return editor;
        }
    }

    private static class TextIcon implements Icon {
        private final String text;
        private final int size;

        public TextIcon(String text, int size) {
            this.text = text;
            this.size = size;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, size));
            FontMetrics fm = g2.getFontMetrics();
            int textX = x + (getIconWidth() - fm.stringWidth(text)) / 2;
            int textY = y + ((getIconHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(text, textX, textY);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size + 5;
        }

        @Override
        public int getIconHeight() {
            return size + 5;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Dashboard dashboard = new Dashboard("Test User", 1);
            dashboard.setVisible(true);
        });
    }
}
