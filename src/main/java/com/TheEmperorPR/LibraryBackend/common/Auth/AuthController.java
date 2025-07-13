package com.TheEmperorPR.LibraryBackend.common.Auth;

import com.TheEmperorPR.LibraryBackend.common.database.dto.LoginRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.RegisterRequest;
import com.TheEmperorPR.LibraryBackend.common.database.entity.AdminCred;
import com.TheEmperorPR.LibraryBackend.common.database.entity.StaffCred;
import com.TheEmperorPR.LibraryBackend.common.database.entity.StudentCred;
import com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo.AdminCredRepo;
import com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo.StaffCredRepo;
import com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo.StudentCredRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private AdminCredRepo adminCredRepo;
    @Autowired private StaffCredRepo staffCredRepo;
    @Autowired private StudentCredRepo studentCredRepo;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/admin/register")
    public ResponseEntity<?> registerAdmin(@RequestBody RegisterRequest request){
        if(adminCredRepo.findByUsername(request.getUsername()).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Admin already exists.");
        }

        AdminCred adminCred = new AdminCred();
        adminCred.setUsername(request.getUsername());
        adminCred.setPassword(passwordEncoder.encode(request.getPassword()));
        adminCredRepo.save(adminCred);

        return ResponseEntity.status(HttpStatus.OK).body("Admin Registered.");
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest request){
        AdminCred adminCred = adminCredRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Admin Not Found"));
        if(!passwordEncoder.matches(request.getPassword(),adminCred.getPassword())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials.");
        }

        String token = jwtUtil.generateToken(request.getUsername(),"ADMIN");

        return ResponseEntity.ok(Map.of("token",token));
    }

    @PostMapping("/staff/login")
    public ResponseEntity<?> staffLogin(@RequestBody LoginRequest request){
        StaffCred staffCred = staffCredRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Staff Not Found"));
        if(!passwordEncoder.matches(request.getPassword(),staffCred.getPassword())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials.");
        }

        String token = jwtUtil.generateToken(request.getUsername(),"STAFF");
        return ResponseEntity.ok(Map.of("token",token));
    }

    @PostMapping("/student/login")
    public ResponseEntity<?> studentLogin(@RequestBody LoginRequest request){
        StudentCred studentCred = studentCredRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Student Not Found"));
        if(!passwordEncoder.matches(request.getPassword(),studentCred.getPassword())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials.");
        }

        String token = jwtUtil.generateToken(request.getUsername(),"STUDENT");
        return ResponseEntity.ok(Map.of("token",token));
    }

}
