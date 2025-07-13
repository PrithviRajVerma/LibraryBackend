package com.TheEmperorPR.LibraryBackend.common.database.mapper;

import com.TheEmperorPR.LibraryBackend.common.database.dto.LoginRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.RegisterRequest;

public class AdminCredMapper {

    public LoginRequest loginRequest(RegisterRequest registerRequest){
        LoginRequest loginRequest = new LoginRequest();

        loginRequest.setUsername(registerRequest.getUsername());
        loginRequest.setPassword(registerRequest.getPassword());

        return loginRequest;
    }

}
