import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Assignment {
    private final String assignmentName;
    private final String className;


    /**
     * Constructs an Assignment object by breaking down eventItem container. Process:
     * Go into the element and get the list of elements, then take the second one (which is the name of the assignment)
     * and make that the assignment name. Take the last element and use regex to get the class name.
     *
     * @param eventItem event-item element. Child of agenda-event__container
     * @throws InvalidCourseNameException Thrown if the matcher cannot find a match in the course name, which usually
     * indicates that the driver somehow got the wrong element
     */
    public Assignment(@NotNull WebElement eventItem) throws InvalidCourseNameException {

        // Assign all the web elements in the event container to a list.
        // Indexes: 0 = " Assignment, due {TIME DUE} ", 1 = " {ASSIGNMENT NAME} ", 2 = " {Completed||Not Completed} "
        // 3 = "Calendar {CLASS NAME} {CLASS ID}"
        List<WebElement> assignmentInfo = eventItem.findElements(By.tagName("span"));

        this.assignmentName = assignmentInfo.get(1).getText().trim();
        System.out.print("Found assignment name: '" + this.assignmentName + "'... ");

        // Regular expression pattern to match the format "Calendar {COURSE NAME} {COURSE ID}"
        Pattern pattern = Pattern.compile("^Calendar (.+?) ([0-9a-fA-F]+)$");
        Matcher matcher = pattern.matcher(assignmentInfo.get(3).getText().trim());

        // If match was correctly found, assign it to the class name. If not, throw an exception.
        if (matcher.find()) {
            this.className = matcher.group(1);  // Returns the course name
            System.out.println(" Found course: " + this.className);
        } else {
            throw new InvalidCourseNameException();
        }
    }

    /**
     * @return The name of this assignment.
     */
    public String getAssignmentName() {
        return assignmentName;
    }

    /**
     * @return The name of the course this assignment is from.
     */
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
