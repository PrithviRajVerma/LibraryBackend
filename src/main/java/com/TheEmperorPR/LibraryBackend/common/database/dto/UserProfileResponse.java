package com.TheEmperorPR.LibraryBackend.common.database.dto;

import com.TheEmperorPR.LibraryBackend.common.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileResponse {

    private Long userId;
    private String username;
    private Role role;
    private boolean isActive;

    // Profile Details
    private Long profileId;
    private String memberId; // studentId or employeeId
    private String firstName;
    private String lastName;
    private String email;
    private String contactNo;
    private String department;
    private String course;
    private String branch;
    private String designation;
    private String address;
    private String city;
    private String state;

    // Student specific stats
    private Integer maxBorrowLimit;
    private Boolean hasActiveHold;
}
