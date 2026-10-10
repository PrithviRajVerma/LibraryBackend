package com.TheEmperorPR.LibraryBackend.common.Auth;

import com.TheEmperorPR.LibraryBackend.common.database.dto.SignInRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.SignUpRequest;
import com.TheEmperorPR.LibraryBackend.common.database.entity.User;
import com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo.UserRepository;
import com.TheEmperorPR.LibraryBackend.core.exception.DuplicateResourceException;
import com.TheEmperorPR.LibraryBackend.core.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user login and admin registration")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/admin/register")
    @Operation(summary = "Register the initial administrator account")
    public ResponseEntity<ApiResponse<String>> registerAdmin(@Valid @RequestBody SignUpRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Admin already exists with username: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin Registered.", user.getUsername()));
    }

    @PostMapping("/login")
    @Operation(summary = "Unified login for Admin, Staff, and Students")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody SignInRequest request) {
        return authenticateAndGenerateToken(request);
    }

    @PostMapping("/admin/login")
    @Operation(summary = "Admin specific login endpoint")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminLogin(@Valid @RequestBody SignInRequest request) {
        return authenticateAndGenerateToken(request);
    }

    @PostMapping("/staff/login")
    @Operation(summary = "Staff specific login endpoint")
    public ResponseEntity<ApiResponse<Map<String, Object>>> staffLogin(@Valid @RequestBody SignInRequest request) {
        return authenticateAndGenerateToken(request);
    }

    @PostMapping("/student/login")
    @Operation(summary = "Student specific login endpoint")
    public ResponseEntity<ApiResponse<Map<String, Object>>> studentLogin(@Valid @RequestBody SignInRequest request) {
        return authenticateAndGenerateToken(request);
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> authenticateAndGenerateToken(SignInRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid Credentials.");
        }

        String token = jwtUtil.generateToken(
                new CustomUserDetails(
                        user.getId(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getRole()
                )
        );

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("userId", user.getId());
        responseData.put("username", user.getUsername());
        responseData.put("role", user.getRole().name());

        return ResponseEntity.ok(ApiResponse.success("Authentication successful", responseData));
    }
}
