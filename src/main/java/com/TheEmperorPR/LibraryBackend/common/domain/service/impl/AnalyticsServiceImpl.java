package com.TheEmperorPR.LibraryBackend.common.domain.service.impl;

import com.TheEmperorPR.LibraryBackend.common.database.dto.AnalyticsResponse;
import com.TheEmperorPR.LibraryBackend.common.database.repository.*;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.BookCopyStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.BorrowStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.FineStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.ReservationStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final BooksRecordRepo booksRecordRepo;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final StudentProfileRepo studentProfileRepo;
    private final StaffProfileRepo staffProfileRepo;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getSystemAnalytics() {
        long totalTitles = booksRecordRepo.count();
        long totalCopies = bookCopyRepository.count();
        long availableCopies = bookCopyRepository.countByBook_BookIdAndStatus(null, BookCopyStatus.AVAILABLE);
        // Note: countByBook_BookIdAndStatus with null bookId queries all books if query is:
        // Let's use custom queries or compute cleanly:
        long actualAvailable = bookCopyRepository.findAll().stream().filter(c -> c.getStatus() == BookCopyStatus.AVAILABLE).count();
        long activeLoans = borrowRecordRepository.findAll().stream().filter(r -> r.getStatus() == BorrowStatus.ACTIVE).count();
        long overdueLoans = borrowRecordRepository.findAll().stream().filter(r -> r.getStatus() == BorrowStatus.OVERDUE).count();
        long waitingReservations = reservationRepository.findAll().stream().filter(r -> r.getStatus() == ReservationStatus.WAITING).count();

        long totalStudents = studentProfileRepo.count();
        long totalStaff = staffProfileRepo.count();

        BigDecimal collectedRevenue = fineRepository.findAll().stream()
                .map(f -> f.getPaidAmount() != null ? f.getPaidAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outstandingFines = fineRepository.findAll().stream()
                .filter(f -> f.getStatus() != FineStatus.PAID && f.getStatus() != FineStatus.WAIVED)
                .map(f -> f.getAmount().subtract(f.getPaidAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AnalyticsResponse.builder()
                .totalTitles(totalTitles)
                .totalPhysicalCopies(totalCopies)
                .availablePhysicalCopies(actualAvailable)
                .activeLoansCount(activeLoans)
                .overdueLoansCount(overdueLoans)
                .totalReservationsWaiting(waitingReservations)
                .totalStudents(totalStudents)
                .totalStaff(totalStaff)
                .totalCollectedRevenue(collectedRevenue)
                .totalOutstandingFines(outstandingFines)
                .build();
    }
}
