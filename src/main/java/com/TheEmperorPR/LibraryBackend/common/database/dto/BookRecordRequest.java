package com.TheEmperorPR.LibraryBackend.common.database.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookRecordRequest {

    @NotNull
    @NotBlank
    private String bookName;

    @NotBlank
    @NotNull
    private String authorName;

    @NotBlank
    private String edition;

    @NotBlank
    @NotNull
    private String Domain;

    @NotBlank
    @NotNull
    private int numberOfBook;
}
