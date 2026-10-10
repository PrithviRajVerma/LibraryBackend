package com.TheEmperorPR.LibraryBackend.common.database.dto;

import com.TheEmperorPR.LibraryBackend.common.domain.enums.BookCopyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCopyResponse {

    private Long copyId;
    private Long bookId;
    private String barcode;
    private BookCopyStatus status;
    private String rackLocation;
    private String conditionNote;
}
