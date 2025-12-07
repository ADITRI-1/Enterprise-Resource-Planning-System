package com.iiitd.erp.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

public class DatePicker extends JDialog {
    private LocalDate selectedDate;
    private final JComboBox<String> monthComboBox;
    private final JComboBox<Integer> yearComboBox;
    private final JPanel calendarGrid;
    private final DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public DatePicker(Frame parent) {
        super(parent, "Select Date", true);
        this.selectedDate = LocalDate.now();

        // Month ComboBox
        String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        monthComboBox = new JComboBox<>(months);
        monthComboBox.setSelectedIndex(selectedDate.getMonthValue() - 1);
        monthComboBox.addActionListener(e -> updateCalendar());

        // Year ComboBox
        int currentYear = Year.now().getValue();
        Integer[] years = IntStream.rangeClosed(currentYear - 100, currentYear + 10).boxed().sorted((a, b) -> b - a).toArray(Integer[]::new);
        yearComboBox = new JComboBox<>(years);
        yearComboBox.setSelectedItem(selectedDate.getYear());
        yearComboBox.addActionListener(e -> updateCalendar());

        // Calendar Grid
        calendarGrid = new JPanel(new GridLayout(0, 7, 5, 5));
        calendarGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(calendarGrid, BorderLayout.CENTER);

        setContentPane(mainPanel);
        buildCalendarGrid();
        pack();
        setLocationRelativeTo(parent);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        header.setBackground(Color.WHITE);
        
        monthComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        yearComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        header.add(new JLabel("Month:"));
        header.add(monthComboBox);
        header.add(Box.createHorizontalStrut(15));
        header.add(new JLabel("Year:"));
        header.add(yearComboBox);
        return header;
    }

    private void updateCalendar() {
        int year = (Integer) yearComboBox.getSelectedItem();
        int month = monthComboBox.getSelectedIndex() + 1;
        selectedDate = LocalDate.of(year, month, Math.min(selectedDate.getDayOfMonth(), LocalDate.of(year, month, 1).lengthOfMonth()));
        buildCalendarGrid();
        revalidate();
        pack();
    }

    private void buildCalendarGrid() {
        calendarGrid.removeAll();

        for (String day : new String[]{"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"}) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            calendarGrid.add(dayLabel);
        }

        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < dayOfWeek; i++) {
            calendarGrid.add(new JLabel(""));
        }

        for (int i = 1; i <= selectedDate.lengthOfMonth(); i++) {
            JButton dayButton = new JButton(String.valueOf(i));
            dayButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dayButton.setFocusPainted(false);
            dayButton.setMargin(new Insets(5, 5, 5, 5));
            
            final int day = i;
            dayButton.addActionListener((ActionEvent e) -> {
                selectedDate = selectedDate.withDayOfMonth(day);
                dispose();
            });

            if (i == LocalDate.now().getDayOfMonth() && selectedDate.getMonth() == LocalDate.now().getMonth() && selectedDate.getYear() == LocalDate.now().getYear()) {
                dayButton.setBackground(new Color(45, 125, 200));
                dayButton.setForeground(Color.WHITE);
            } else {
                dayButton.setBackground(Color.WHITE);
            }
            calendarGrid.add(dayButton);
        }
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    public String getSelectedDateAsString() {
        if (selectedDate != null) {
            return selectedDate.format(displayFormatter);
        }
        return "";
    }
}
