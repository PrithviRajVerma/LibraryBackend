package com.TheEmperorPR.LibraryBackend.common.Auth;

import com.TheEmperorPR.LibraryBackend.common.database.dto.SignInRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.SignUpRequest;
import com.TheEmperorPR.LibraryBackend.common.database.entity.User;
import com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo.UserRepository;
import jakarta.servlet.ServletOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
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

    @Autowired private UserRepository userRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/admin/register")
    public ResponseEntity<?> registerAdmin(@RequestBody SignUpRequest request){
        if(userRepository.findByUsername(request.getUsername()).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Admin already exists.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body("Admin Registered.");
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody SignInRequest request){
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Admin Not Found"));
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials.");
        }

        String token = jwtUtil.generateToken(
                new CustomUserDetails(
                        user.getId(),
                        user.getPassword(),
                        user.getUsername(),
                        user.getRole()
                )
        );

        return ResponseEntity.ok(Map.of("token",token));
    }



    @PostMapping("/staff/login")
    public ResponseEntity<?> staffLogin(@RequestBody SignInRequest request){
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Staff Not Found"));
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials.");
        }

        String token = jwtUtil.generateToken(
                new CustomUserDetails(
                        user.getId(),
                        user.getPassword(),
                        user.getUsername(),
                        user.getRole()
                )
        );
        return ResponseEntity.ok(Map.of("token",token));
    }

    @PostMapping("/student/login")
    public ResponseEntity<?> studentLogin(@RequestBody SignInRequest request){
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Student Not Found"));
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Credentials.");
        }

        String token = jwtUtil.generateToken(
                new CustomUserDetails(
                        user.getId(),
                        user.getPassword(),
                        user.getUsername(),
                        user.getRole()
                )
        );
        return ResponseEntity.ok(Map.of("token",token));
    }

}
