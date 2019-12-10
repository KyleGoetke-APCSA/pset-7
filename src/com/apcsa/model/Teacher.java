package com.apcsa.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.apcsa.model.User;

public class Teacher extends User {

    private int teacherId;
    private int departmentId;
    private String firstName;
    private String lastName;

    public Teacher(User user, ResultSet rs) throws SQLException {
        super(user.getUserId(), user.getAccountType(), user.getUsername(), user.getPassword(), user.getLastLogin());

        this.teacherId = rs.getInt("teacher_id");
        this.departmentId = rs.getInt("department_id");
        this.firstName = rs.getString("first_name");
        this.lastName = rs.getString("last_name");
    }

}