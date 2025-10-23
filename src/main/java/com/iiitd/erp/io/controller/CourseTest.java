package com.iiitd.erp.io.controller;

import com.iiitd.erp.persistent.persistence.repository.CourseRepository;
import org.springframework.web.bind.annotation.*;

@RestController  //Marks
@RequestMapping("/api/courses")
@CrossOrigin
public class CourseTest {
    private final CourseRepository repo;

    public CourseTest(CourseRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public Object all() { return repo.findAll(); }  //fetching all the courses from the database and return them as HTTP GET request

//    @PostMapping public Object add(@RequestBody CourseCreationRequest courseCreationRequest) { return repo.save((Map) body); }
}


