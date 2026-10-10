package com.TheEmperorPR.LibraryBackend.common.domain.service;

import com.TheEmperorPR.LibraryBackend.common.database.dto.*;

public interface UserService {

    UserProfileResponse registerStaff(StaffProfileRequest request);

    UserProfileResponse registerStudent(StudentProfileRequest request);

    UserProfileResponse getCurrentUserProfile(String username);

    UserProfileResponse updateProfile(String username, UpdateProfileRequest request);

    void changePassword(String username, ChangePasswordRequest request);
}
