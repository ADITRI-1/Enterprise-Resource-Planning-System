package com.iiitd.erp.core.service.support;

import com.iiitd.erp.Domain.AcademicRecordView;
import com.iiitd.erp.Domain.AcademicRecordView.CourseRecord;
import com.iiitd.erp.Domain.AcademicRecordView.TermRecord;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Renders an academic record as an A4 PDF transcript (colours match the web app). */
public final class TranscriptPdf {

    private static final Color NAVY = new Color(9, 53, 82);
    private static final Color TEAL = new Color(85, 172, 168);
    private static final Color MUTED = new Color(90, 100, 117);
    private static final Color RULE = new Color(220, 225, 235);
    private static final Color STRIPE = new Color(245, 248, 251);

    private static final Font TITLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
    private static final Font SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 9, TEAL);
    private static final Font H2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, NAVY);
    private static final Font LABEL = FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED);
    private static final Font VALUE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font HEAD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, MUTED);
    private static final Font CELL = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font CELL_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, NAVY);
    private static final Font SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8, MUTED);

    private TranscriptPdf() {}

    public static byte[] render(AcademicRecordView record) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 40, 40, 40, 50);
        PdfWriter.getInstance(doc, out);
        doc.addTitle("Transcript - " + record.studentName());
        doc.open();

        doc.add(banner());
        doc.add(studentDetails(record));

        if (record.terms().isEmpty()) {
            doc.add(new Paragraph("No courses taken yet.", CELL));
        }
        for (TermRecord term : record.terms()) {
            Paragraph heading = new Paragraph(label(term.semester()) + " " + term.year(), H2);
            heading.setSpacingBefore(14);
            heading.setSpacingAfter(6);
            doc.add(heading);
            doc.add(termTable(term));
        }

        Paragraph footer = new Paragraph(
                "Grade points: A 10, A- 9, B 8, B- 7, C 6, C- 5, D 4, F 0. SGPA/CGPA = sum(points x credits) / sum(credits) "
                        + "over graded courses. Generated " + LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")) + ".",
                SMALL);
        footer.setSpacingBefore(18);
        doc.add(footer);
        doc.close();
        return out.toByteArray();
    }

    private static PdfPTable banner() {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(NAVY);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(14);
        c.addElement(new Paragraph("University ERP", TITLE));
        c.addElement(new Paragraph("ACADEMIC TRANSCRIPT", SUBTITLE));
        t.addCell(c);
        t.setSpacingAfter(14);
        return t;
    }

    private static PdfPTable studentDetails(AcademicRecordView r) {
        PdfPTable t = new PdfPTable(new float[]{1, 1, 1, 1});
        t.setWidthPercentage(100);
        detail(t, "Name", r.studentName());
        detail(t, "Roll number", r.rollNo());
        detail(t, "Program", r.programId());
        detail(t, "CGPA", r.cgpa() == null ? "-" : String.format(Locale.ROOT, "%.2f", r.cgpa()));
        detail(t, "Credits earned", String.valueOf(r.creditsEarned()));
        detail(t, "Credits graded", String.valueOf(r.creditsGraded()));
        detail(t, "", "");
        detail(t, "", "");
        return t;
    }

    private static void detail(PdfPTable t, String label, String value) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPaddingBottom(8);
        c.addElement(new Paragraph(label, LABEL));
        c.addElement(new Paragraph(value == null ? "-" : value, VALUE));
        t.addCell(c);
    }

    private static PdfPTable termTable(TermRecord term) {
        PdfPTable t = new PdfPTable(new float[]{1.2f, 3.6f, 0.9f, 0.9f, 0.9f});
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        for (String h : new String[]{"COURSE", "TITLE", "CREDITS", "GRADE", "POINTS"}) {
            t.addCell(cell(h, HEAD, null, Rectangle.BOTTOM));
        }
        boolean stripe = false;
        for (CourseRecord c : term.courses()) {
            Color bg = stripe ? STRIPE : null;
            t.addCell(cell(c.courseId(), CELL_BOLD, bg, Rectangle.NO_BORDER));
            t.addCell(cell(c.title(), CELL, bg, Rectangle.NO_BORDER));
            t.addCell(cell(String.valueOf(c.credits()), CELL, bg, Rectangle.NO_BORDER));
            t.addCell(cell(c.grade() == null ? "In progress" : c.grade(), c.grade() == null ? SMALL : CELL_BOLD, bg, Rectangle.NO_BORDER));
            t.addCell(cell(c.gradePoints() == null ? "-" : String.valueOf(c.gradePoints()), CELL, bg, Rectangle.NO_BORDER));
            stripe = !stripe;
        }
        PdfPCell summary = cell("SGPA " + (term.sgpa() == null ? "-" : String.format(Locale.ROOT, "%.2f", term.sgpa()))
                + "   ·   " + term.creditsGraded() + " credits graded", CELL_BOLD, null, Rectangle.TOP);
        summary.setColspan(5);
        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(summary);
        return t;
    }

    private static PdfPCell cell(String text, Font font, Color background, int border) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "-" : text, font));
        c.setBorder(border);
        c.setBorderColor(RULE);
        c.setPadding(6);
        if (background != null) c.setBackgroundColor(background);
        return c;
    }

    private static String label(String semester) {
        return semester == null ? "" : semester.charAt(0) + semester.substring(1).toLowerCase(Locale.ROOT);
    }
}
