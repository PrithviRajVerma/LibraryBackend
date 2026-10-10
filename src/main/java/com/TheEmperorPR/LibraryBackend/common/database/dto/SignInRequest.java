package com.TheEmperorPR.LibraryBackend.common.database.dto;

import com.TheEmperorPR.LibraryBackend.common.domain.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignInRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotNull
    private Role role;
}
