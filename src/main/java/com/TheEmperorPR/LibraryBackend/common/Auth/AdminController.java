package com.TheEmperorPR.LibraryBackend.common.Auth;


import com.TheEmperorPR.LibraryBackend.common.database.dto.SignUpRequest;
import com.TheEmperorPR.LibraryBackend.common.database.entity.User;
import com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

   @Autowired private UserRepository userRepository;
   @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/setStaffCred")
    public ResponseEntity<?> setStaffCred(@RequestBody SignUpRequest request){
        if(userRepository.findByUsername(request.getUsername()).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username Already Exists.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        try{
            userRepository.save(user);
        }
        catch (Exception e){
            System.out.println("Cannot Save Staff Credentials.");
            System.out.println(e);
        };

        return ResponseEntity.ok("Staff Credential Saved.");
    }

    public ResponseEntity<?> setStudentCred(@RequestBody SignUpRequest request){
        if(userRepository.findByUsername(request.getUsername()).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username Already Exists.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        try{
            userRepository.save(user);
        }
        catch (Exception e){
            System.out.println("Cannot Save Student Credentials.");
            System.out.println(e);
        };

        return ResponseEntity.ok("Student Credential Saved.");
    }


}
