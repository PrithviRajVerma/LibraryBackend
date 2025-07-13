package com.TheEmperorPR.LibraryBackend.student.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
public class StudentController {

    @GetMapping("/greeting")
    public String greetStudent(){
        return "Welcome Student";
    }

}
