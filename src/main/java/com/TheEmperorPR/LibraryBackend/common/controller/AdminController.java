//package com.TheEmperorPR.LibraryBackend.common.controller;
//
//import com.TheEmperorPR.LibraryBackend.common.database.dto.SignUpRequest;
//import com.TheEmperorPR.LibraryBackend.common.database.entity.StaffProfileEntity;
//import com.TheEmperorPR.LibraryBackend.common.database.repository.StaffProfileRepo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/admin")
//public class AdminController {
//
//    @Autowired private StaffCredRepo staffCredRepo;
//    @Autowired private StaffProfileRepo staffProfileRepo;
//    @Autowired private PasswordEncoder passwordEncoder;
//
//    @PostMapping("/registerStaff")
//    public ResponseEntity<?> registerStaff(@RequestBody SignUpRequest request){
//        if(staffCredRepo.findByUsername(request.getUsername()).isPresent()) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body("Staff Already Exists");
//        }
//        StaffCred staffCred = new StaffCred();
//        staffCred.setUsername(request.getUsername());
//        staffCred.setPassword(passwordEncoder.encode(request.getPassword()));
//
//        staffCredRepo.save(staffCred);
//
//        staffCredRepo.findByUsername(request.getUsername());
//        StaffProfileEntity staffProfileEntity = new StaffProfileEntity();
//
//
//        return ResponseEntity.ok("Staff Registered");
//    }
//}
