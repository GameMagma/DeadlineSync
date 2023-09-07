import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Represents a browser window.
 */
public class Window {
    private final WebDriver driver;     // Web driver that interacts with the browser and everything inside it
    private final Wait<WebDriver> wait; // Settings for wait - used to wait until the webpage loads

    public Window() {
        this.driver = new ChromeDriver();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(5)); // Explicit waiting
    }

    /*
    There are two possible methods (that I can think of) to find assignments:
    1. Go to the calendar, go to the agenda, and parse the assignments for each day.
    2. For each class, go into that class's course page. Then, for each assignment, grab the title and the due date.

    Number 1 makes things more difficult to grab (not every assignment has a set unique ID), but doing number 2 would
    force me to go into the first course, grab each assignment, then back out and go to the next class, and so on, which
    would take a long time.

    I'll be doing number 1, but I might do number 2 as well, just for fun.
     */

    /**
     * Open the window, go to canvas, and sign in with the user's credentials
     */
    public void setup(HashMap<String, String> schoolCredentials) {
        driver.get("https://nwmissouri.instructure.com/");

        // Wait for the sign-in page to show up
        wait.until(d -> driver.findElement(By.id("userNameArea")).isDisplayed());

        // Find username and password boxes
        WebElement userNameInput = driver.findElement(By.id("userNameInput"));
        WebElement passwordInput = driver.findElement(By.id("passwordInput"));

        // Type in the username and password then hit enter
        userNameInput.sendKeys(schoolCredentials.get("username"));
        passwordInput.sendKeys(schoolCredentials.get("password"));
        passwordInput.submit();

        // Wait until we get to canvas. The user may have to go through the 2FA process
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(d -> driver.getCurrentUrl().equals("https://nwmissouri.instructure.com/"));
    }

    /**
     * This method of getting assignments uses method 1, which is the more efficient option.
     * This will go to the agenda in the calendar section, go through each day, and put each assignment in
     * a list.
     */
    public List<AgendaDay> getAssignments() {
        // Switch to the calendar
        WebElement calendarButton = driver.findElement(By.id("global_nav_calendar_link"));
        wait.until(d -> calendarButton.isDisplayed());
        calendarButton.click();
        wait.until(d -> driver.getTitle().equals("Calendar"));

        // Select the agenda tab
        WebElement agendaTabButton = driver.findElement(By.id("agenda"));
        wait.until(d -> agendaTabButton.isDisplayed());
        agendaTabButton.click();

        // Wait until the screen actually loads
        wait.until(d -> driver.findElement(By.id("application")).isDisplayed());

        // Get the list of assignments by climbing down through the children of calendar-app
        WebElement calendarApp = driver.findElement(By.id("calendar-app"));

        WebElement agendaWrapper = calendarApp.findElement(By.className("agenda-wrapper"));
        wait.until(d -> agendaWrapper.isDisplayed());

        List<WebElement> elementList_agendaDay = driver.findElements(By.className("agenda-day"));
        List<WebElement> elementList_agendaEvent = driver.findElements(By.className("agenda-event__container"));
        // Check to make sure two lists are equal
        if (elementList_agendaDay.size() != elementList_agendaEvent.size()) {
            System.out.println("TWO LISTS AREN'T EQUAL");
            System.out.println("Day list: " + elementList_agendaDay.size());
            System.out.println("Event list: " + elementList_agendaEvent.size());
        }

        // Get all agenda containers
//        List<WebElement> assignmentLists = agendaWrapper.findElement(By.className("agenda-container"))
//                .findElements(By.tagName("div"));

        /*
        Assignment lists should be a list of elements of agenda dates and content, alternating. Format on the page:
            Due date:
            Assignment
            Assignment
            Assignment

            Due date:
            Assignment

        Format in the code:
            <agenda day>
            <event container>
            <agenda day>
            <event container>

        Due dates with no assignments will not be shown or generated.
         */

        // Get every agendaDay we can and put them in the list
        List<AgendaDay> agenda = new ArrayList<>(); // The entire agenda - each object is a due date with event containers
        // Since everything comes in groups of two, iterate by 2s to skip over the second object each time
        for (int i = 0; (i < elementList_agendaDay.size() && i < elementList_agendaEvent.size()); i++) {
            // The list alternates between day and container, so the current i value will be the day and i+1 is the
            //  container.
            agenda.add(new AgendaDay(driver, elementList_agendaDay.get(i), elementList_agendaEvent.get(i)));
        }

        return agenda;
    }

    public void teardown() {
        if (driver != null) {
            System.out.println("Shutting down browser with ID " + driver.getWindowHandle());
            driver.quit();
        }
    }
}
