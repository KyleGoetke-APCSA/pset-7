package com.apcsa.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.model.Student;
import com.apcsa.model.Teacher;
import com.apcsa.model.User;
import java.sql.Connection;

public class Application {

    private Scanner in;
    private User activeUser;
    public static boolean running;

    public static final int RTCHANGEPWD = 1;    // ROOT - reset user password
    public static final int RTRESETDB = 2;      // ROOT - factory reset database
    public static final int RTLOGOUT = 3;       // ROOT - logout
    public static final int RTSHUTDOWN = 4;     // ROOT - shut down
    public static final int ADBYFAC = 1;        // ADMIN - view faculty
    public static final int ADBYDEP = 2;        // ADMIN - view by department (#22)
    public static final int ADBYENROLL = 3;     // ADMIN - view student enrollment
    public static final int ADBYGRADE = 4;      // ADMIN - view by grade
    public static final int ADBYCOURSE = 5;     // ADMIN - view by course
    public static final int ADCHANGEPWD = 6;    // ADMIN - change password
    public static final int ADLOGOUT = 7;       // ADMIN - logout
    public static final int TCBYCOURSE = 1;     // TEACHER - view enrollment by course
    public static final int TCNEWASGN = 2;      // TEACHER - add assignment
    public static final int TCDLTASGN = 3;      // TEACHER - delete assignment
    public static final int TCNEWGRD = 4;       // TEACHER - enter grade
    public static final int TCCHANGEPWD = 5;    // TEACHER - change password
    public static final int TCLOGOUT = 6;       // TEACHER - logout
    public static final int STVIEWGRD = 1;      // STUDENT - view course grades
    public static final int STBYCOURSE = 2;     // STUDENT - view assignment grades by course
    public static final int STCHANGEPWD = 3;    // STUDENT - change password
    public static final int STLOGOUT = 4;       // STUDENT - logout

    /**
     * Creates an instance of the Application class, which is responsible for interacting
     * with the user via the command line interface.
     */

