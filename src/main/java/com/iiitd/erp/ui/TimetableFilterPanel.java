package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.*;
import com.iiitd.erp.ui.client.AdminClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.Year;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class TimetableFilterPanel extends JPanel {

    private final AdminClient admin;
    private static final Logger LOGGER = Logger.getLogger(TimetableFilterPanel.class.getName());

    private final JComboBox<CourseResponse> cbCourse = new JComboBox<>();
    private final JComboBox<SectionResponse> cbSection = new JComboBox<>();
    private final JComboBox<InstructorResponse> cbInstructor = new JComboBox<>();
    private final JComboBox<RoomResponse> cbRoom = new JComboBox<>();
    
    private ActionListener selectionListener;

    public TimetableFilterPanel(AdminClient admin) {
        this.admin = admin;
        setOpaque(false);
        setLayout(new BorderLayout(10, 20));
        setBorder(new EmptyBorder(0, 0, 0, 10));

        JLabel title = new JLabel("Schedule Explorer");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 15, 0);
        gbc.gridx = 0;
        gbc.weightx = 1;

        addFilter(filters, gbc, 0, "By Course", cbCourse);
        addFilter(filters, gbc, 2, "By Section", cbSection);
        addFilter(filters, gbc, 4, "By Instructor", cbInstructor);
        addFilter(filters, gbc, 6, "By Room", cbRoom);

        add(filters, BorderLayout.CENTER);
        
        loadDropdowns();
    }

    public void setSelectionListener(ActionListener listener) {
        this.selectionListener = listener;
    }

    private void addFilter(JPanel panel, GridBagConstraints gbc, int y, String label, JComboBox<?> comboBox) {
        gbc.gridy = y;
        panel.add(new JLabel(label), gbc);
        gbc.gridy = y + 1;
        styleComboBox(comboBox);
        comboBox.addActionListener(e -> {
            if (selectionListener != null) {
                selectionListener.actionPerformed(e);
            }
        });
        panel.add(comboBox, gbc);
    }
    
    public void loadFilters() { loadDropdowns(); }

    private void loadDropdowns() {
        loadCourses();
        loadSections();
        loadInstructors();
        loadRooms();
    }

    private void loadCourses() {
        SwingWorker<List<CourseResponse>, Void> worker = new SwingWorker<>() {
            @Override protected List<CourseResponse> doInBackground() throws Exception { return admin.listCourses(); }
            @Override protected void done() { try { cbCourse.removeAllItems(); cbCourse.addItem(null); get().forEach(cbCourse::addItem); } catch (Exception e) { LOGGER.log(Level.SEVERE, "Failed to load courses", e); }}
        };
        worker.execute();
    }
    
    private void loadSections() {
        SwingWorker<List<SectionResponse>, Void> worker = new SwingWorker<>() {
            @Override protected List<SectionResponse> doInBackground() throws Exception { return admin.listSections(); }
            @Override protected void done() { try { cbSection.removeAllItems(); cbSection.addItem(null); get().forEach(cbSection::addItem); } catch (Exception e) { LOGGER.log(Level.SEVERE, "Failed to load sections", e); }}
        };
        worker.execute();
    }

    private void loadInstructors() {
        SwingWorker<List<InstructorResponse>, Void> worker = new SwingWorker<>() {
            @Override protected List<InstructorResponse> doInBackground() throws Exception { return admin.listInstructors(); }
            @Override protected void done() { try { cbInstructor.removeAllItems(); cbInstructor.addItem(null); get().forEach(cbInstructor::addItem); } catch (Exception e) { LOGGER.log(Level.SEVERE, "Failed to load instructors", e); }}
        };
        worker.execute();
    }

    private void loadRooms() {
        SwingWorker<List<RoomResponse>, Void> worker = new SwingWorker<>() {
            @Override protected List<RoomResponse> doInBackground() throws Exception { return admin.listRooms(); }
            @Override protected void done() { try { cbRoom.removeAllItems(); cbRoom.addItem(null); get().forEach(cbRoom::addItem); } catch (Exception e) { LOGGER.log(Level.SEVERE, "Failed to load rooms", e); }}
        };
        worker.execute();
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        comboBox.setBackground(Color.WHITE);
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof CourseResponse) setText(((CourseResponse) value).title());
                else if (value instanceof SectionResponse) setText(((SectionResponse) value).sectionId());
                else if (value instanceof InstructorResponse) setText(((InstructorResponse) value).name());
                else if (value instanceof RoomResponse) setText(((RoomResponse) value).roomId());
                else setText("All");
                return this;
            }
        });
    }
}
