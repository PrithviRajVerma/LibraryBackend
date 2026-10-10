package com.TheEmperorPR.LibraryBackend.common.domain.service.impl;

import com.TheEmperorPR.LibraryBackend.common.database.dto.*;
import com.TheEmperorPR.LibraryBackend.common.database.entity.StaffProfileEntity;
import com.TheEmperorPR.LibraryBackend.common.database.entity.StudentProfileEntity;
import com.TheEmperorPR.LibraryBackend.common.database.entity.User;
import com.TheEmperorPR.LibraryBackend.common.database.repository.StaffProfileRepo;
import com.TheEmperorPR.LibraryBackend.common.database.repository.StudentProfileRepo;
import com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo.UserRepository;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.Role;
import com.TheEmperorPR.LibraryBackend.common.domain.service.UserService;
import com.TheEmperorPR.LibraryBackend.core.exception.DuplicateResourceException;
import com.TheEmperorPR.LibraryBackend.core.exception.InvalidOperationException;
import com.TheEmperorPR.LibraryBackend.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StaffProfileRepo staffProfileRepo;
    private final StudentProfileRepo studentProfileRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserProfileResponse registerStaff(StaffProfileRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (staffProfileRepo.existsByEmployeeId(request.getEmployeeId())) {
            throw new DuplicateResourceException("StaffProfile", "employeeId", request.getEmployeeId());
        }
        if (staffProfileRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("StaffProfile", "email", request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STAFF)
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);

        StaffProfileEntity staffProfile = StaffProfileEntity.builder()
                .user(savedUser)
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .contactNo(request.getContactNo())
                .department(request.getDepartment())
                .designation(request.getDesignation())
                .build();
        StaffProfileEntity savedProfile = staffProfileRepo.save(staffProfile);

        return UserProfileResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .isActive(savedUser.isActive())
                .profileId(savedProfile.getId())
                .memberId(savedProfile.getEmployeeId())
                .firstName(savedProfile.getFirstName())
                .lastName(savedProfile.getLastName())
                .email(savedProfile.getEmail())
                .contactNo(savedProfile.getContactNo())
                .department(savedProfile.getDepartment().name())
                .designation(savedProfile.getDesignation())
                .build();
    }

    @Override
    @Transactional
    public UserProfileResponse registerStudent(StudentProfileRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }
        if (studentProfileRepo.existsByStudentId(request.getStudentId())) {
            throw new DuplicateResourceException("StudentProfile", "studentId", request.getStudentId());
        }
        if (studentProfileRepo.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("StudentProfile", "email", request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .isActive(true)
                .build();
        User savedUser = userRepository.save(user);

        StudentProfileEntity studentProfile = StudentProfileEntity.builder()
                .user(savedUser)
                .studentId(request.getStudentId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .contactNo(request.getContactNo())
                .department(request.getDepartment())
                .course(request.getCourse())
                .branch(request.getBranch())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .maxBorrowLimit(3)
                .hasActiveHold(false)
                .build();
        StudentProfileEntity savedProfile = studentProfileRepo.save(studentProfile);

        return UserProfileResponse.builder()
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .role(savedUser.getRole())
                .isActive(savedUser.isActive())
                .profileId(savedProfile.getId())
                .memberId(savedProfile.getStudentId())
                .firstName(savedProfile.getFirstName())
                .lastName(savedProfile.getLastName())
                .email(savedProfile.getEmail())
                .contactNo(savedProfile.getContactNo())
                .department(savedProfile.getDepartment().name())
                .course(savedProfile.getCourse().name())
                .branch(savedProfile.getBranch() != null ? savedProfile.getBranch().name() : null)
                .address(savedProfile.getAddress())
                .city(savedProfile.getCity() != null ? savedProfile.getCity().name() : null)
                .state(savedProfile.getState() != null ? savedProfile.getState().name() : null)
                .maxBorrowLimit(savedProfile.getMaxBorrowLimit())
                .hasActiveHold(savedProfile.isHasActiveHold())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        UserProfileResponse.UserProfileResponseBuilder builder = UserProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .isActive(user.isActive());

        if (user.getRole() == Role.STUDENT) {
            studentProfileRepo.findByUser_Username(username).ifPresent(p -> {
                builder.profileId(p.getId())
                        .memberId(p.getStudentId())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .email(p.getEmail())
                        .contactNo(p.getContactNo())
                        .department(p.getDepartment().name())
                        .course(p.getCourse().name())
                        .branch(p.getBranch() != null ? p.getBranch().name() : null)
                        .address(p.getAddress())
                        .city(p.getCity() != null ? p.getCity().name() : null)
                        .state(p.getState() != null ? p.getState().name() : null)
                        .maxBorrowLimit(p.getMaxBorrowLimit())
                        .hasActiveHold(p.isHasActiveHold());
            });
        } else if (user.getRole() == Role.STAFF) {
            staffProfileRepo.findByUser_Username(username).ifPresent(p -> {
                builder.profileId(p.getId())
                        .memberId(p.getEmployeeId())
                        .firstName(p.getFirstName())
                        .lastName(p.getLastName())
                        .email(p.getEmail())
                        .contactNo(p.getContactNo())
                        .department(p.getDepartment().name())
                        .designation(p.getDesignation());
            });
        }

        return builder.build();
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (user.getRole() == Role.STUDENT) {
            StudentProfileEntity profile = studentProfileRepo.findByUser_Username(username)
                    .orElseThrow(() -> new ResourceNotFoundException("StudentProfile for user " + username + " not found"));
            if (request.getContactNo() != null) profile.setContactNo(request.getContactNo());
            if (request.getAddress() != null) profile.setAddress(request.getAddress());
            if (request.getCity() != null) profile.setCity(request.getCity());
            if (request.getState() != null) profile.setState(request.getState());
            studentProfileRepo.save(profile);
        } else if (user.getRole() == Role.STAFF) {
            StaffProfileEntity profile = staffProfileRepo.findByUser_Username(username)
                    .orElseThrow(() -> new ResourceNotFoundException("StaffProfile for user " + username + " not found"));
            if (request.getContactNo() != null) profile.setContactNo(request.getContactNo());
            staffProfileRepo.save(profile);
        }

        return getCurrentUserProfile(username);
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InvalidOperationException("Current password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
