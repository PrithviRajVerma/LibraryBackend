package com.TheEmperorPR.LibraryBackend.common.Auth;

import com.TheEmperorPR.LibraryBackend.common.database.dto.*;
import com.TheEmperorPR.LibraryBackend.common.database.entity.User;
import com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo.UserRepository;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.Role;
import com.TheEmperorPR.LibraryBackend.common.domain.service.UserService;
import com.TheEmperorPR.LibraryBackend.core.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Endpoints for administrator provisioning and configuration")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final com.TheEmperorPR.LibraryBackend.common.domain.service.AnalyticsService analyticsService;

    @GetMapping("/analytics/summary")
    @Operation(summary = "Get system-wide metrics and circulation financial analytics")
    public ResponseEntity<ApiResponse<AnalyticsResponse>> getAnalytics() {
        AnalyticsResponse summary = analyticsService.getSystemAnalytics();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @PostMapping("/users/staff")
    @Operation(summary = "Provision a staff member with user account and profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> provisionStaff(@Valid @RequestBody StaffProfileRequest request) {
        UserProfileResponse profile = userService.registerStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff profile created successfully", profile));
    }

    @PostMapping("/users/student")
    @Operation(summary = "Provision a student member with user account and profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> provisionStudent(@Valid @RequestBody StudentProfileRequest request) {
        UserProfileResponse profile = userService.registerStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Student profile created successfully", profile));
    }

    @PostMapping("/setStaffCred")
    @Operation(summary = "Legacy endpoint: Provision staff credential only")
    public ResponseEntity<ApiResponse<String>> setStaffCred(@Valid @RequestBody SignUpRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Username Already Exists."));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.STAFF);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Staff Credential Saved.", null));
    }

    @PostMapping("/setStudentCred")
    @Operation(summary = "Legacy endpoint: Provision student credential only")
    public ResponseEntity<ApiResponse<String>> setStudentCred(@Valid @RequestBody SignUpRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Username Already Exists."));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.STUDENT);
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Student Credential Saved.", null));
    }
}
