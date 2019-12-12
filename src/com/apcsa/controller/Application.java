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
                activeUser = activeUser.isAdministrator()
                    ? PowerSchool.getAdministrator(activeUser) : activeUser.isTeacher()
                    ? PowerSchool.getTeacher(activeUser) : activeUser.isStudent()
                    ? PowerSchool.getStudent(activeUser) : activeUser.isRoot()
                    ? activeUser : null;

                if (isFirstLogin() && !activeUser.isRoot()) {
                    // first-time users need to change their passwords from the default provided
                } else if (activeUser.isRoot()) {
                	boolean validLogin = true;
                	while (validLogin) {
                        switch (getSelectionRoot()) {
                            case RTCHANGEPWD: System.out.print("\nroot change password\n"); break;
                            case RTRESETDB: System.out.print("\nroot reset database\n"); /*PowerSchool.reset();*/ break;
                            case RTSHUTDOWN: rootShutdown(); break;
                            case RTLOGOUT: validLogin = logoutConfirm(); in.nextLine(); break;
                            default: System.out.print("\nInvalid selection.\n"); break;
                        }
                	}
                } else if (activeUser.isAdministrator()) {
                	boolean validLogin = true;
                	while (validLogin) {
                        switch (getSelectionAdministrator()) {
                            case ADBYFAC: System.out.print("\nview by faculty\n"); break;
                            case ADBYDEP: System.out.print("\nview by dept\n"); break;
                            case ADBYENROLL: System.out.print("\nview enrollment\n"); break;
                            case ADBYGRADE: System.out.print("\nview by grade\n"); break;
                            case ADBYCOURSE: System.out.print("\nview by course\n"); break;
                            case ADCHANGEPWD: System.out.print("\nadmin change password\n"); break;
                            case ADLOGOUT: validLogin = logoutConfirm(); in.nextLine(); break;
                            default: System.out.print("\nInvalid selection.\n"); break;
                        }
                	}
                } else if (activeUser.isTeacher()) {
                	boolean validLogin = true;
                	while (validLogin) {
                        switch (getSelectionTeacher()) {
                            case TCBYCOURSE: System.out.print("\nview enrollment by course\n"); break;
                            case TCNEWASGN: System.out.print("\nadd assignment\n"); break;
                            case TCDLTASGN: System.out.print("\ndelete assignment\n"); break;
                            case TCNEWGRD: System.out.print("\nenter grade\n"); break;
                            case TCCHANGEPWD: System.out.print("\nteacher change password\n"); break;
                            case TCLOGOUT: validLogin = logoutConfirm(); in.nextLine(); break;
                            default: System.out.print("\nInvalid selection.\n"); break;
                        }
                	}
                } else if (activeUser.isStudent()) {
                	boolean validLogin = true;
                	while (validLogin) {
                        switch (getSelectionStudent()) {
                            case STVIEWGRD: System.out.print("\nview course grades\n"); break;
                            case STBYCOURSE: System.out.print("\nview asgn grades by course\n"); break;
                            case STCHANGEPWD: System.out.print("\nstudent change password\n"); break;
                            case STLOGOUT: validLogin = logoutConfirm(); in.nextLine(); break;
                            default: System.out.print("\nInvalid selection.\n"); break;
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

    public boolean logoutConfirm() {
        System.out.print("\nAre you sure you want to logout? (y/n) ");
    	String logoutDecision = in.next();
    	if (logoutDecision.equals("y")) {
    		return false;
    	} else {
    		return true;
    	}
    }

    public int getSelectionRoot() {
        int rootDecision;
    	System.out.println("\nHello again, ROOT!\n");
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
        String firstName = activeUser.getFirstName();
    	System.out.printf("\nHello again, %s!\n", firstName);
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

    public int getSelectionTeacher() {
    	int teacherDecision;
        String firstName = activeUser.getFirstName();
    	System.out.printf("\nHello again, %s!\n", firstName);
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
        String firstName = activeUser.getFirstName();
    	System.out.printf("\nHello again, %s!\n", firstName);
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
