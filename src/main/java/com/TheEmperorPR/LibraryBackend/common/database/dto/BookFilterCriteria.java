package com.TheEmperorPR.LibraryBackend.common.database.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookFilterCriteria {

    private String query;       // Matches title or author
    private String title;
    private String author;
    private String domain;      // Category
    private String isbn;
    private Boolean availableOnly;
}
