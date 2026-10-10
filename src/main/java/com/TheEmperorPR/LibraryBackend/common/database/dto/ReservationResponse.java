package com.TheEmperorPR.LibraryBackend.common.database.dto;

import com.TheEmperorPR.LibraryBackend.common.domain.enums.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReservationResponse {

    private Long reservationId;
    private Long bookId;
    private String bookTitle;
    private String authorName;
    private Long studentId;
    private String studentMemberId;
    private String studentName;
    private LocalDateTime reservedDate;
    private LocalDateTime expiryDate;
    private int queuePosition;
    private ReservationStatus status;
}
