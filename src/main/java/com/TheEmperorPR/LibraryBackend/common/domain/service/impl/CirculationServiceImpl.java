package com.TheEmperorPR.LibraryBackend.common.domain.service.impl;

import com.TheEmperorPR.LibraryBackend.common.database.dto.BorrowRecordResponse;
import com.TheEmperorPR.LibraryBackend.common.database.dto.CheckoutRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.ReturnRequest;
import com.TheEmperorPR.LibraryBackend.common.database.entity.*;
import com.TheEmperorPR.LibraryBackend.common.database.repository.*;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.*;
import com.TheEmperorPR.LibraryBackend.common.domain.service.CirculationService;
import com.TheEmperorPR.LibraryBackend.core.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CirculationServiceImpl implements CirculationService {

    private final BooksRecordRepo booksRecordRepo;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final StudentProfileRepo studentProfileRepo;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;

    private static final BigDecimal DAILY_FINE_RATE = BigDecimal.valueOf(1.00);
    private static final BigDecimal FINE_HOLD_THRESHOLD = BigDecimal.valueOf(10.00);

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public BorrowRecordResponse checkoutBook(CheckoutRequest request, String staffUsername) {
        StudentProfileEntity student = studentProfileRepo.findById(request.getStudentProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile", "id", request.getStudentProfileId()));

        if (student.isHasActiveHold()) {
            throw new InvalidOperationException("Student account has an active hold due to unpaid fines");
        }

        long activeLoans = borrowRecordRepository.countByStudent_IdAndStatus(student.getId(), BorrowStatus.ACTIVE);
        if (activeLoans >= student.getMaxBorrowLimit()) {
            throw new BorrowLimitExceededException(String.format("Student has reached maximum borrow limit of %d books", student.getMaxBorrowLimit()));
        }

        BookCopy copy;
        Book book;

        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            copy = bookCopyRepository.findByBarcodeWithLock(request.getBarcode().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("BookCopy", "barcode", request.getBarcode()));

            if (copy.getStatus() != BookCopyStatus.AVAILABLE) {
                throw new BookUnavailableException(String.format("Book copy with barcode '%s' is not available (Current status: %s)", copy.getBarcode(), copy.getStatus()));
            }
            book = booksRecordRepo.findByIdWithLock(copy.getBook().getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", "id", copy.getBook().getBookId()));
        } else if (request.getBookId() != null) {
            book = booksRecordRepo.findByIdWithLock(request.getBookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

            if (book.getAvailableCopies() <= 0) {
                throw new BookUnavailableException(String.format("All copies of '%s' are currently checked out", book.getBookName()));
            }

            copy = bookCopyRepository.findFirstAvailableCopyWithLock(book.getBookId(), BookCopyStatus.AVAILABLE)
                    .orElseThrow(() -> new BookUnavailableException(String.format("No available physical copies found for book '%s'", book.getBookName())));
        } else {
            throw new InvalidOperationException("Either bookId or barcode must be provided for checkout");
        }

        // Atomically transition copy and book inventory
        copy.setStatus(BookCopyStatus.ISSUED);
        bookCopyRepository.save(copy);

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        booksRecordRepo.save(book);

        int loanDuration = request.getLoanDays() > 0 ? request.getLoanDays() : 14;
        LocalDateTime now = LocalDateTime.now();

        BookIssueRecord record = BookIssueRecord.builder()
                .bookCopy(copy)
                .student(student)
                .issueDate(now)
                .dueDate(now.plusDays(loanDuration))
                .status(BorrowStatus.ACTIVE)
                .issuedByStaffId(staffUsername)
                .renewalCount(0)
                .build();

        BookIssueRecord savedRecord = borrowRecordRepository.save(record);
        return mapToResponse(savedRecord, BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public BorrowRecordResponse returnBook(ReturnRequest request, String staffUsername) {
        BookIssueRecord record;

        if (request.getBorrowRecordId() != null) {
            record = borrowRecordRepository.findById(request.getBorrowRecordId())
                    .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord", "id", request.getBorrowRecordId()));
        } else if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            record = borrowRecordRepository.findByBookCopy_BarcodeAndStatus(request.getBarcode().trim(), BorrowStatus.ACTIVE)
                    .or(() -> borrowRecordRepository.findByBookCopy_BarcodeAndStatus(request.getBarcode().trim(), BorrowStatus.OVERDUE))
                    .orElseThrow(() -> new ResourceNotFoundException("Active borrow record not found for barcode: " + request.getBarcode()));
        } else {
            throw new InvalidOperationException("Either borrowRecordId or barcode must be provided to return book");
        }

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new InvalidOperationException("This book has already been returned");
        }

        LocalDateTime returnTime = LocalDateTime.now();
        record.setReturnDate(returnTime);
        record.setStatus(BorrowStatus.RETURNED);
        record.setReceivedByStaffId(staffUsername);

        BookCopy copy = record.getBookCopy();
        if (request.getConditionNote() != null) {
            copy.setConditionNote(request.getConditionNote());
        }

        // Calculate late fine if overdue
        BigDecimal fineAmount = BigDecimal.ZERO;
        if (returnTime.isAfter(record.getDueDate())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate().toLocalDate(), LocalDate.now());
            if (overdueDays > 0) {
                fineAmount = DAILY_FINE_RATE.multiply(BigDecimal.valueOf(overdueDays));
                Fine fine = Fine.builder()
                        .borrowRecord(record)
                        .amount(fineAmount)
                        .paidAmount(BigDecimal.ZERO)
                        .status(FineStatus.UNPAID)
                        .assessedDate(returnTime)
                        .remarks(String.format("Returned %d days late", overdueDays))
                        .build();
                fineRepository.save(fine);

                // Check hold threshold
                BigDecimal totalUnpaid = fineRepository.calculateTotalUnpaidFineByStudentId(record.getStudent().getId());
                if (totalUnpaid.compareTo(FINE_HOLD_THRESHOLD) >= 0) {
                    record.getStudent().setHasActiveHold(true);
                    studentProfileRepo.save(record.getStudent());
                }
            }
        }

        Book book = copy.getBook();
        // Check waitlist for book
        Optional<Reservation> nextReservation = reservationRepository.findFirstByBook_BookIdAndStatusOrderByQueuePositionAsc(
                book.getBookId(), ReservationStatus.WAITING);

        if (nextReservation.isPresent()) {
            Reservation reservation = nextReservation.get();
            reservation.setStatus(ReservationStatus.READY_FOR_PICKUP);
            reservation.setExpiryDate(returnTime.plusHours(48)); // 48-hour claim window
            reservationRepository.save(reservation);

            copy.setStatus(BookCopyStatus.RESERVED);
        } else {
            copy.setStatus(BookCopyStatus.AVAILABLE);
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            booksRecordRepo.save(book);
        }

        bookCopyRepository.save(copy);
        BookIssueRecord savedRecord = borrowRecordRepository.save(record);

        return mapToResponse(savedRecord, fineAmount);
    }

    @Override
    @Transactional
    public BorrowRecordResponse renewBook(Long recordId, String requesterUsername) {
        BookIssueRecord record = borrowRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("BorrowRecord", "id", recordId));

        if (record.getStatus() != BorrowStatus.ACTIVE) {
            throw new InvalidOperationException("Only ACTIVE loans can be renewed (Current status: " + record.getStatus() + ")");
        }

        if (record.getRenewalCount() >= 2) {
            throw new InvalidOperationException("Maximum renewal limit of 2 times has been reached for this loan");
        }

        Long bookId = record.getBookCopy().getBook().getBookId();
        long pendingReservations = reservationRepository.countByBook_BookIdAndStatus(bookId, ReservationStatus.WAITING);
        if (pendingReservations > 0) {
            throw new InvalidOperationException("Cannot renew book: other students are waiting in reservation queue");
        }

        record.setRenewalCount(record.getRenewalCount() + 1);
        record.setDueDate(record.getDueDate().plusDays(14));
        BookIssueRecord savedRecord = borrowRecordRepository.save(record);

        return mapToResponse(savedRecord, BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BorrowRecordResponse> getStudentLoans(Long studentProfileId, Pageable pageable) {
        return borrowRecordRepository.findByStudent_Id(studentProfileId, pageable)
                .map(r -> mapToResponse(r, BigDecimal.ZERO));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BorrowRecordResponse> getMyActiveLoans(String studentUsername) {
        StudentProfileEntity student = studentProfileRepo.findByUser_Username(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile for " + studentUsername + " not found"));

        return borrowRecordRepository.findByStudent_IdAndStatus(student.getId(), BorrowStatus.ACTIVE)
                .stream()
                .map(r -> mapToResponse(r, BigDecimal.ZERO))
                .collect(Collectors.toList());
    }

    private BorrowRecordResponse mapToResponse(BookIssueRecord record, BigDecimal fineAmount) {
        return BorrowRecordResponse.builder()
                .recordId(record.getId())
                .bookId(record.getBookCopy().getBook().getBookId())
                .bookTitle(record.getBookCopy().getBook().getBookName())
                .authorName(record.getBookCopy().getBook().getAuthorName())
                .barcode(record.getBookCopy().getBarcode())
                .studentId(record.getStudent().getId())
                .studentMemberId(record.getStudent().getStudentId())
                .studentName(record.getStudent().getFirstName() + " " + record.getStudent().getLastName())
                .issueDate(record.getIssueDate())
                .dueDate(record.getDueDate())
                .returnDate(record.getReturnDate())
                .status(record.getStatus())
                .renewalCount(record.getRenewalCount())
                .issuedByStaffId(record.getIssuedByStaffId())
                .receivedByStaffId(record.getReceivedByStaffId())
                .accruedFine(fineAmount)
                .build();
    }
}
