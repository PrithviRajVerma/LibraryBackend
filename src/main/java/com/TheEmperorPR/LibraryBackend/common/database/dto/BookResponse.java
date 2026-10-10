package com.TheEmperorPR.LibraryBackend.common.database.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookResponse {

    private Long bookId;
    private String isbn;
    private String bookName;
    private String authorName;
    private String publisher;
    private String edition;
    private String domain;
    private String description;
    private int totalCopies;
    private int availableCopies;
    private LocalDateTime createdAt;
    private List<BookCopyResponse> copies;
}
