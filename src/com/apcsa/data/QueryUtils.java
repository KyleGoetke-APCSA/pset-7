package com.apcsa.data;

public class QueryUtils {

    /////// QUERY CONSTANTS ///////////////////////////////////////////////////////////////

    /*
     * Determines if the default tables were correctly loaded.
     */

    public static final String SETUP_SQL =
        "SELECT COUNT(name) AS names FROM sqlite_master " +
            "WHERE type = 'table' " +
        "AND name NOT LIKE 'sqlite_%'";

    /*
     * Updates the last login timestamp each time a user logs into the system.
     */

    public static final String LOGIN_SQL =
        "SELECT * FROM users " +
            "WHERE username = ?" +
        "AND auth = ?";

    /*
     * Updates the last login timestamp each time a user logs into the system.
     */

    public static final String UPDATE_LAST_LOGIN_SQL =
        "UPDATE users " +
            "SET last_login = ? " +
        "WHERE username = ?";

    /*
     * Retrieves an administrator associated with a user account.
     */

    public static final String GET_ADMIN_SQL =
        "SELECT * FROM administrators " +
            "WHERE user_id = ?";

    /*
     * Retrieves a teacher associated with a user account.
     */

    public static final String GET_TEACHER_SQL =
        "SELECT * FROM teachers " +
            "WHERE user_id = ?";

    /*
     * Retrieves a student associated with a user account.
     */

    public static final String GET_STUDENT_SQL =
        "SELECT * FROM students " +
            "WHERE user_id = ?";

    /*
     * Resets the password associated with any user
     */

    public static final String UPDATE_PASSWORD_SQL =
        "UPDATE users " +
        "SET auth = ? " +
            "WHERE username = ?";

    /*
     * Retrieves all teachers.
     */

    public static final String GET_ALL_TEACHERS_SQL =
        "SELECT * FROM teachers, departments " +
        "WHERE teachers.department_id = departments.department_id " +
        "ORDER BY last_name, first_name";

    public static final String GET_ALL_TEACHERS_BY_DEPT_SQL =
        "SELECT * FROM teachers, departments " +
        "WHERE teachers.department_id = departments.department_id  AND departments.department_id = ?" +
        "ORDER BY last_name, first_name";

    public static final String GET_ALL_STUDENTS_SQL =
        "SELECT * FROM students " +
        "ORDER BY last_name, first_name";

    public static final String GET_STUDENTS_BY_GRADE_SQL =
        "SELECT * FROM students " +
        "WHERE grade_level = ?" +
        "ORDER BY last_name, first_name";

    public static final String GET_STUDENTS_BY_COURSE_SQL =
        "SELECT * FROM students, courses, course_grades " +
        "WHERE courses.course_no = ? AND courses.course_id = course_grades.course_id AND course_grades.student_id = students.student_id " +
        "ORDER BY last_name, first_name";

    public static final String GET_COURSES_SQL =
        "SELECT * FROM courses, teachers " +
        "WHERE teachers.department_id =? AND teachers.department_id = courses.department_id "+
        "ORDER BY courses.course_id";

    public static final String GET_COURSE_NUMBER =
        "SELECT * FROM courses " +
        "WHERE course_id = ?";

    public static final String UPDATE_AUTH_SQL =
        "UPDATE users " +
            "SET auth = ? " +
        "WHERE username = ?";

    public static final String GET_NUMBER_OF_COURSES =
        "SELECT COUNT(*) FROM courses";

    public static final String GET_COURSES_FOR_STUDENT =
        "SELECT * FROM course_grades, courses" +
        "WHERE course_grades.student_id = ? AND course_grades.course_id = courses.course_id" +
        "ORDER BY course_name";

    public static final String GET_STUDENT_GRADE =
        "SELECT grade FROM course_grades " +
        "WHERE course_id = ? " +
        "AND student_id = ?";

    public static final String GET_STUDENT_ID_FROM_COURSE_ID =
        "SELECT student_id FROM course_grades " +
        "WHERE course_id = ?";

    public static final String GET_COURSES =
        "SELECT course_no FROM courses " +
        "WHERE department_id = ?";

    public static final String GET_COURSE_ID =
        "SELECT course_id FROM courses " +
        "WHERE course_no = ?";

    public static final String GET_STUDENTS_BY_STUDENT_ID =
        "SELECT first_name, last_name, gpa FROM students " +
        "WHERE student_id = ?";

    public static final String GET_ASSIGNMENT_IDS =
        "SELECT assignment_id FROM assignments ";

    public static final String GET_STUDENT_COURSES_SQL =
        "SELECT courses.title, grade, courses.course_id, courses.course_no FROM course_grades " +
        "INNER JOIN courses ON course_grades.course_id = courses.course_id " +
        "INNER JOIN students ON students.student_id = course_grades.student_id " +
        "WHERE students.student_id = ?";

    public static final String GET_COURSE_NUMBERS_FOR_STUDENT =
        "SELECT courses.title, grade, courses.course_id, courses.course_no FROM course_grades " +
        "INNER JOIN courses ON course_grades.course_id = courses.course_id " +
        "INNER JOIN students ON students.student_id = course_grades.student_id " +
        "WHERE students.student_id = ?";

    public static final String GET_COURSE_ID_FROM_DEPARTMENT_ID =
        "SELECT course_id FROM courses " +
        "WHERE course_no = ?";

    public static final String ADD_ASSIGNMENT =
        "INSERT INTO assignments " +
        "VALUES(?, ?, ?, ?, ?, ?, ?)";

    public static final String PREVIOUS_ASSIGNMENT_ID =
        "SELECT count(*) FROM assignments ";
}
