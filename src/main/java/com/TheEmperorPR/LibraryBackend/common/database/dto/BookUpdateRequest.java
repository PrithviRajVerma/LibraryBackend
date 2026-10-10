package com.TheEmperorPR.LibraryBackend.common.database.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookUpdateRequest {

    @NotBlank(message = "Book name / title is required")
    private String bookName;

    @NotBlank(message = "Author name is required")
    private String authorName;

    private String publisher;
    private String edition;

    @NotBlank(message = "Domain is required")
    private String domain;

    private String description;
}
