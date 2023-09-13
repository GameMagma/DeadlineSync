import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String... args) {
        Scanner scanner = new Scanner(System.in); // Input scanner
        HashMap<String, String> schoolCredentials = new HashMap<>(); // The user's login credentials for Canvas
        List<AgendaDay> agenda = new ArrayList<>(); // Empty initialization

        // Get user's credentials
        System.out.print("Enter the username for your canvas account: ");
        String username = scanner.next();

        boolean flag = true;
        while (flag) {
            if (username != null) {
                flag = false;
            } else {
                System.out.print("\nUsername is null, please enter it again: ");
                username = scanner.next();
            }
        }

        System.out.print("Enter the password for your canvas account: ");
        String password = scanner.next();

        flag = true;
        while (flag) {
            if (password != null) {
                flag = false;
            } else {
                System.out.print("\nPassword is null, please enter it again.");
                password = scanner.next();
            }
        }


        scanner.reset(); // Flushes the input, just in case. \n like to stick around sometimes, so this gets rid of it.

        // Put school credentials into a hashmap so the driver can log in
        schoolCredentials.put("username", username);
        schoolCredentials.put("password", password);

        Window browser = new Window(); // The browser window being used to get the assignments and due dates

        try {
            browser.setup(schoolCredentials);
            System.out.println("Browser successfully set up.");
            agenda = browser.getAssignments(); // List of Assignments on Canvas
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            browser.teardown();
        }

        try {
            System.out.println("\n All assignments have been retrieved. Will now start matching to Task List.");

            TaskListManager.initializeTaskList(false);

            // Get all tasks that are on the task list. If it exists, remove it from the Assignment list.
            for (AgendaDay dueDate : agenda) { // For each due date
                for (int t = 0; t < dueDate.getAssignmentList().size(); t++) { // For each Assignment due on that day
                    TaskListManager.convertToRFC3339(dueDate.getDate());

                    // If the task could not be found, it doesn't exist, so it needs to be on the list.
                    // If it was found, we don't need to worry about it, so remove it from the assignment list.
                    if (TaskListManager.doesTaskExist(
                            dueDate.getDate(), dueDate.getAssignmentList().get(t).getAssignmentName()))
                    {
                        System.out.printf("Assignment %s (Class %s) exists. Removing it from the list.",
                                dueDate.getAssignmentList().get(t).getAssignmentName(),
                                dueDate.getAssignmentList().get(t).getClassName()); // Debugging
                        dueDate.removeAssignment(t);
                    }
                }
            }

            System.out.printf("New assignment list: %s\n", agenda);


            // Add tasks to task list
        } catch (Exception e) {
            System.out.println("EXCEPTION HAS BEEN THROWN:");
            e.printStackTrace();
        }
    }
}
