import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import com.google.api.services.tasks.model.TaskLists;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// TODO: I'm building a new authorized client service every time I call a function. I should find a way to have only one
public class TaskListManager {
    private static final String APPLICATION_NAME = "Google Tasks API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(TasksScopes.TASKS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /** The user's list of tasks */
    private static List<Task> tasks;

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = TaskListManager.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

//    public static void main(String... args) throws IOException, GeneralSecurityException {
//        // Build a new authorized API client service.
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//                .setApplicationName(APPLICATION_NAME)
//                .build();
//
//        // Print the first 10 task lists.
//        TaskLists result = service.tasklists().list()
//                .setMaxResults(10)
//                .execute();
//        List<TaskList> taskLists = result.getItems();
//        if (taskLists == null || taskLists.isEmpty()) {
//            System.out.println("No task lists found.");
//        } else {
//            System.out.println("Task lists:");
//            for (TaskList tasklist : taskLists) {
//                System.out.printf("%s (%s)\n", tasklist.getTitle(), tasklist.getId());
//            }
//        }
//    }

    public static void createTask() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Create a task using the API
    }

    /**
     * Gets the list of tasks from the user and set it to the private {@code tasks} variable.
     * YOU HAVE TO CALL THIS BEFORE DOING ANYTHING WITH TASKS.
     *
     * @param fullList Whether to get the full list of tasks the user has ever had, including the tasks before
     *                 the current date. Note that if set to true, the list could potentially be years worth of tasks,
     *                 making it very time-consuming to search or iterate through.
     *
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static void initializeTaskList(boolean fullList) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Tasks service = new Tasks.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        TaskLists result = service.tasklists().list().execute(); // Get all the user's task lists
        TaskList taskLists = result.getItems().get(0); // Get the first task list we find. Should be primary list

        if (fullList) {
            // Get the list of tasks, including completed/hidden tasks (completed tasks are hidden) BEFORE today
            tasks = service.tasks().list(taskLists.getId())
                    .setShowHidden(true)
                    .execute().getItems();
        } else {
            // Get the list of tasks, including completed/hidden tasks (completed tasks are hidden) NOT BEFORE today
            tasks = service.tasks().list(taskLists.getId())
                    .setShowHidden(true)
                    .setDueMin(TaskListManager.convertToRFC3339(new Date()).toString())
                    .execute().getItems();
        }

        // Debugging
        System.out.println("\nGot the list of tasks: ");
        for (Task task :
                tasks) {
            System.out.printf("Task name: %s\n", task.getTitle());
            System.out.printf("Task date: %s\n\n", task.getDue());
        }
    }

    /**
     * Convert an object of Java's {@link Date} to an object of Google's {@link DateTime}.
     * @param date The date you want to convert
     * @return The reformatted object.
     */
    public static DateTime convertDate(Date date) {
        // Convert date to the specific RFC3339 format that the Google API uses, then parse the result to convert
        return DateTime.parseRfc3339(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(date));
    }

    /**
     * Converts {@code Date} object to a string in the Rfc3339 format. This is consistent with Google's {@link DateTime}
     * format and can be parsed using {@code DateTime.parseRfc3339()} if you want to convert it to a
     * {@linkplain DateTime} object. Note that this also sets the timezone offset to 0, since Google's Tasks API cannot
     * grab the time and will always return with an offset of 0.
     *
     * @param date Date object you wish to convert
     * @return Formatted date.
     */
    public static String convertToRFC3339(Date date) {
        // Convert date to LocalDateTime, keeping the exact same date and time components
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        // Format LocalDateTime as a string with the 'Z' suffix to represent UTC
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
    }

    /**
     * Gets a task from Tasks that has the given dueDate and taskName. {@code Tasks} must be initialized before calling
     * this. Use {@code initializeTaskList()} to do so.
     *
     * @param dueDate The due date of the task to search for.
     * @param taskName The name of the task to search for.
     * @return The task if it can be found, {@code null} if it cannot be found.
     */
    public static Task getTask(Date dueDate, String taskName) {
        String formattedDueDate = convertToRFC3339(dueDate);

        System.out.printf("Initialized task list (in get task): %s\n", tasks);


        for (Task task :
                tasks) {
            if (task != null) {
//                System.out.printf("Assignment name: %s\n" +
//                        "Assignment due date: %s\n" +
//                        "Task Name: %s\n" +
//                        "Task Date: %s\n",
//                        taskName, formattedDueDate, task.getTitle(), task.getDue());
                // If the due date and the name of the task is equal to what we've been given, return that task
                if ((task.getDue().equalsIgnoreCase(formattedDueDate))
                        && task.getTitle().equalsIgnoreCase(taskName)) {
                    System.out.printf("Found matching task and assignment: Task %s and Assignment %s",
                            task.getTitle(), taskName);
                    return task;
                }
            }
        }
        return null; // If it's reached this point, nothing was found, so just return null
    }

    /**
     * Checks if the service can find the task with the given due date and taskName
     * @param dueDate Minimum completion date of the task.
     * @param taskName Name of the task (not task ID)
     * @return true if, and only if, {@code getTask()} does not return {@code null}.
     */
    public static boolean doesTaskExist(Date dueDate, String taskName) {
        return TaskListManager.getTask(dueDate, taskName) != null;
    }
}