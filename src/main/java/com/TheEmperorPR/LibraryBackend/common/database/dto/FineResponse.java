package com.TheEmperorPR.LibraryBackend.common.database.dto;

import com.TheEmperorPR.LibraryBackend.common.domain.enums.FineStatus;
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
public class FineResponse {

    private Long fineId;
    private Long borrowRecordId;
    private String bookTitle;
    private String barcode;
    private Long studentId;
    private String studentMemberId;
    private String studentName;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private FineStatus status;
    private LocalDateTime assessedDate;
    private LocalDateTime paidDate;
    private String remarks;
}
