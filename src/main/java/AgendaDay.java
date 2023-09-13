import org.jetbrains.annotations.NotNull;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AgendaDay {
    private Date date;                       // Due date
    private List<Assignment> assignmentList; // List of assignments

    /**
     * Constructs the object and breaks down the agenda-day and agenda-event__container elements. Process:
     * Go into the day element and parse for the date. Then, translate it to the current date using the parser from
     * {@link SimpleDateFormat} and adding on the timezone and year using {@link Calendar} and {@link TimeZone}. Then
     * go into the element_EventContainer element and get the list of event items. Create an {@link Assignment}
     * object with each event item.
     *
     * @param driver The web driver.
     * @param element_agendaDay The web element holding the due date of the given assignments
     * @param element_EventContainer The event container web element holding the list of assignments
     * @see Assignment
     */
    public AgendaDay(@NotNull WebDriver driver,
                     @NotNull WebElement element_agendaDay, @NotNull WebElement element_EventContainer) {
        this.assignmentList = new ArrayList<>(); // Initializes it so we can add assignments to it later

        /*
        Format for agenda day:
            <agenda day>
                <agenda date>
                    <span>Mon, Sep 11</span>

        Format for agenda-event__container:
            <agenda-event__container>
                <ul agenda-event__list>
                    <li agenda-event__item>
                        <span class="screenreader-only">
                            " Assignment, due {TIME DUE}, "
                        <span class="screenreader-only">
                            " {ASSIGNMENT NAME} "
                        <span class="screenreader-only">
                            " {Completed||Not Completed} "
                        <span class="screenreader-only">Calendar {CLASS NAME} {CLASS ID}</span>
                    <li agenda-event__item>
                        {SAME STRUCTURE AS ABOVE}
                    <li agenda-event__item>
                        {SAME STRUCTURE AS ABOVE}
                </ul>
         */

        /* == Get the date from the element == */
        // Get the text of the date from the element
        String dateInElement = element_agendaDay.findElement(By.className("agenda-date"))
                .findElement(By.tagName("span")).getText();

        // Parse the string into a usable date format
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d");
        try {
            Date datePlain = dateFormat.parse(dateInElement); // The parsed date without the year or timezone
            // Get the current year and timezone.
            Calendar currentCalendar = Calendar.getInstance();
            int currentYear = currentCalendar.get(Calendar.YEAR);
            TimeZone currentTimeZone = currentCalendar.getTimeZone();

            // Adjust the parsed date object with the extracted year.
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(datePlain);
            dateCalendar.set(Calendar.YEAR, currentYear);
            dateCalendar.setTimeZone(currentTimeZone);

            Date adjustedDate = dateCalendar.getTime();

            // Format the adjusted date to show year and timezone explicitly.
            SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, MMM d, yyyy Z");
            outputFormat.setTimeZone(currentTimeZone); // Set the timezone to the formatter.
            this.date = outputFormat.parse(outputFormat.format(adjustedDate));

            System.out.println("Found date: " + this.date);

            /* Find the assignment name */
            List<WebElement> agendaEventItems = element_EventContainer.findElement(By.tagName("ul"))
                    .findElements(By.tagName("li")); // Assign all the assignments from that date

            // Take each event item, convert them into a usable assignment object, and add it to the assignment list
            for (WebElement eventItem :
                    agendaEventItems) {
                assignmentList.add(new Assignment(eventItem));
            }
        } catch (ParseException e) {
            System.out.println("Error parsing the given date string. Stack Trace:");
            e.printStackTrace();
            this.date = null;
            this.assignmentList = null;
        } catch (InvalidCourseNameException e) {
            System.out.println("Error parsing for course name. Stack Trace:");
            e.printStackTrace();
            this.date = null;
            this.assignmentList = null;
        }
    }

    /**
     * @return The date of this Agenda Day. All assignments in the Assignment list will be due on this date.
     * @see Assignment
     */
    public Date getDate() {
        return date;
    }

    /**
     * @return The list of every {@link Assignment} due on this day.
     */
    public List<Assignment> getAssignmentList() {
        return assignmentList;
    }

    /**
     * Removes the assignment object at the given index.
     *
     * @param index Index of assignment to remove
     */
    public void removeAssignment(int index) {
        assignmentList.remove(index);
    }

    @Override
    public String toString() {
        return "AgendaDay{" +
                "date=" + date +
                ", assignmentList=" + assignmentList +
                '}';
    }
}
