import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assignment {
    private final WebDriver driver;
    private final String assignmentName;
    private String className;


    public Assignment(WebDriver driver, WebElement eventItem) throws InvalidCourseNameException {
        this.driver = driver;

        // Assign all the web elements in the event container to a list.
        // Indexes: 0 = " Assignment, due {TIME DUE} ", 1 = " {ASSIGNMENT NAME} ", 2 = " {Completed||Not Completed} "
        // 3 = "Calendar {CLASS NAME} {CLASS ID}"
        List<WebElement> assignmentInfo = eventItem.findElements(By.tagName("span"));

        this.assignmentName = assignmentInfo.get(1).getText().trim();
        System.out.print("Found assignment name: '" + this.assignmentName + "'... ");

        // Regular expression pattern to match the format "Calendar {COURSE NAME} {COURSE ID}"
        Pattern pattern = Pattern.compile("^Calendar (.+?) ([0-9a-fA-F]+)$");
//        System.out.println("Course name string: " + assignmentInfo.get(3).getText().trim()); // DEBUGGING
        Matcher matcher = pattern.matcher(assignmentInfo.get(3).getText().trim());

        // If match was correctly found, assign it to the class name. If not, throw an exception.
        if (matcher.find()) {
            this.className = matcher.group(1);  // Returns the course name
            System.out.println(" Found course: " + this.className);
        } else {
            System.out.println("Throwing exception"); // Print new line to make things neater after printing the assignment name
            throw new InvalidCourseNameException();
        }
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "assignmentName='" + assignmentName + '\'' +
                ", className='" + className + '\'' +
                '}';
    }
}
