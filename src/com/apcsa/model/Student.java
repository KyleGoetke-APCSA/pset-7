package com.apcsa.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.apcsa.data.PowerSchool;
import com.apcsa.data.QueryUtils;
import com.apcsa.model.User;

public class Student extends User {

    private int studentId;
    private int classRank;
    private int gradeLevel;
    private int graduationYear;
    private double gpa;
    private String firstName;
    private String lastName;

    public Student(ResultSet rs) throws SQLException {
        super(-1, "student", null, null, null);

        this.studentId = rs.getInt("student_id");
        this.classRank = rs.getInt("class_rank");
        this.gradeLevel = rs.getInt("grade_level");
        this.graduationYear = rs.getInt("graduation");
        this.gpa = rs.getDouble("gpa");
        this.firstName = rs.getString("first_name");
        this.lastName = rs.getString("last_name");
    }

    public Student(User user, ResultSet rs) throws SQLException {
        super(user);

        this.studentId = rs.getInt("student_id");
        this.classRank = rs.getInt("class_rank");
        this.gradeLevel = rs.getInt("grade_level");
        this.graduationYear = rs.getInt("graduation");
        this.gpa = rs.getDouble("gpa");
        this.firstName = rs.getString("first_name");
        this.lastName = rs.getString("last_name");
    }

    public String getName() {
        return lastName + ", " + firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getClassRank() {
        return classRank;
    }

    public double getGpa() {
        return gpa;
    }

    public double getStudentId() {
        return studentId;
    }

    public void viewCourseGrades() {
        System.out.print("\n");
        try (Connection conn = PowerSchool.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(QueryUtils.GET_STUDENT_COURSES_SQL);
            stmt.setInt(1, (int) this.getStudentId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println(rs.getString("title") + " / " + rs.getInt("grade"));
                }
            }
        } catch (SQLException e) {
            PowerSchool.shutdown(true);
        }
    }

}
