package com.apcsa.controller;

import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.model.User;

public class Application {

    private Scanner in;
    private User activeUser;

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
    public static final int TCDLTASGN = 3;	    // TEACHER - delete assignment
    public static final int TCNEWGRD = 4;       // TEACHER - enter grade
    public static final int TCCHANGEPWD = 5;	// TEACHER - change password
    public static final int TCLOGOUT = 6;       // TEACHER - logout
    public static final int STVIEWGRD = 1;      // STUDENT - view course grades
    public static final int STBYCOURSE = 2;	    // STUDENT - view assignment grades by course
    public static final int STCHANGEPWD = 3;	// STUDENT - change password
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
                activeUser = activeUser.isAdministrator() ? PowerSchool.getAdministrator(activeUser)
                : activeUser.isTeacher() ? PowerSchool.getTeacher(activeUser)
                : activeUser.isStudent() ? PowerSchool.getStudent(activeUser)
                : activeUser.isRoot() ? activeUser : null;

                if (isFirstLogin() && !activeUser.isRoot()) {
                    // first-time users need to change their passwords from the default provided
                } else if (activeUser.isRoot()) {
                	boolean validLogin = true;
                	while (validLogin) {
                        switch (getSelectionRoot()) {
                        case RTCHANGEPWD: System.out.println("\nroot change password\n"); break;
                        case RTRESETDB: /*PowerSchool.reset();*/ System.out.println("\nroot reset database\n"); break;
                        case RTSHUTDOWN: System.out.println("\nroot shut down\n"); break;
                        case RTLOGOUT: validLogin = false; in.nextLine(); break;
                        default: System.out.println("\nInvalid selection.\n"); break;
                        }
                	}
                } else if (activeUser.isAdministrator()) {
                	boolean validLogin = true;
                	while (validLogin) {
                        switch (getSelectionAdministrator()) {
                        case ADBYFAC: System.out.println("\nview by faculty\n"); break;
                        case ADBYDEP: System.out.println("\nview by dept\n"); break;
                        case ADBYENROLL: System.out.println("\nview enrollment\n"); break;
                        case ADBYGRADE: System.out.println("\nview by grade\n"); break;
                        case ADBYCOURSE: System.out.println("\nview by course\n"); break;
                        case ADCHANGEPWD: System.out.println("\nadmin change password\n"); break;
                        case ADLOGOUT: validLogin = false; in.nextLine(); break;
                        default: System.out.println("\nInvalid selection.\n"); break;
                        }
                	}
                } else if (activeUser.isTeacher()) {
                	boolean validLogin = true;
                	while (validLogin) {
                        switch (getSelectionTeacher()) {
                        case TCBYCOURSE: System.out.println("\nview enrollment by course\n"); break;
                        case TCNEWASGN: System.out.println("\nadd assignment\n"); break;
                        case TCDLTASGN: System.out.println("\ndelete assignment\n"); break;
                        case TCNEWGRD: System.out.println("\nenter grade\n"); break;
                        case TCCHANGEPWD: System.out.println("\nteacher change password\n"); break;
                        case TCLOGOUT: validLogin = false; in.nextLine(); break;
                        default: System.out.println("\nInvalid selection.\n"); break;
                        }
                	}
                } else if (activeUser.isStudent()) {
                	boolean validLogin = true;
                	while (validLogin) {
                        switch (getSelectionStudent()) {
                        case STVIEWGRD: System.out.println("\nview course grades\n"); break;
                        case STBYCOURSE: System.out.println("\nview asgn grades by course\n"); break;
                        case STCHANGEPWD: System.out.println("\nstudent change password\n"); break;
                        case STLOGOUT: validLogin = false; in.nextLine(); break;
                        default: System.out.println("\nInvalid selection.\n"); break;
                        }
                	}
                }

                // create and show the user interface
                //
                // remember, the interface will be difference depending on the type
                // of user that is logged in (root, administrator, teacher, student)
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

    public int getSelectionRoot() {
        System.out.println("[1] Reset user password.");
        System.out.println("[2] Factory reset database.");
        System.out.println("[3] Logout.");
        System.out.println("[4] Shutdown.");

        if (in.hasNextInt()) {
        	return in.nextInt();
        } else {
            in.nextLine();
            return 6;
        }
    }

    public int getSelectionAdministrator() {
    	System.out.println("Hello again, [USER FIRST NAME]!\n");
    	System.out.println("[1] View faculty.");
        System.out.println("[2] View faculty by department.");
        System.out.println("[3] View student enrollment.");
        System.out.println("[4] View student enrollment by grade.");
        System.out.println("[5] View student enrollment by course.");
        System.out.println("[6] Change password.");
        System.out.println("[7] Logout.");

        if (in.hasNextInt()) {
        	return in.nextInt();
        } else {
            in.nextLine();
            return 6;
        }
    }

    public int getSelectionTeacher() {
        System.out.println("[1] View enrollment by course.");
        System.out.println("[2] Add assignment.");
        System.out.println("[3] Delete assignment.");
        System.out.println("[4] Enter grade.");
        System.out.println("[5] Change password.");
        System.out.println("[6] Logout.");

        if (in.hasNextInt()) {
        	return in.nextInt();
        } else {
            in.nextLine();
            return 6;
        }
    }

    public int getSelectionStudent() {
        System.out.println("[1] View course grades.");
        System.out.println("[2] View assignment grades by course.");
        System.out.println("[3] Change password.");
        System.out.println("[4] Logout.");

        if (in.hasNextInt()) {
        	return in.nextInt();
        } else {
            in.nextLine();
            return 6;
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

    /////// MAIN METHOD ///////////////////////////////////////////////////////////////////

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
