package com.TheEmperorPR.LibraryBackend.fine;

import com.TheEmperorPR.LibraryBackend.common.database.dto.FinePaymentRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.FineResponse;
import com.TheEmperorPR.LibraryBackend.common.database.entity.*;
import com.TheEmperorPR.LibraryBackend.common.database.repository.*;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.FineStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.service.impl.FineServiceImpl;
import com.TheEmperorPR.LibraryBackend.core.exception.InvalidOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FineServiceTest {

    @Mock
    private FineRepository fineRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private StudentProfileRepo studentProfileRepo;

    @InjectMocks
    private FineServiceImpl fineService;

    private StudentProfileEntity student;
    private Fine fine;

    @BeforeEach
    void setUp() {
        student = StudentProfileEntity.builder()
                .id(1L)
                .studentId("STU001")
                .firstName("John")
                .lastName("Doe")
                .hasActiveHold(true)
                .build();

        Book book = Book.builder()
                .bookId(10L)
                .bookName("Design Patterns")
                .authorName("Gang of Four")
                .build();

        BookCopy copy = BookCopy.builder()
                .id(100L)
                .barcode("DP-001")
                .book(book)
                .build();

        BookIssueRecord record = BookIssueRecord.builder()
                .id(50L)
                .bookCopy(copy)
                .student(student)
                .build();

        fine = Fine.builder()
                .id(10L)
                .borrowRecord(record)
                .amount(BigDecimal.valueOf(15.00))
                .paidAmount(BigDecimal.ZERO)
                .status(FineStatus.UNPAID)
                .assessedDate(LocalDateTime.now())
                .build();
    }

    @Test
    void payFine_FullPayment_AutoReleasesStudentHold() {
        FinePaymentRequest request = FinePaymentRequest.builder()
                .amount(BigDecimal.valueOf(15.00))
                .remarks("Paid in full at counter")
                .build();

        when(fineRepository.findById(10L)).thenReturn(Optional.of(fine));
        when(fineRepository.save(any(Fine.class))).thenAnswer(i -> i.getArgument(0));
        when(fineRepository.calculateTotalUnpaidFineByStudentId(1L)).thenReturn(BigDecimal.ZERO);

        FineResponse response = fineService.payFine(10L, request);

        assertEquals(FineStatus.PAID, response.getStatus());
        assertEquals(BigDecimal.valueOf(15.00), response.getPaidAmount());
        assertEquals(BigDecimal.ZERO, response.getRemainingAmount());
        assertFalse(student.isHasActiveHold());
        verify(studentProfileRepo).save(student);
    }

    @Test
    void payFine_PartialPayment_StatusPartiallyPaid() {
        FinePaymentRequest request = FinePaymentRequest.builder()
                .amount(BigDecimal.valueOf(5.00))
                .build();

        when(fineRepository.findById(10L)).thenReturn(Optional.of(fine));
        when(fineRepository.save(any(Fine.class))).thenAnswer(i -> i.getArgument(0));
        when(fineRepository.calculateTotalUnpaidFineByStudentId(1L)).thenReturn(BigDecimal.valueOf(10.00));

        FineResponse response = fineService.payFine(10L, request);

        assertEquals(FineStatus.PARTIALLY_PAID, response.getStatus());
        assertEquals(BigDecimal.valueOf(5.00), response.getPaidAmount());
        assertEquals(BigDecimal.valueOf(10.00), response.getRemainingAmount());
        assertTrue(student.isHasActiveHold());
    }

    @Test
    void payFine_FailsWhenAmountExceedsRemainingBalance() {
        FinePaymentRequest request = FinePaymentRequest.builder()
                .amount(BigDecimal.valueOf(25.00))
                .build();

        when(fineRepository.findById(10L)).thenReturn(Optional.of(fine));

        assertThrows(InvalidOperationException.class, () -> fineService.payFine(10L, request));
        verify(fineRepository, never()).save(any(Fine.class));
    }
}
