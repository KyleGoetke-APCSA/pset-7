package com.apcsa.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.apcsa.model.User;

public class Administrator extends User {

    private int administratorId;
    private String firstName;
    private String lastName;
    private String jobTitle;

    public Administrator(User user, ResultSet rs) throws SQLException {
        super(user);

        this.administratorId = rs.getInt("administrator_id");
        this.firstName = rs.getString("first_name");
        this.lastName = rs.getString("last_name");
        this.jobTitle = rs.getString("job_title");
    }

    public String getFirstName() {
        return this.firstName;
    }

}
