package com.iiitd.erp.io.dto.response;

public class Grade {
    private String courseCode;
    private String courseTitle;
    private int credits;
    private String grade;
    private double sgpaImpact;

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public double getSgpaImpact() {
        return sgpaImpact;
    }

    public void setSgpaImpact(double sgpaImpact) {
        this.sgpaImpact = sgpaImpact;
    }
}
