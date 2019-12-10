package com.apcsa.controller;

import java.util.Scanner;
import com.apcsa.data.PowerSchool;
import com.apcsa.model.User;

public class Application {

    private Scanner in;
    private User activeUser;

    public static final int rtChangePwd= 1;     // ROOT - reset user password
    public static final int rtResetDB = 2;      // ROOT - factory reset database
    public static final int rtshutDown = 3;     // ROOT - shut down
    public static final int adByFac = 4;        // ADMIN - view faculty
    public static final int adByDep = 5;        // ADMIN - view by department (#22)
    public static final int adByEnroll = 6;     // ADMIN - view student enrollment
    public static final int adByGrade = 7;      // ADMIN - view by grade
    public static final int adByCourse = 8;     // ADMIN - view by course
    public static final int adChangePwd = 9;    // ADMIN - change password
    public static final int tcbyCourse = 10;    // TEACHER - view enrollment by course
    public static final int tcNewAsgn = 11;     // TEACHER - add assignment
    public static final int tcDltAsgn = 12;	    // TEACHER - delete assignment
    public static final int tcNewGrd = 13;      // TEACHER - enter grade
    public static final int tcChangePwd = 14;	// TEACHER - change password
    public static final int stViewGrd = 15;     // STUDENT - view course grades
    public static final int stByCourse = 16;	// STUDENT - view assignment grades by course
    public static final int stChangePwd = 17;	// STUDENT - change password
    public static final int logout = 18;        // logout

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
                	// while (validLogin) {
                    switch (getSelection()) {
                    case VIEW: showBalance(); break;
                    case DEPOSIT: deposit(); break;
                    case WITHDRAW: withdraw(); break;
                    case TRANSFER: transfer(); break;
                    case LOGOUT: bank.update(activeAccount); bank.save(); validLogin = false; in.nextLine(); break;
                    default: System.out.println("\nInvalid selection.\n"); break;
                }
            }
                } else if (activeUser.isAdministrator()) {
                	// show teacher options
                } else if (activeUser.isTeacher()) {
                	// show teacher options
                } else if (activeUser.isStudent()) {
                	// show student options
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
