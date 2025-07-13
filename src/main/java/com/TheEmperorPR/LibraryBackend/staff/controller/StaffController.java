package com.TheEmperorPR.LibraryBackend.staff.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/staff")
public class StaffController {

    @PostMapping("/addBook")
    public ResponseEntity<?> addBook(){
        return ResponseEntity.ok("Book Added.");
    }


}