    public Application() {
        this.in = new Scanner(System.in);

        try {
            PowerSchool.initialize(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the PowerSchool application.
     */

    public void startup() {
        System.out.println("PowerSchool -- now for students, teachers, and school administrators!");

        // continuously prompt for login credentials and attempt to login

        while (true) {
            System.out.print("\nUsername: ");
            String username = in.next();

            System.out.print("Password: ");
            String password = in.next();

            // if login is successful, update generic user to administrator, teacher, or student

            if (login(username, password)) {
                activeUser = activeUser.isAdministrator()
                    ? PowerSchool.getAdministrator(activeUser) : activeUser.isTeacher()
                    ? PowerSchool.getTeacher(activeUser) : activeUser.isStudent()
                    ? PowerSchool.getStudent(activeUser) : activeUser.isRoot()
                    ? activeUser : null;

                if (isFirstLogin() && !activeUser.isRoot()) {
                    // first-time users need to change their passwords from the default provided
                    System.out.print("\nAs a new user, you must change your password. \n\nEnter your new password: ");
                    String newPassword = in.next();
                    PowerSchool.changePassword(username, newPassword);
                ////////////////////////////// ROOT ////////////////////////////
                } else if (activeUser.isRoot()) {
                    boolean validLogin = true;
                    while (validLogin) {
                        System.out.println("\nHello again, ROOT!\n");
                        switch (getSelectionRoot()) {
                            case RTCHANGEPWD:
                                resetPassword();
                                break;
                            case RTRESETDB:
                                System.out.print("\nAre you sure you want to reset all settings and data? (y/n) ");
                                String resetDecision = in.next();
                                if (resetDecision.equals("y")) {
                                    PowerSchool.reset();
                                    System.out.println("\nSuccessfully reset database.");
                                }
                                break;
                            case RTSHUTDOWN:
                                rootShutdown();
                                break;
                            case RTLOGOUT:
                                validLogin = logoutConfirm();
                                in.nextLine();
                                break;
                            default:
                                System.out.print("\nInvalid selection.\n");
                                break;
                        }
                    }
                ////////////////////////////// ADMINISTRATOR ////////////////////////////
                } else if (activeUser.isAdministrator()) {
                    boolean validLogin = true;
                    while (validLogin) {
                        String firstName = activeUser.getFirstName();
                        System.out.printf("\nHello again, %s!\n\n", firstName);
                        switch (getSelectionAdministrator()) {
                            case ADBYFAC:
                                viewFaculty();
                                break;
                            case ADBYDEP:
                                viewDepartments();
                                break;
                            case ADBYENROLL:
                                viewStudents();
                                break;
                            case ADBYGRADE:
                                viewStudentsByGrade();
                                break;
                            case ADBYCOURSE:
                                viewStudentsByCourse();
                                break;
                            case ADCHANGEPWD:
                                resetUserPassword();
                                break;
                            case ADLOGOUT:
                                validLogin = logoutConfirm();
                                in.nextLine();
                            break;
                            default:
                                System.out.print("\nInvalid selection.\n");
                                break;
                        }
                    }
                ////////////////////////////// TEACHER ////////////////////////////
                } else if (activeUser.isTeacher()) {
                    boolean validLogin = true;
                    while (validLogin) {
                        String firstName = activeUser.getFirstName();
                        System.out.printf("\nHello again, %s!\n\n", firstName);
                        switch (getSelectionTeacher()) {
                            case TCBYCOURSE:
                                enrollmentByCourse();
                                break;
                            case TCNEWASGN:
                                addAssignment();
                                break;
                            case TCDLTASGN:
                                System.out.print("\ndelete assignment\n");
                                break;
                            case TCNEWGRD:
                                System.out.print("\nenter grade\n");
                                break;
                            case TCCHANGEPWD:
                                resetUserPassword();
                                break;
                            case TCLOGOUT:
                                validLogin = logoutConfirm();
                                in.nextLine();
                                break;
                            default: System.out.print("\nInvalid selection.\n"); break;
                        }
                    }
                ////////////////////////////// STUDENT ////////////////////////////
                } else if (activeUser.isStudent()) {
                    boolean validLogin = true;
                    while (validLogin) {
                        String firstName = activeUser.getFirstName();
                        System.out.printf("\nHello again, %s!\n\n", firstName);
                        switch (getSelectionStudent()) {
                            case STVIEWGRD:
                                ((Student) activeUser).viewCourseGrades();
                                break;
                            case STBYCOURSE:
                                ((Student) activeUser).viewAssignmentGradesByCourse(in);
                                break;
                            case STCHANGEPWD:
                                resetUserPassword();
                                break;
                            case STLOGOUT:
                                validLogin = logoutConfirm();
                                in.nextLine();
                                break;
                            default:
                                System.out.print("\nInvalid selection.\n");
                                break;
                        }
                    }
                }
            } else {
                System.out.println("\nInvalid username and/or password.");
            }
        }
    }

    /**
     * Logs in with the provided credentials.
     *
     * @param username the username for the requested account
     * @param password the password for the requested account
     * @return true if the credentials were valid; false otherwise
     */

    public void rootShutdown() {
        System.out.print("\nAre you sure? (y/n) ");
        String shutdownDecision = in.next();
        if (shutdownDecision.equals("y")) {
            if (in != null) {
                in.close();
            }
            System.out.println("\nGoodbye!");
            System.exit(0);
        }
    }

    private void resetPassword() {
        System.out.print("\nUsername: ");
        String username = in.next();
        if (Utils.confirm(in, "\nAre you sure you want to reset the password for " + username + "?  (y/n) ")) {
            if (in != null) {
                if (PowerSchool.resetPassword(username)) {
                    PowerSchool.resetLastLogin(username);
                    System.out.println("\nSuccessfully reset password for " + username + ".");
                } else {
                    System.out.println("\nPassword reset failed");
                }
            }
        }
    }

    public void resetUserPassword() {
        System.out.print("\nEnter current password: ");
        String oldPassword = in.next();
        System.out.print("Enter new password: ");
        String newPassword = in.next();
        if (Utils.getHash(oldPassword).equals(activeUser.getPassword())) {
            PowerSchool.changePassword(activeUser.getUsername(), Utils.getHash(newPassword));
            System.out.println("\nSuccessfully changed password.");
        } else if (!(oldPassword.equals(activeUser.getPassword()))) {
            System.out.println("\nInvalid current password.");
        }
    }

    public boolean logoutConfirm() {
        System.out.print("\nAre you sure you want to logout? (y/n) ");
        String logoutDecision = in.next();
        if (logoutDecision.equals("y")) {
            return false;
        } else {
            return true;
        }
    }

    private void viewFaculty() {
        ArrayList<Teacher> teachers = PowerSchool.getTeachers();

        if (teachers.isEmpty()) {
            System.out.println("\nNo teachers to display.");
        } else {
            System.out.println();

            int i = 1;
            for (Teacher teacher : teachers) {
                System.out.println(i++ + ". " + teacher.getName() + " / " + teacher.getDepartmentName());
            }
        }
    }

    private void viewDepartments() {
        ArrayList<Teacher> teachers = PowerSchool.getTeachersByDept(getDepartmentSelection());

        if (teachers.isEmpty()) {
            System.out.println("\nNo teachers to display.");
        } else {
            System.out.println();

            int i = 1;
            for (Teacher teacher : teachers) {
                System.out.println(i++ + ". " + teacher.getName() + " / " + teacher.getDepartmentName());
            }
        }

    }

    private int getDepartmentSelection() {
        int selection = -1;
        System.out.println("\nChoose a department.");

        while (selection < 1 || selection > 6) {
            System.out.println("\n[1] Computer Science.");
            System.out.println("[2] English.");
            System.out.println("[3] History.");
            System.out.println("[4] Mathematics.");
            System.out.println("[5] Physical Education.");
            System.out.println("[6] Science.");
            System.out.print("\n::: ");

            selection = Utils.getInt(in, -1);
        }

        return selection;
    }

    private void viewStudents() {
        ArrayList<Student> students = PowerSchool.getStudents();

        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();

            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName());
            }
        }
    }

