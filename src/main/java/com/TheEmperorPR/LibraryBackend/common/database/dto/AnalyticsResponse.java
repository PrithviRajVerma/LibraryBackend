package com.TheEmperorPR.LibraryBackend.common.database.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {

    private long totalTitles;
    private long totalPhysicalCopies;
    private long availablePhysicalCopies;
    private long activeLoansCount;
    private long overdueLoansCount;
    private long totalReservationsWaiting;
    private long totalStudents;
    private long totalStaff;
    private BigDecimal totalCollectedRevenue;
    private BigDecimal totalOutstandingFines;
}
