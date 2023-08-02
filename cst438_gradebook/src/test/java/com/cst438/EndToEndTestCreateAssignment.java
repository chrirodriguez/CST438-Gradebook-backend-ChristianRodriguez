package com.cst438;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;

/*
 * Tests the following user story:
 *  as an Instructor I can create a
 *  new assignment for a course that I teach
 */

@SpringBootTest
public class EndToEndTestCreateAssignment {

    public static final String GECKO_DRIVER_FILE_LOCATION = "C:/Program Files/chromedriver-win64/chromedriver.exe";
    public static final String URL = "http://localhost:3000";
    public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
    public static final String TEST_COURSE_TITLE = "Test Course";
    public static final String TEST_ASSIGNMENT_NAME = "Test Assignment";
    public static final String TEST_ASSIGNMENT_DUE_DATE = "2023-07-24";
    public static final int TEST_COURSE_ID = 99999;
    public static final int SLEEP_DURATION = 1000; // 1 second.

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Test
    public void testAddAssignment() throws Exception {
       // Database setup: create courses
       Course course = new Course();
       course.setCourse_id(TEST_COURSE_ID);
       course.setInstructor(TEST_INSTRUCTOR_EMAIL);
       course.setSemester("Fall");
       course.setYear(2021);
       course.setTitle(TEST_COURSE_TITLE);
       courseRepository.save(course);

       // Used to delete the test assignment during cleanup
       Assignment assignmentToDelete = null;

       // Initialize the WebDriver
       System.setProperty("webdriver.chrome.driver", GECKO_DRIVER_FILE_LOCATION);
       WebDriver driver = new ChromeDriver();
       driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

       try {
           // Go to the page URL
           driver.get(URL);
           Thread.sleep(SLEEP_DURATION);

           // Click "add assignment" button
           driver.findElement(By.xpath("//button[@id='AddAssig']")).click();
           Thread.sleep(SLEEP_DURATION);

           // Clear the name field
           WebElement nameField = driver.findElement(By.id("name"));
           nameField.clear();

           // Add the Assignment name
           nameField.sendKeys(TEST_ASSIGNMENT_NAME);

           // Add the course ID
           WebElement courseIdField = driver.findElement(By.id("course-id"));
           courseIdField.sendKeys(Integer.toString(TEST_COURSE_ID));

           // Add the due date
           WebElement dueDateField = driver.findElement(By.id("date"));
           dueDateField.sendKeys(TEST_ASSIGNMENT_DUE_DATE);

           // Click the "submit" button
           driver.findElement(By.xpath("//button[@id='SubmitAssigment']")).click();
           Thread.sleep(SLEEP_DURATION);

           // Click the "back" button
           driver.findElement(By.xpath("//button[@id='Back']")).click();
           Thread.sleep(SLEEP_DURATION);

           // Verify that the assignment has been added to the database
           List<Assignment> assignmentList = assignmentRepository.findNeedGradingByEmail(TEST_INSTRUCTOR_EMAIL);
           boolean found = false;
           for (Assignment assignment : assignmentList) {
               if (assignment.getName().equals(TEST_ASSIGNMENT_NAME)) {
                   found = true;
                   assignmentToDelete = assignment;
                   break;
               }
           }
           // Verify that the new assignment is in the assignment list
           assertTrue(found, "Unable to locate TEST ASSIGNMENT in the list of assignments from the database.");

       } catch (Exception ex) {
           throw ex;
       } finally {
           // Clean up the database so the test is repeatable
           if (assignmentToDelete != null)
               assignmentRepository.delete(assignmentToDelete);
           courseRepository.delete(course);

           driver.quit();
       }
   }

}