    private void viewStudentsByGrade() {
        ArrayList<Student> students = PowerSchool.getStudentsByGrade(getGradeSelection());

        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();

            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + student.getClassRank());
            }
        }
    }

    private void viewStudentsByCourse() {
        String courseNo = "";
        try {
        courseNo = getCourseSelection();
        }catch(SQLException e) {

        }
        ArrayList<Student> students = PowerSchool.getStudentsByCourse(courseNo);

        if (students.isEmpty()) {
            System.out.println("\nNo students to display.");
        } else {
            System.out.println();

            int i = 1;
            for (Student student : students) {
                System.out.println(i++ + ". " + student.getName() + " / " + fixGPA(student));
            }
        }
    }

    /*
     * Retrieves a user's course selection.
     *
     * @return the selected course
     */

    private String getCourseSelection() throws SQLException {
        boolean valid = false;
        String courseNo = null;

        while (!valid) {
            System.out.print("\nCourse No.: ");
            courseNo = in.next();

            if (isValidCourse(courseNo)) {
                valid = true;
            } else {
                System.out.println("\nCourse not found.");
            }
        }

        return courseNo;
    }

    private String fixGPA(Student student) {
        double GPA = student.getGpa();
        if (GPA == -1) {
            return "--";
        } else {
            return String.valueOf(GPA);
        }
    }

    private boolean isValidCourse(String courseId) {
        boolean validCourse = false;
        for (int i=1; i <  PowerSchool.getNumberOfCourses(); i++) {
            if (PowerSchool.getCourseNumber(i).equals(courseId)) {
                validCourse = true;
            }
        }
        return validCourse;
    }

    /*
     * Retrieves a user's grade selection.
     *
     * @return the selected grade
     */

    private int getGradeSelection() {
        int selection = -1;
        System.out.println("\nChoose a grade level.");

        while (selection < 1 || selection > 4) {
            System.out.println("\n[1] Freshman.");
            System.out.println("[2] Sophomore.");
            System.out.println("[3] Junior.");
            System.out.println("[4] Senior.");
            System.out.print("\n::: ");

            selection = Utils.getInt(in, -1);
        }

        return selection + 8;   // +8 because you want a value between 9 and 12
    }

    public void addAssignment() {
        System.out.println("\nChoose a course.\n");
        int departmentId = ((Teacher) activeUser).getDepartmentId();
        ArrayList<String> courses = PowerSchool.getCoursesFromDepartment(departmentId);
        for(int i = 0; i <= courses.size()-1; i++) {
            System.out.println("[" + (i + 1) + "] " + courses.get(i));
        }
        System.out.print("\n::: ");
        int courseSelection = in.nextInt();
        while(courseSelection > courses.size() || courseSelection < 1) {
            System.out.println("\nInvalid selection.");
            System.out.println("\nChoose a course.\n");
            for(int i = 0; i <= courses.size()-1; i++) {
                System.out.println("[" + (i + 1) + "] " + courses.get(i));
            }
            System.out.print("\n::: ");
            courseSelection = in.nextInt();
        }

        String courseNo = courses.get(courseSelection-1);
        int courseId = PowerSchool.getCourseIdFromCourseNo(courseNo);

        printMarkingPeriods();
        int markingPeriod = in.nextInt();

        while(markingPeriod > 6 || markingPeriod < 1) {
            System.out.println("\nInvalid selection.");
            printMarkingPeriods();
            markingPeriod = in.nextInt();
        }

        int isMidterm = 0;
        int isFinal = 0;
        if (markingPeriod == 5) {
            isMidterm = 1;
        } else if (markingPeriod == 6) {
            isFinal = 1;
        }

        in.nextLine();
        System.out.print("\nAssignment Title: ");
        String title = in.nextLine();

        System.out.print("\nPoint Value: ");
        int pointValue = in.nextInt();

        while (pointValue > 100 || pointValue < 1) {
            System.out.println("\nPoint values must be between 1 and 100.");
            System.out.print("\nPoint Value: ");
            pointValue = in.nextInt();
        }

        int assignmentId = 0;
        in.nextLine();
        String wantTo = "you want to create this assignment?";
        System.out.print("\nAre you sure you want to create this assignment? (y/n) ");
        String yesNo = in.nextLine();
        yesNo = yesNo.toLowerCase();
        int checked = checkYesNo(yesNo, wantTo);

        if (checked == -1) {
            System.out.println("");
        } else if (checked == 1) {
            int rows = PowerSchool.assignmentRows();
            if (rows == 0) {
                assignmentId = 1;
            } else {
                ArrayList<String> assignmentIds = PowerSchool.getAssignmentIds();
                String lastAssignmentId = assignmentIds.get(assignmentIds.size() - 1);
                assignmentId = Integer.parseInt(lastAssignmentId) + 1;
            }
            PowerSchool.addAssignment(courseId, assignmentId, markingPeriod, isMidterm, isFinal, title, pointValue);
            System.out.println("\nSuccessfully created assignment.");
        }
    }

    public static void printMarkingPeriods() {
        System.out.println("\nChoose a marking period or exam status.\n");
        System.out.println("[1] MP1 assignment.");
        System.out.println("[2] MP2 assignment.");
        System.out.println("[3] MP3 assignment.");
        System.out.println("[4] MP4 assignment.");
        System.out.println("[5] Midterm exam.");
        System.out.println("[6] Final exam.");
        System.out.print("\n::: ");
    }

    public int getSelectionRoot() {
        int rootDecision;
        System.out.println("[1] Reset user password.");
        System.out.println("[2] Factory reset database.");
        System.out.println("[3] Logout.");
        System.out.println("[4] Shutdown.");
        System.out.print("\n::: ");


        if (in.hasNextInt()) {
            rootDecision = in.nextInt();
            return rootDecision;
        } else {
            in.next();
            return 10;
        }
    }

    public int getSelectionAdministrator() {
        int adminDecision;
        System.out.println("[1] View faculty.");
        System.out.println("[2] View faculty by department.");
        System.out.println("[3] View student enrollment.");
        System.out.println("[4] View student enrollment by grade.");
        System.out.println("[5] View student enrollment by course.");
        System.out.println("[6] Change password.");
        System.out.println("[7] Logout.");
        System.out.print("\n::: ");

        if (in.hasNextInt()) {
            adminDecision = in.nextInt();
            return adminDecision;
        } else {
            in.next();
            return 10;
        }
    }

    private int checkYesNo(String yesNo, String wantTo) {
        if (!yesNo.equals("y") && !yesNo.equals("n")) {
            while (!yesNo.equals("y") && !yesNo.equals("n")) {
                System.out.println("\nInvalid selection.\n");
                System.out.print("\nAre you sure " + wantTo +" (y/n) ");
                yesNo = in.nextLine();
                yesNo = yesNo.toLowerCase();
            }
            if ((yesNo.equals("n"))) {
                return -1;
            } else if (yesNo.equals("y")) {
                return 1;
            }
        } else if (yesNo.equals("n")) {
            return -1;
        } else if (yesNo.equals("y")) {
            return 1;
        }
        return 0;
    }

    public int getSelectionTeacher() {
        int teacherDecision;
        System.out.println("[1] View enrollment by course.");
        System.out.println("[2] Add assignment.");
        System.out.println("[3] Delete assignment.");
        System.out.println("[4] Enter grade.");
        System.out.println("[5] Change password.");
        System.out.println("[6] Logout.");
        System.out.print("\n::: ");

        if (in.hasNextInt()) {
            teacherDecision = in.nextInt();
            return teacherDecision;
        } else {
            in.next();
            return 10;
        }
    }

    public int getSelectionStudent() {
        int studentDecision;
        System.out.println("[1] View course grades.");
        System.out.println("[2] View assignment grades by course.");
        System.out.println("[3] Change password.");
        System.out.println("[4] Logout.");
        System.out.print("\n::: ");

        if (in.hasNextInt()) {
            studentDecision = in.nextInt();
            return studentDecision;
        } else {
            in.next();
            return 10;
        }
    }

    public void enrollmentByCourse() {
        System.out.println("\nChoose a course.\n");
        int departmentId = ((Teacher) activeUser).getDepartmentId();
        ArrayList<String> courses = PowerSchool.getCoursesFromDepartment(departmentId);
        for (int i = 0; i <= courses.size()-1; i++) {
            System.out.println("[" + (i + 1) + "] " + courses.get(i));
        }
        System.out.print("\n::: ");
        int courseSelection = in.nextInt();
        if (courseSelection < 1 || courseSelection > courses.size()) {
            while (courseSelection < 1 || courseSelection > courses.size()) {
                System.out.println("\nInvalid selection.");
                System.out.println("\nChoose a course.\n");
                for (int i = 0; i <= courses.size()-1; i++) {
                    System.out.println("[" + (i + 1) + "] " + courses.get(i));
                }
                System.out.print("\n::: ");
                courseSelection = in.nextInt();
            }
        }
        String courseNo = courses.get(courseSelection-1);
        String courseId = String.valueOf(PowerSchool.getCourseId(courseNo));
        ArrayList<String> studentIds = PowerSchool.getStudentId(courseId);
        ArrayList<String> students = new ArrayList<String>();
        for (int i = 0; i < studentIds.size(); i++) {
            students.addAll(PowerSchool.getStudentsByStudentId(studentIds.get(i)));
        }
        ArrayList<String> studentGrades = new ArrayList<String>();
        for (int i = 0; i < studentIds.size(); i++) {
            studentGrades.add(PowerSchool.getStudentGrade(courseId, studentIds.get(i)));
        }
        if (studentIds.isEmpty()) {
            System.out.println("\nThere are no students in this course.");
        } else {
            System.out.println("");
            for (int i = 0, x = 0; i < students.size(); i = i + 3) {
                String studentGrade = studentGrades.get(x);
                if (studentGrade == null) {
                    studentGrade = "--";
                }
                System.out.println((x+1) + ". " + students.get(i+1) + ", " + students.get(i) + " / " + studentGrade);
                x += 1;
            }
            System.out.println("");
        }
    }

    public boolean login(String username, String password) {
        activeUser = PowerSchool.login(username, password);

        return activeUser != null;
    }

    /**
     * Determines whether or not the user has logged in before.
     *
     * @return true if the user has never logged in; false otherwise
     */

    public boolean isFirstLogin() {
        return activeUser.getLastLogin().equals("0000-00-00 00:00:00.000");
    }

    //////// MAIN METHOD //////////////////////////////////////////////////////////////////

    /*
     * Starts the PowerSchool application.
     *
     * @param args unused command line argument list
     */

    public static void main(String[] args) {
        Application app = new Application();

        app.startup();
    }
}
