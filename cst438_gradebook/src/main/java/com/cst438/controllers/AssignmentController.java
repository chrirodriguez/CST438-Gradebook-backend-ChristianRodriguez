package com.cst438.controllers;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.Optional;

/* This controller will allow an instructor to add a new assignment
 * for a particular course which will contain a name and a due date.
 * The instructor should also be able to change the name and due date of assignments,
 * as well as delete assignments for any course.*/


@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:3001" })
@RestController
@RequestMapping("/assignments")
public class AssignmentController {

    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    public AssignmentController(AssignmentRepository assignmentRepository, CourseRepository courseRepository) {
        this.assignmentRepository = assignmentRepository;
        this.courseRepository = courseRepository;
    }

    // Endpoint to get an assignment by ID
    @GetMapping("/assignments/{id}")
    public ResponseEntity<Assignment> getAssignmentById(@PathVariable("id") int assignmentId) {
        Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);
        if (assignmentOptional.isPresent()) {
            Assignment assignment = assignmentOptional.get();
            return new ResponseEntity<>(assignment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /*// Endpoint to create a new assignment
    @PostMapping
    public ResponseEntity<Assignment> createAssignment(@RequestBody Assignment assignment) {
        Optional<Course> courseOptional = courseRepository.findById(assignment.getCourse().getCourse_id());
        if (courseOptional.isPresent()) {
            assignment.setCourse(courseOptional.get());
            Assignment createdAssignment = assignmentRepository.save(assignment);
            return new ResponseEntity<>(createdAssignment, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }*/
    
    //testing
    @PostMapping("/assignments")
    @Transactional
    public void addAssignment(@PathVariable int course_id, @RequestParam String assignment_name,
          @RequestParam String due_date) {
       // check that this request is from the course instructor and for a valid course
       String email = "dwisneski@csumb.edu"; // user name (should be instructor's email)
       Course c = courseRepository.findById(course_id).orElse(null);
       if (c == null) {
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Course does not exist. ");
       }
       if (!c.getInstructor().equals(email)) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not Authorized. ");
       }

       Assignment assignment = new Assignment();
       assignment.setCourse(c);
       assignment.setName(assignment_name);
       assignment.setNeedsGrading(1);
       assignment.setDueDate(Date.valueOf(due_date));

       assignmentRepository.save(assignment);
    }

    // Endpoint to update the name and due date of an assignment
    @PutMapping("/{id}")
    public ResponseEntity<Assignment> updateAssignment(@PathVariable("id") int assignmentId,
                                                       @RequestBody Assignment updatedAssignment) {
        Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);
        if (assignmentOptional.isPresent()) {
            Assignment assignment = assignmentOptional.get();
            assignment.setName(updatedAssignment.getName());
            assignment.setDueDate(updatedAssignment.getDueDate());
            Assignment savedAssignment = assignmentRepository.save(assignment);
            return new ResponseEntity<>(savedAssignment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Endpoint to delete an assignment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable("id") int assignmentId) {
        Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);
        if (assignmentOptional.isPresent()) {
            Assignment assignment = assignmentOptional.get();
            if (assignment.getNeedsGrading() == 0) {
                assignmentRepository.delete(assignment);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


}
