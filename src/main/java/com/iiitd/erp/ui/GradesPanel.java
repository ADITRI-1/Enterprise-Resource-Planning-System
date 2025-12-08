package com.iiitd.erp.ui;

import com.iiitd.erp.io.dto.response.GradeResponse;
import com.iiitd.erp.ui.client.StudentClient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GradesPanel extends JPanel {

    private final StudentClient student;
    private JTable gradesTable;
    private TableRowSorter<DefaultTableModel> sorter;
    private static final Logger LOGGER = Logger.getLogger(GradesPanel.class.getName());
    private List<GradeResponse> gradeList;

    public GradesPanel(StudentClient student) {
        this.student = student;
        setOpaque(false);
        setLayout(new BorderLayout(0, 15));
        setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("My Grades");
        title.setFont(new Font("Segoe UI", Font.BOLD, 32));
        title.setForeground(new Color(40, 50, 65));
        title.setBorder(new EmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
        contentPanel.setOpaque(false);
        
        contentPanel.add(createFilterBar(), BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Course", "Component", "Weightage", "Score", "Final Grade"}, 0);
        gradesTable = new JTable(model);
        sorter = new TableRowSorter<>(model);
        gradesTable.setRowSorter(sorter);
        styleTable(gradesTable);
        contentPanel.add(new JScrollPane(gradesTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exportCsvButton = new JButton("Export as CSV");
        exportCsvButton.addActionListener(e -> exportToCsv());
        buttonPanel.add(exportCsvButton);

        JButton exportPdfButton = new JButton("Export as PDF");
        exportPdfButton.addActionListener(e -> exportToPdf());
        buttonPanel.add(exportPdfButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        loadGrades();
    }

    private JPanel createFilterBar() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        
        JTextField filterField = new JTextField(30);
        filterField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        filterField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 8, 5, 8)
        ));

        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void insertUpdate(DocumentEvent e) { filter(); }
            private void filter() {
                String text = filterField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterField);
        return filterPanel;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
    }

    private void loadGrades() {
        SwingWorker<List<GradeResponse>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<GradeResponse> doInBackground() throws Exception {
                return student.getMyGrades();
            }

            @Override
            protected void done() {
                try {
                    gradeList = get();
                    DefaultTableModel model = (DefaultTableModel) gradesTable.getModel();
                    model.setRowCount(0);
                    for (GradeResponse grade : gradeList) {
                        model.addRow(new Object[]{grade.getCourseName(), grade.getComponent(), grade.getWeightage(), grade.getScore(), grade.getFinalGrade()});
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to load grades", e);
                }
            }
        };
        worker.execute();
    }

    private void exportToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave)) {
                for (int i = 0; i < gradesTable.getColumnCount(); i++) {
                    writer.append(gradesTable.getColumnName(i));
                    if (i < gradesTable.getColumnCount() - 1) {
                        writer.append(',');
                    }
                }
                writer.append('\\n');

                for (int i = 0; i < gradesTable.getRowCount(); i++) {
                    for (int j = 0; j < gradesTable.getColumnCount(); j++) {
                        writer.append(gradesTable.getValueAt(i, j).toString());
                        if (j < gradesTable.getColumnCount() - 1) {
                            writer.append(',');
                        }
                    }
                    writer.append('\\n');
                }
                JOptionPane.showMessageDialog(this, "CSV file exported successfully.", "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to export to CSV", e);
                JOptionPane.showMessageDialog(this, "Error exporting to CSV: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToPdf() {
        // PDF export logic would go here.
        // This is a placeholder as it requires a PDF library.
        JOptionPane.showMessageDialog(this, "PDF export is not yet implemented.", "Not Implemented", JOptionPane.INFORMATION_MESSAGE);
    }
}
