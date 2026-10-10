package com.TheEmperorPR.LibraryBackend.common.database.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRecordRequest {

    @NotBlank
    private String bookName;

    @NotBlank
    private String authorName;

    private String edition;

    @NotBlank
    private String domain;

    @NotNull
    @Min(1)
    private Integer numberOfBook;
}
