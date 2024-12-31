import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AlarmClockApp {
    public static void main(String[] args) {
        // Main Frame
        JFrame frame = new JFrame("Group 3 Alarm Clock");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // Title and Quote
        JLabel title = new JLabel("\u23F0 Group 3 Alarm Clock \u23F0", JLabel.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        JLabel quote = new JLabel("We are here always to To Remind You", JLabel.CENTER);
        quote.setFont(new Font("Arial", Font.ITALIC, 18));

        // Digital Clock
        JLabel digitalClock = new JLabel("", JLabel.CENTER);
        digitalClock.setFont(new Font("Arial", Font.BOLD, 16));

        javax.swing.Timer clockTimer = new javax.swing.Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            digitalClock.setText(now.format(DateTimeFormatter.ofPattern("EEEE, HH:mm:ss")));
        });
        clockTimer.start();

        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        topPanel.add(title);
        topPanel.add(digitalClock);
        topPanel.add(quote);
        frame.add(topPanel, BorderLayout.NORTH);

        // Table for Alarms
        String[] columnNames = { "Name", "Time", "Days", "Occurrence", "Status" };
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Disable editing directly in table cells
            }
        };
        JTable alarmTable = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(alarmTable);
        frame.add(tableScrollPane, BorderLayout.CENTER);

        // Buttons
        JButton createAlarmButton = new JButton("Create Alarm");
        JButton clearAlarmsButton = new JButton("Clear All Alarms");
        JButton editAlarmButton = new JButton("Edit Selected Alarm");
        JButton cancelAlarmButton = new JButton("Cancel Selected Alarm");
        JToggleButton themeToggleButton = new JToggleButton("Dark Theme");
        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.add(createAlarmButton);
        buttonPanel.add(clearAlarmsButton);
        buttonPanel.add(editAlarmButton);
        buttonPanel.add(cancelAlarmButton);
        buttonPanel.add(themeToggleButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Create Alarm Panel
        JPanel createAlarmPanel = new JPanel(new GridLayout(8, 2));
        createAlarmPanel.setBorder(BorderFactory.createTitledBorder("New Alarm"));
        JTextField alarmNameField = new JTextField();
        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1)); // Hours: 0-23
        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1)); // Minutes: 0-59
        hourSpinner.setEditor(new JSpinner.NumberEditor(hourSpinner, "00")); // Format as 2 digits
        minuteSpinner.setEditor(new JSpinner.NumberEditor(minuteSpinner, "00"));

        JRadioButton oneTimeButton = new JRadioButton("One Time");
        JRadioButton consistentButton = new JRadioButton("Consistent");
        ButtonGroup occurrenceGroup = new ButtonGroup();
        occurrenceGroup.add(oneTimeButton);
        occurrenceGroup.add(consistentButton);

        JCheckBox[] dayCheckboxes = new JCheckBox[7];
        String[] days = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        JPanel dayPanel = new JPanel(new GridLayout(2, 4));
        for (int i = 0; i < days.length; i++) {
            dayCheckboxes[i] = new JCheckBox(days[i]);
            dayPanel.add(dayCheckboxes[i]);
        }

        JButton saveButton = new JButton("Save Alarm");
        createAlarmPanel.add(new JLabel("Name for Alarm:"));
        createAlarmPanel.add(alarmNameField);
        createAlarmPanel.add(new JLabel("Set Hour:"));
        createAlarmPanel.add(hourSpinner);
        createAlarmPanel.add(new JLabel("Set Minute:"));
        createAlarmPanel.add(minuteSpinner);
        createAlarmPanel.add(new JLabel("Occurrence:"));
        JPanel occurrencePanel = new JPanel();
        occurrencePanel.add(oneTimeButton);
        occurrencePanel.add(consistentButton);
        createAlarmPanel.add(occurrencePanel);
        createAlarmPanel.add(new JLabel("Days:"));
        createAlarmPanel.add(dayPanel);
        createAlarmPanel.add(new JLabel());
        createAlarmPanel.add(saveButton);
        createAlarmPanel.setVisible(false);

        frame.add(createAlarmPanel, BorderLayout.EAST);

        createAlarmButton.addActionListener(e -> createAlarmPanel.setVisible(true));
        clearAlarmsButton.addActionListener(e -> model.setRowCount(0));

        saveButton.addActionListener(e -> {
            try {
                String alarmName = alarmNameField.getText();
                int hour = (int) hourSpinner.getValue();
                int minute = (int) minuteSpinner.getValue();
                String alarmTime = String.format("%02d:%02d", hour, minute);
                String occurrence = oneTimeButton.isSelected() ? "One Time" : "Consistent";

                EnumSet<DayOfWeek> selectedDays = EnumSet.noneOf(DayOfWeek.class);
                for (int i = 0; i < dayCheckboxes.length; i++) {
                    if (dayCheckboxes[i].isSelected()) {
                        selectedDays.add(DayOfWeek.of(i + 1));
                    }
                }

                if (selectedDays.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please select at least one day for the alarm.");
                    return;
                }

                // Calculate remaining time considering the selected days
                LocalDateTime now = LocalDateTime.now();
                LocalTime currentTime = now.toLocalTime();
                LocalTime alarmLocalTime = LocalTime.of(hour, minute);

                int daysUntilAlarm = 0;
                DayOfWeek currentDay = now.getDayOfWeek();

                // Check if the selected day and time has already passed today
                for (int i = 0; i < 7; i++) {
                    DayOfWeek nextDay = DayOfWeek.of((currentDay.getValue() + i - 1) % 7 + 1);
                    if (selectedDays.contains(nextDay)) {
                        // If alarm time is earlier than current time, set it for the next week
                        if (nextDay == currentDay && alarmLocalTime.isBefore(currentTime)) {
                            daysUntilAlarm = 7; // Next week
                        } else {
                            daysUntilAlarm = i;
                        }
                        break;
                    }
                }

                // Set alarm for the correct time
                LocalDateTime alarmDateTime = now.plusDays(daysUntilAlarm).with(alarmLocalTime);
                Duration duration = Duration.between(now, alarmDateTime);

                // If alarm time is in the past, make sure it's set for the next week
                if (duration.isNegative()) {
                    alarmDateTime = alarmDateTime.plusWeeks(1);
                    duration = Duration.between(now, alarmDateTime);
                }

                long hoursRemaining = duration.toHours();
                long minutesRemaining = duration.toMinutesPart();

                // Add alarm to table
                JButton cancelButton = new JButton("Cancel");
                cancelButton.setBackground(Color.RED);
                cancelButton.setForeground(Color.WHITE);
                cancelButton.addActionListener(event -> model.removeRow(model.getRowCount() - 1));

                model.addRow(new Object[] { alarmName, alarmTime, selectedDays, occurrence, "Active", cancelButton });

                // Notify user with the updated remaining time
                String message = String.format(
                        "Alarm \"%s\" saved successfully!\nTime remaining: %d days, %d hours, and %d minutes.",
                        alarmName, daysUntilAlarm, hoursRemaining % 24, minutesRemaining);
                JOptionPane.showMessageDialog(frame, message);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid data. Please check your inputs.");
            }
        });

        editAlarmButton.addActionListener(e -> {
            int selectedRow = alarmTable.getSelectedRow(); // Get the selected row
            if (selectedRow != -1) { // Ensure a row is selected
                // Get current values of the selected row
                String currentName = (String) model.getValueAt(selectedRow, 0);
                String currentTime = (String) model.getValueAt(selectedRow, 1);
                EnumSet<DayOfWeek> currentDays = (EnumSet<DayOfWeek>) model.getValueAt(selectedRow, 2);
                String currentOccurrence = (String) model.getValueAt(selectedRow, 3);

                // Create an input dialog for editing the alarm details
                String newName = JOptionPane.showInputDialog(frame, "Edit Alarm Name:", currentName);
                if (newName == null || newName.trim().isEmpty())
                    return; // User canceled or entered invalid name

                // Edit time using spinners or input dialog
                String newTime = JOptionPane.showInputDialog(frame, "Edit Alarm Time (HH:MM):", currentTime);
                if (newTime == null || !newTime.matches("\\d{2}:\\d{2}")) {
                    JOptionPane.showMessageDialog(frame, "Invalid time format. Please use HH:MM.");
                    return;
                }

                String[] timeParts = newTime.split(":");
                int newHour = Integer.parseInt(timeParts[0]);
                int newMinute = Integer.parseInt(timeParts[1]);

                // Edit days (here we're assuming you have checkboxes to select days)
                EnumSet<DayOfWeek> newDays = EnumSet.noneOf(DayOfWeek.class);
                for (int i = 0; i < dayCheckboxes.length; i++) {
                    if (dayCheckboxes[i].isSelected()) {
                        newDays.add(DayOfWeek.of(i + 1));
                    }
                }

                if (newDays.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please select at least one day.");
                    return;
                }

                // Edit occurrence type (One Time or Consistent)
                String newOccurrence = oneTimeButton.isSelected() ? "One Time" : "Consistent";

                // Validate and update the table row with the new data
                model.setValueAt(newName, selectedRow, 0);
                model.setValueAt(newTime, selectedRow, 1);
                model.setValueAt(newDays, selectedRow, 2);
                model.setValueAt(newOccurrence, selectedRow, 3);

                // Notify the user
                JOptionPane.showMessageDialog(frame, "Alarm edited successfully!");

            } else {
                JOptionPane.showMessageDialog(frame, "Please select an alarm to edit.");
            }
        });

        cancelAlarmButton.addActionListener(e -> {
            int selectedRow = alarmTable.getSelectedRow(); // Get the selected row
            if (selectedRow != -1) {
                // Remove the selected row from the table model
                model.removeRow(selectedRow);
            } else {
                // Show an error message if no row is selected
                JOptionPane.showMessageDialog(frame, "Please select an alarm to cancel.");
            }
        });
                  new JFXPanel();

                  String FP="Alarm Sound Effect.mp3"
         private static void PlaySound (String FP) {
            try{
                Media sound=new Media(new java.io.file(FP).toURI().toString());
                MediaPlayer mediaPlayer= new MediaPlayer(sound);
                   mediaPlayer.play();
                
            }
            catch(Exception e){
                e.printStackTrace();
            }

         }
                  



        javax.swing.Timer alarmCheckTimer = new javax.swing.Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));
            DayOfWeek currentDay = now.getDayOfWeek();

            for (int i = 0; i < model.getRowCount(); i++) {
                String alarmTime = model.getValueAt(i, 1).toString();
                EnumSet<DayOfWeek> alarmDays = (EnumSet<DayOfWeek>) model.getValueAt(i, 2);

                if (alarmTime.equals(currentTime) && alarmDays.contains(currentDay)
                        && model.getValueAt(i, 4).equals("Active")) {
                    // trigger alarm and notification code

                    int rowIndex = i;
                   
                    // Alarm Notification with Snooze Option
                    int choice = JOptionPane.showOptionDialog(frame,
                            
                            "Alarm \"" + model.getValueAt(i, 0) + "\" is ringing!",
                            "Alarm Ringing",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new Object[] { "Snooze", "Dismiss" },
                            "Snooze");
                            
                            PlaySound("Alarm Sound Effect.mp3");
                            
                    if (choice == JOptionPane.YES_OPTION) { // Snooze Option
                        String[] timeParts = alarmTime.split(":");
                        int hour = Integer.parseInt(timeParts[0]);
                        int minute = Integer.parseInt(timeParts[1]);
                        minute += 5;
                        if (minute >= 60) {
                            minute -= 60;
                            hour = (hour + 1) % 24;
                        }
                        String snoozedTime = String.format("%02d:%02d", hour, minute);
                        model.setValueAt(snoozedTime, rowIndex, 1);

                    } else if (choice == JOptionPane.NO_OPTION) { // Dismiss Option
                        if (model.getValueAt(i, 3).equals("One Time")) { // Check if the alarm is one-time
                            model.removeRow(i--); // Remove the row for one-time alarms
                        } else {
                            // For recurring alarms, update the status to "Rung"
                            model.setValueAt("Rung", i, 4); // Assuming the status is in column 4
                        }

                    }
                }
            }
        });
        alarmCheckTimer.start();

        themeToggleButton.addActionListener(e -> {
            boolean isDark = themeToggleButton.isSelected();
            Color bgColor = isDark ? Color.DARK_GRAY : Color.WHITE;
            Color fgColor = isDark ? Color.WHITE : Color.BLACK;

            frame.getContentPane().setBackground(bgColor);
            topPanel.setBackground(bgColor);
            title.setForeground(fgColor);
            digitalClock.setForeground(fgColor);
            quote.setForeground(fgColor);
            tableScrollPane.getViewport().setBackground(bgColor);
            alarmTable.setForeground(fgColor);
            alarmTable.setBackground(bgColor);
            createAlarmPanel.setBackground(bgColor);
        });

        frame.setVisible(true);
    }
    
    

}