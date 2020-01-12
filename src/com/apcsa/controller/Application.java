package com.apcsa.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.data.QueryUtils;
import com.apcsa.model.Student;
import com.apcsa.model.Teacher;
import com.apcsa.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
                    System.out.print("\nEnter new password: ");
                    String newPassword = in.next();
                    PowerSchool.changePassword(username, newPassword);
                    System.out.println("\nSuccessfully changed password.");
                ////////////////////////////// ROOT ////////////////////////////
                } else if (activeUser.isRoot()) {
                    boolean validLogin = true;
                    while (validLogin) {
                        System.out.println("\nHello, again, ROOT!\n");
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
                        System.out.printf("\nHello, again, %s!\n\n", firstName);
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
                        System.out.printf("\nHello, again, %s!\n\n", firstName);
                        switch (getSelectionTeacher()) {
                            case TCBYCOURSE:
                                enrollmentByCourse();
                                break;
                            case TCNEWASGN:
                                addAssignment();
                                break;
                            case TCDLTASGN:
                                deleteAssignment();
                                break;
                            case TCNEWGRD:
                                enterGrade();
                                break;
                            case TCCHANGEPWD:
                                resetUserPassword();
                                break;
                            case TCLOGOUT:
                                validLogin = logoutConfirm();
                                in.nextLine();
                                break;
                            default:
                            	System.out.print("\nInvalid selection.\n");
                            	break;
                        }
                    }
                ////////////////////////////// STUDENT ////////////////////////////
                } else if (activeUser.isStudent()) {
                    boolean validLogin = true;
                    while (validLogin) {
                        String firstName = activeUser.getFirstName();
                        System.out.printf("\nHello, again, %s!\n\n", firstName);
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
                System.out.println(i++ + ". " + student.getName() + " / " + student.getGraduationYear());
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
                System.out.println(i++ + ". " + student.getName() + " / #" + student.getClassRank());
            }
        }
    }

    private void viewStudentsByCourse() {
        String courseNo = "";
        try {
        courseNo = getCourseSelection();
        } catch(SQLException e) {

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

        return selection + 8;
    }

    public void enterGrade() {
        boolean hasAssignment = true;
           System.out.println("\nChoose a course.\n");
           int departmentId = ((Teacher) activeUser).getDepartmentId(); //department id of the teacher
           ArrayList<String> courses = PowerSchool.getCourses(departmentId); //all courses
           for (int i = 0; i <= courses.size()-1; i++) {
               System.out.println("[" + (i + 1) + "] " + courses.get(i)); //printing out courses
           }
           System.out.print("\n::: ");
           int courseSelection = in.nextInt();

           if (courseSelection < 1 || courseSelection > courses.size()) {
               while (courseSelection < 1 || courseSelection > courses.size()){
                   System.out.println("\nInvalid selection.");
                   System.out.println("\nChoose a course.\n");
                   for (int i = 0; i <= courses.size()-1; i++) {
                          System.out.println("[" + (i + 1) + "] " + courses.get(i)); //printing out courses
                      }
                System.out.print("\n::: ");
                courseSelection = in.nextInt();
               }
           }
        String courseNo = courses.get(courseSelection-1);
           int courseId = PowerSchool.getCourseIdFromCourseNo(courseNo); //courseId
           printMarkingPeriods();
           int markingPeriod = in.nextInt();//Selected Marking Period
           if (markingPeriod < 1 || markingPeriod > 6) {
               while (markingPeriod < 1 || markingPeriod > 6) {
                   System.out.println("\nInvalid selection.");
                   printMarkingPeriods();
                   markingPeriod = in.nextInt();
               }
           }

           ArrayList<String> assignments = PowerSchool.getAssignments(courseId, markingPeriod);//get courses by courseId and markingPeriod
           if (assignments.isEmpty()) {
               System.out.println("\nThere are no assignments here.");
               hasAssignment = false;
        }
        while (hasAssignment) {
            System.out.println("\nChoose an assignment.\n");
            for (int i = 0; i <= assignments.size()-1; i++) {
                System.out.println("[" + (i + 1) + "] " + assignments.get(i) + " (" + PowerSchool.getPointValue(assignments.get(i)) + " pts)");
            }
            System.out.print("\n::: ");
            int assignmentSelection = in.nextInt();
            if (assignmentSelection < 1 || assignmentSelection > assignments.size()) {
                while (assignmentSelection < 1 || assignmentSelection > assignments.size()) {
                    System.out.println("\nInvlaid selection.");
                    System.out.println("\nChoose an assignment.\n");
                    for (int i = 0; i <= assignments.size()-1; i++) {
                        System.out.println("[" + (i + 1) + "] " + assignments.get(i) + " (" + PowerSchool.getPointValue(assignments.get(i)) + " pts)");
                    }
                    System.out.print("\n::: ");
                    assignmentSelection = in.nextInt();
                }
            }
            String title = assignments.get(assignmentSelection-1); //title of the assignment

            String StringCourseId = PowerSchool.getCourseIdFromCourseNoTwo(courseNo);//courseId
            ArrayList<String> studentIds = PowerSchool.getStudentId(StringCourseId);//getting studentIds enrolled in a course with courseId
            ArrayList<String> students = new ArrayList<String>();
            for (int i = 0; i < studentIds.size(); i++) {
                students.addAll(PowerSchool.getStudentsByStudentId(studentIds.get(i))); //getting students firstname, lastname, and gpa
            }
            ArrayList<String> studentsFirstNames = new ArrayList<String>();
            for (int i = 0; i < studentIds.size(); i++) {
                studentsFirstNames.addAll(PowerSchool.getStudentFirstNames(studentIds.get(i)));
            }
            ArrayList<String> studentLastNames = new ArrayList<String>();
            for (int i = 0; i < studentIds.size(); i++) {
                studentLastNames.addAll(PowerSchool.getStudentLastNames(studentIds.get(i)));
            }
            System.out.println("\nChoose a student.\n");
            for (int i = 0, x = 1; i < students.size(); i = i + 3) {
                System.out.println("[" + (x) + "] " + students.get(i+1) + ", " + students.get(i));
                x++;
            }
            System.out.print("\n::: ");
            int studentSelection = in.nextInt();
            if (studentSelection < 1 || studentSelection > students.size()/3) {
                while (studentSelection < 1 || studentSelection > students.size()/3) {
                    System.out.println("\nInvalid selection.");
                    System.out.println("\nChoose a student.\n");
                    for (int i = 0, x = 1; i < students.size(); i = i + 3) {
                        System.out.println("[" + (x) + "] " + students.get(i+1) + ", " + students.get(i));
                        x++;
                    }
                    System.out.print("\n::: ");
                    studentSelection = in.nextInt();
                }
            }
            String studentLastName = studentLastNames.get(studentSelection-1);
            String studentFirstName = studentsFirstNames.get(studentSelection-1);
            int rows = PowerSchool.assignmentGradesRows();
            System.out.println("\nAssignment: " + title + " (" + PowerSchool.getPointValue(title) + " pts)");
            System.out.println("Student: " + studentLastName + ", " + studentFirstName);
            int assignmentId = PowerSchool.getAssignmentIdFromTitle(title, courseId, markingPeriod);
            int studentId = Integer.parseInt(studentIds.get(studentSelection-1));
            if (rows == 0) {
                System.out.println("Current Grade: --");
            } else {
                if (PowerSchool.previousGrade(courseId, assignmentId, studentId) == -1) {
                    System.out.println("Current Grade: --");
                } else {
                    System.out.println("Current Grade: " + PowerSchool.previousGrade(courseId, assignmentId, studentId));
                }
            }
            System.out.print("\nNew Grade: ");
            double pointsEarned = in.nextDouble();
            if (pointsEarned < 0 || pointsEarned > PowerSchool.getPointValue(title)) {
                while (pointsEarned < 0 || pointsEarned > PowerSchool.getPointValue(title)) {
                    System.out.print("\nChoose a grade between 0 and the possible number of points.\n\nNew Grade: ");
                    pointsEarned = in.nextDouble();
                }
            }
            in.nextLine();
            String wantTo = "you want to enter this grade?";
            System.out.print("\nAre you sure you want to enter this grade? (y/n) ");
            String yesNo = in.nextLine();
            yesNo = yesNo.toLowerCase();
            int checked = checkYesNo(yesNo, wantTo);

            if (checked == -1) {
                System.out.println("");
            } else if (checked == 1) {
                if (PowerSchool.checkGrade(courseId, assignmentId, studentId) == -1) {
                    PowerSchool.addAssignmentGrade(courseId, assignmentId, studentId, pointsEarned, PowerSchool.getPointValue(title), 1);
                    System.out.println("\nSuccesfully entered grade.");
                } else if (PowerSchool.checkGrade(courseId, assignmentId, studentId) == 1) {
                    PowerSchool.updateAssignmentGrade(courseId, assignmentId, studentId, pointsEarned);
                    System.out.println("\nSuccesfully entered grade.");
                }
                ArrayList<Integer> assignmentIds = PowerSchool.getAssignmentIdByMP(markingPeriod);
                ArrayList<Double> grades = new ArrayList<Double>();

                for (int i = 0; i < assignmentIds.size(); i++) {
                    grades.addAll(PowerSchool.getGrades(courseId, assignmentIds.get(i),studentId));
                }
                ArrayList<Double> percent = new ArrayList<Double>();
                for (int i = 0; i < grades.size(); i+=2) {
                    percent.add((grades.get(i)/grades.get(i+1))*100);
                }
                double total = 0;
                for (int i = 0; i < percent.size(); i++) {
                    total += percent.get(i);
                }
                double average = total/percent.size();
                switch (markingPeriod) {
                case 1: PowerSchool.updateCourseGradesMP1(courseId, studentId, average); break;
                case 2: PowerSchool.updateCourseGradesMP2(courseId, studentId, average); break;
                case 3: PowerSchool.updateCourseGradesMP3(courseId, studentId, average); break;
                case 4: PowerSchool.updateCourseGradesMP4(courseId, studentId, average); break;
                case 5: PowerSchool.updateCourseGradesMidterm(courseId, studentId, average); break;
                case 6: PowerSchool.updateCourseGradesFinal(courseId, studentId, average); break;
                default: System.out.println("\nInvalid selection.\n"); break;
                }
            }
            ArrayList<Double> grades = new ArrayList<Double>();
            if (PowerSchool.getMP1Grade(courseId, studentId) == null){
                grades.add(-1.0);
            } else {
                grades.add((Double) PowerSchool.getMP1Grade(courseId, studentId));
            }
            if (PowerSchool.getMP2Grade(courseId, studentId) == null){
                grades.add(-1.0);
            } else {
                grades.add((Double) PowerSchool.getMP2Grade(courseId, studentId));
            }
            if (PowerSchool.getMP3Grade(courseId, studentId) == null){
                grades.add(-1.0);
            } else {
                grades.add((Double) PowerSchool.getMP3Grade(courseId, studentId));
            }
            if (PowerSchool.getMP4Grade(courseId, studentId) == null){
                grades.add(-1.0);
            } else {
                grades.add((Double) PowerSchool.getMP4Grade(courseId, studentId));
            }
            if (PowerSchool.getMidtermGrade(courseId, studentId) == null){
                grades.add(-1.0);
            } else {
                grades.add((Double) PowerSchool.getMidtermGrade(courseId, studentId));
            }

            double grade = Utils.getGrade(grades);
            PowerSchool.updateCourseGrade(courseId, studentId, grade);
            PowerSchool.getCourseGrades(studentId);

            ArrayList<Object> courseGrades = PowerSchool.getCourseGrades(studentId);
            ArrayList<Double> fourScale = new ArrayList<Double>();
            for (int i = 0; i < courseGrades.size(); i++) {
                if ((Double) courseGrades.get(i) == -1.0) {

                } else if ((Double) courseGrades.get(i) >= 93 && (Double) courseGrades.get(i) <= 100) {
                    fourScale.add(4.0);
                } else if ((Double) courseGrades.get(i) >= 90 && (Double) courseGrades.get(i) <= 92) {
                    fourScale.add(3.7);
                } else if ((Double) courseGrades.get(i) >= 87 && (Double) courseGrades.get(i) <= 89) {
                    fourScale.add(3.3);
                } else if ((Double) courseGrades.get(i) >= 83 && (Double) courseGrades.get(i) <= 86) {
                    fourScale.add(3.0);
                } else if ((Double) courseGrades.get(i) >= 80 && (Double) courseGrades.get(i) <= 82) {
                    fourScale.add(2.7);
                } else if ((Double) courseGrades.get(i) >= 77 && (Double) courseGrades.get(i) <= 79) {
                    fourScale.add(2.3);
                } else if ((Double) courseGrades.get(i) >= 73 && (Double) courseGrades.get(i) <= 76) {
                    fourScale.add(2.0);
                } else if ((Double) courseGrades.get(i) >= 70 && (Double) courseGrades.get(i) <= 72) {
                    fourScale.add(1.7);
                } else if ((Double) courseGrades.get(i) >= 67 && (Double) courseGrades.get(i) <= 69) {
                    fourScale.add(1.3);
                } else if ((Double) courseGrades.get(i) >= 65 && (Double) courseGrades.get(i) <= 66) {
                    fourScale.add(1.0);
                } else if ((Double) courseGrades.get(i) > 65) {
                    fourScale.add(0.0);
                }
            }
            ArrayList<Integer> courseIds = PowerSchool.getCourseIds(studentId);
            ArrayList<Integer> creditHours = PowerSchool.getCreditHours(courseIds);
            int totalGradePoints = 0;
            int hours = 0;
            for (int i = 0; i < fourScale.size(); i++) {
                totalGradePoints += fourScale.get(i)*creditHours.get(i);
                hours += creditHours.get(i);
            }
            double gpa = (double) (totalGradePoints)/ (double) hours;
            double roundedGpa = Math.round(gpa * 100.0) / 100.0;
            PowerSchool.updateGPA(roundedGpa, studentId);
            hasAssignment = false;
        }
    }

    public void addAssignment() {
        System.out.println("\nChoose a course.\n");
        int departmentId = ((Teacher) activeUser).getDepartmentId();
        ArrayList<String> courses = PowerSchool.getCourses(departmentId);
        for (int i = 0; i <= courses.size()-1; i++) {
            System.out.println("[" + (i + 1) + "] " + courses.get(i));
        }

        System.out.print("\n::: ");
        int courseSelection = in.nextInt();
        while (courseSelection > courses.size() || courseSelection < 1) {
            System.out.println("\nInvalid selection.");
            System.out.println("\nChoose a course.\n");
            for (int i = 0; i <= courses.size()-1; i++) {
                System.out.println("[" + (i + 1) + "] " + courses.get(i));
            }

            System.out.print("\n::: ");
            courseSelection = in.nextInt();
        }

        String courseNo = courses.get(courseSelection-1);
        int courseId = PowerSchool.getCourseIdFromCourseNo(courseNo);
        printMarkingPeriods();
        int markingPeriod = in.nextInt();
        while (markingPeriod > 6 || markingPeriod < 1) {
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
        System.out.print("Point Value: ");
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

    private void deleteAssignment() {
        int courseId = getCourseId();
        System.out.println("\nChoose a marking period or exam status.\n");
        System.out.println("[1] MP1 assignment.");
        System.out.println("[2] MP2 assignment.");
        System.out.println("[3] MP3 assignment.");
        System.out.println("[4] MP4 assignment.");
        System.out.println("[5] Midterm exam.");
        System.out.println("[6] Final exam.");
        System.out.print("\n::: ");
        int markingPeriod = Utils.getInt(in, -1);
        while (markingPeriod <= 0 || markingPeriod > 6) {
            if (markingPeriod <= 0 || markingPeriod > 6) {
                System.out.println("\nInvalid Selection.");
                }
            System.out.println("\nChoose a marking period or exam status.\n");
            System.out.println("[1] MP1 assignment.");
            System.out.println("[2] MP2 assignment.");
            System.out.println("[3] MP3 assignment.");
            System.out.println("[4] MP4 assignment.");
            System.out.println("[5] Midterm exam.");
            System.out.println("[6] Final exam.");
            System.out.print("\n::: ");
            markingPeriod = Utils.getInt(in, -1);
         }
         ArrayList<String> assignments = PowerSchool.getAssignments(courseId, markingPeriod);
         ArrayList<String> pointValues = PowerSchool.getPointValues(courseId, markingPeriod);

         System.out.println();
         if (!assignments.isEmpty()) {
             int assignmentSelection = -1;
                while (assignmentSelection <= 0 || assignmentSelection > assignments.size()) {
                    int j = 1;
                    for (String i: assignments) {
                        System.out.println("["+ j++ + "] " + i + " (" + pointValues.get(j-2) + " pts)");
                    }
                    System.out.print("\n::: ");
                    assignmentSelection = Utils.getInt(in, -1);
                    if (assignmentSelection <= 0 || assignmentSelection > assignments.size()) {
                        System.out.println("\nInvalid Selection.\n");
                    }
                }
                String title = assignments.get(assignmentSelection-1);
                if (Utils.confirm(in, "\nAre you sure you want to delete this assignment? (y/n) ")) {
                    if (PowerSchool.deleteAssignment(courseId, markingPeriod, title) == 1) {
                        System.out.println("\nSuccessfully deleted " + title + ".");
                    } else {
                        System.out.println("\nError deleting assignment.");
                    }
                }
         } else {
             System.out.println("No assignments.");
         }

    }

    private int getCourseId() {
        String courseNumber = getCourseSelectionTeacher();
        return PowerSchool.getCourseId(courseNumber);
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

    private String getCourseSelectionTeacher() {
        Teacher teacher = PowerSchool.getTeacher(activeUser);
        ArrayList<String> courses = PowerSchool.getCourses(teacher.getDepartmentId());
        System.out.println();
        int courseSelection = -1;
        while (courseSelection <= 0 || courseSelection > courses.size()) {
            int j = 1;
            for (String i: courses) {
                System.out.println("["+ j++ + "] " + i);
            }
            System.out.print("\n::: ");
            courseSelection = Utils.getInt(in, -1);
            if (courseSelection <= 0 || courseSelection > courses.size()) {
                System.out.println("\nInvalid Selection.\n");
            }
        }
        return courses.get(courseSelection-1);
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
