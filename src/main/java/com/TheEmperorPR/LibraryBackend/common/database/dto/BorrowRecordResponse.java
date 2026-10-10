package com.TheEmperorPR.LibraryBackend.common.database.dto;

import com.TheEmperorPR.LibraryBackend.common.domain.enums.BorrowStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BorrowRecordResponse {

    private Long recordId;
    private Long bookId;
    private String bookTitle;
    private String authorName;
    private String barcode;
    private Long studentId;
    private String studentMemberId;
    private String studentName;
    private LocalDateTime issueDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private BorrowStatus status;
    private int renewalCount;
    private String issuedByStaffId;
    private String receivedByStaffId;
    private BigDecimal accruedFine;
}
