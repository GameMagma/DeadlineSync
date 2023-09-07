import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String... args) {
        Scanner scanner = new Scanner(System.in);
        HashMap<String, String> schoolCredentials = new HashMap<>();
        List<AgendaDay> agenda; // The list of assignments on Canvas

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

        Window browser = new Window();

        try {
            browser.setup(schoolCredentials);
            System.out.println("Browser successfully set up.");
            agenda = browser.getAssignments();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            browser.teardown();
        }
    }
}
