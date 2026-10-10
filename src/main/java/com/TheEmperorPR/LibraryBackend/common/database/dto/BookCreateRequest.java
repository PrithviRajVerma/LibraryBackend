package com.TheEmperorPR.LibraryBackend.common.database.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCreateRequest {

    private String isbn;

    @NotBlank(message = "Book name / title is required")
    private String bookName;

    @NotBlank(message = "Author name is required")
    private String authorName;

    private String publisher;

    private String edition;

    @NotBlank(message = "Domain / category is required")
    private String domain;

    private String description;

    @NotNull(message = "Initial copy count is required")
    @Min(value = 1, message = "Initial copy count must be at least 1")
    @Builder.Default
    private Integer numberOfBooks = 1;

    private String defaultRackLocation;
}
