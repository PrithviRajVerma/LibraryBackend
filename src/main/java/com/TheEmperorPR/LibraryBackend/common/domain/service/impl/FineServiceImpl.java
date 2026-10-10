package com.TheEmperorPR.LibraryBackend.common.domain.service.impl;

import com.TheEmperorPR.LibraryBackend.common.database.dto.FinePaymentRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.FineResponse;
import com.TheEmperorPR.LibraryBackend.common.database.entity.BookIssueRecord;
import com.TheEmperorPR.LibraryBackend.common.database.entity.Fine;
import com.TheEmperorPR.LibraryBackend.common.database.entity.StudentProfileEntity;
import com.TheEmperorPR.LibraryBackend.common.database.repository.BorrowRecordRepository;
import com.TheEmperorPR.LibraryBackend.common.database.repository.FineRepository;
import com.TheEmperorPR.LibraryBackend.common.database.repository.StudentProfileRepo;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.BorrowStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.FineStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.service.FineService;
import com.TheEmperorPR.LibraryBackend.core.exception.InvalidOperationException;
import com.TheEmperorPR.LibraryBackend.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
public class FineServiceImpl implements FineService {

    private final FineRepository fineRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final StudentProfileRepo studentProfileRepo;

    private static final BigDecimal DAILY_FINE_RATE = BigDecimal.valueOf(1.00);
    private static final BigDecimal FINE_HOLD_THRESHOLD = BigDecimal.valueOf(10.00);

    @Override
    @Transactional(readOnly = true)
    public Page<FineResponse> getStudentFines(Long studentProfileId, Pageable pageable) {
        return fineRepository.findByBorrowRecord_Student_Id(studentProfileId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FineResponse> getMyFines(String studentUsername) {
        StudentProfileEntity student = studentProfileRepo.findByUser_Username(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile for " + studentUsername + " not found"));

        return fineRepository.findByBorrowRecord_Student_Id(student.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FineResponse payFine(Long fineId, FinePaymentRequest request) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine", "id", fineId));

        if (fine.getStatus() == FineStatus.PAID) {
            throw new InvalidOperationException("This fine has already been fully paid");
        }
        if (fine.getStatus() == FineStatus.WAIVED) {
            throw new InvalidOperationException("This fine was waived and requires no payment");
        }

        BigDecimal remaining = fine.getAmount().subtract(fine.getPaidAmount());
        if (request.getAmount().compareTo(remaining) > 0) {
            throw new InvalidOperationException(String.format("Payment amount (%.2f) exceeds remaining fine balance (%.2f)", request.getAmount(), remaining));
        }

        BigDecimal newPaidAmount = fine.getPaidAmount().add(request.getAmount());
        fine.setPaidAmount(newPaidAmount);
        fine.setPaidDate(LocalDateTime.now());

        if (newPaidAmount.compareTo(fine.getAmount()) >= 0) {
            fine.setStatus(FineStatus.PAID);
        } else {
            fine.setStatus(FineStatus.PARTIALLY_PAID);
        }

        if (request.getRemarks() != null) {
            fine.setRemarks(fine.getRemarks() != null ? fine.getRemarks() + " | " + request.getRemarks() : request.getRemarks());
        }

        Fine saved = fineRepository.save(fine);

        // Re-evaluate hold status for the student
        StudentProfileEntity student = fine.getBorrowRecord().getStudent();
        BigDecimal remainingUnpaid = fineRepository.calculateTotalUnpaidFineByStudentId(student.getId());
        if (remainingUnpaid.compareTo(FINE_HOLD_THRESHOLD) < 0 && student.isHasActiveHold()) {
            student.setHasActiveHold(false);
            studentProfileRepo.save(student);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public FineResponse waiveFine(Long fineId, String adminReason) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException("Fine", "id", fineId));

        fine.setStatus(FineStatus.WAIVED);
        fine.setRemarks("Waived by Admin. Reason: " + (adminReason != null ? adminReason : "Administrative discretion"));
        Fine saved = fineRepository.save(fine);

        // Re-evaluate hold status for the student
        StudentProfileEntity student = fine.getBorrowRecord().getStudent();
        BigDecimal remainingUnpaid = fineRepository.calculateTotalUnpaidFineByStudentId(student.getId());
        if (remainingUnpaid.compareTo(FINE_HOLD_THRESHOLD) < 0 && student.isHasActiveHold()) {
            student.setHasActiveHold(false);
            studentProfileRepo.save(student);
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public int assessOverdueLoansAndApplyFines() {
        LocalDateTime now = LocalDateTime.now();
        List<BookIssueRecord> overdueLoans = borrowRecordRepository.findByStatusAndDueDateBefore(BorrowStatus.ACTIVE, now);

        int processed = 0;
        for (BookIssueRecord record : overdueLoans) {
            record.setStatus(BorrowStatus.OVERDUE);
            borrowRecordRepository.save(record);

            long overdueDays = ChronoUnit.DAYS.between(record.getDueDate().toLocalDate(), LocalDate.now());
            if (overdueDays > 0) {
                BigDecimal fineDue = DAILY_FINE_RATE.multiply(BigDecimal.valueOf(overdueDays));

                Optional<Fine> existingFineOpt = fineRepository.findByBorrowRecord_Id(record.getId());
                if (existingFineOpt.isPresent()) {
                    Fine existingFine = existingFineOpt.get();
                    if (existingFine.getStatus() != FineStatus.PAID && existingFine.getStatus() != FineStatus.WAIVED) {
                        existingFine.setAmount(fineDue);
                        existingFine.setRemarks(String.format("Overdue by %d days (auto-updated)", overdueDays));
                        fineRepository.save(existingFine);
                    }
                } else {
                    Fine newFine = Fine.builder()
                            .borrowRecord(record)
                            .amount(fineDue)
                            .paidAmount(BigDecimal.ZERO)
                            .status(FineStatus.UNPAID)
                            .assessedDate(now)
                            .remarks(String.format("Overdue by %d days (batch evaluated)", overdueDays))
                            .build();
                    fineRepository.save(newFine);
                }

                // Check student hold threshold
                StudentProfileEntity student = record.getStudent();
                BigDecimal totalUnpaid = fineRepository.calculateTotalUnpaidFineByStudentId(student.getId());
                if (totalUnpaid.compareTo(FINE_HOLD_THRESHOLD) >= 0 && !student.isHasActiveHold()) {
                    student.setHasActiveHold(true);
                    studentProfileRepo.save(student);
                }
            }
            processed++;
        }

        return processed;
    }

    private FineResponse mapToResponse(Fine fine) {
        BigDecimal remaining = fine.getAmount().subtract(fine.getPaidAmount());
        return FineResponse.builder()
                .fineId(fine.getId())
                .borrowRecordId(fine.getBorrowRecord().getId())
                .bookTitle(fine.getBorrowRecord().getBookCopy().getBook().getBookName())
                .barcode(fine.getBorrowRecord().getBookCopy().getBarcode())
                .studentId(fine.getBorrowRecord().getStudent().getId())
                .studentMemberId(fine.getBorrowRecord().getStudent().getStudentId())
                .studentName(fine.getBorrowRecord().getStudent().getFirstName() + " " + fine.getBorrowRecord().getStudent().getLastName())
                .amount(fine.getAmount())
                .paidAmount(fine.getPaidAmount())
                .remainingAmount(remaining.compareTo(BigDecimal.ZERO) > 0 ? remaining : BigDecimal.ZERO)
                .status(fine.getStatus())
                .assessedDate(fine.getAssessedDate())
                .paidDate(fine.getPaidDate())
                .remarks(fine.getRemarks())
                .build();
    }
}
