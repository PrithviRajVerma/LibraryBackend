package com.TheEmperorPR.LibraryBackend.circulation;

import com.TheEmperorPR.LibraryBackend.common.database.dto.BorrowRecordResponse;
import com.TheEmperorPR.LibraryBackend.common.database.dto.CheckoutRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.ReturnRequest;
import com.TheEmperorPR.LibraryBackend.common.database.entity.*;
import com.TheEmperorPR.LibraryBackend.common.database.repository.*;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.BookCopyStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.BorrowStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.ReservationStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.service.impl.CirculationServiceImpl;
import com.TheEmperorPR.LibraryBackend.core.exception.BookUnavailableException;
import com.TheEmperorPR.LibraryBackend.core.exception.BorrowLimitExceededException;
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
public class CirculationServiceTest {

    @Mock
    private BooksRecordRepo booksRecordRepo;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private BorrowRecordRepository borrowRecordRepository;

    @Mock
    private StudentProfileRepo studentProfileRepo;

    @Mock
    private FineRepository fineRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private CirculationServiceImpl circulationService;

    private StudentProfileEntity student;
    private Book book;
    private BookCopy copy;

    @BeforeEach
    void setUp() {
        student = StudentProfileEntity.builder()
                .id(1L)
                .studentId("STU001")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .maxBorrowLimit(3)
                .hasActiveHold(false)
                .build();

        book = Book.builder()
                .bookId(10L)
                .bookName("Clean Architecture")
                .authorName("Robert C. Martin")
                .domain("Software Engineering")
                .numberOfBooks(2)
                .availableCopies(2)
                .build();

        copy = BookCopy.builder()
                .id(100L)
                .book(book)
                .barcode("CA-001")
                .status(BookCopyStatus.AVAILABLE)
                .build();
    }

    @Test
    void checkoutBook_Success() {
        CheckoutRequest request = CheckoutRequest.builder()
                .studentProfileId(1L)
                .bookId(10L)
                .loanDays(14)
                .build();

        when(studentProfileRepo.findById(1L)).thenReturn(Optional.of(student));
        when(borrowRecordRepository.countByStudent_IdAndStatus(1L, BorrowStatus.ACTIVE)).thenReturn(0L);
        when(booksRecordRepo.findByIdWithLock(10L)).thenReturn(Optional.of(book));
        when(bookCopyRepository.findFirstAvailableCopyWithLock(10L, BookCopyStatus.AVAILABLE)).thenReturn(Optional.of(copy));
        when(borrowRecordRepository.save(any(BookIssueRecord.class))).thenAnswer(i -> {
            BookIssueRecord r = i.getArgument(0);
            r.setId(500L);
            return r;
        });

        BorrowRecordResponse response = circulationService.checkoutBook(request, "staff_admin");

        assertNotNull(response);
        assertEquals(500L, response.getRecordId());
        assertEquals("Clean Architecture", response.getBookTitle());
        assertEquals(1, book.getAvailableCopies());
        assertEquals(BookCopyStatus.ISSUED, copy.getStatus());
        verify(bookCopyRepository).save(copy);
        verify(booksRecordRepo).save(book);
    }

    @Test
    void checkoutBook_FailsWhenStudentHasActiveHold() {
        student.setHasActiveHold(true);
        CheckoutRequest request = CheckoutRequest.builder()
                .studentProfileId(1L)
                .bookId(10L)
                .build();

        when(studentProfileRepo.findById(1L)).thenReturn(Optional.of(student));

        assertThrows(InvalidOperationException.class, () -> circulationService.checkoutBook(request, "staff"));
        verifyNoInteractions(bookCopyRepository);
    }

    @Test
    void checkoutBook_FailsWhenBorrowLimitExceeded() {
        CheckoutRequest request = CheckoutRequest.builder()
                .studentProfileId(1L)
                .bookId(10L)
                .build();

        when(studentProfileRepo.findById(1L)).thenReturn(Optional.of(student));
        when(borrowRecordRepository.countByStudent_IdAndStatus(1L, BorrowStatus.ACTIVE)).thenReturn(3L);

        assertThrows(BorrowLimitExceededException.class, () -> circulationService.checkoutBook(request, "staff"));
        verifyNoInteractions(booksRecordRepo);
    }

    @Test
    void checkoutBook_FailsWhenNoCopiesAvailable() {
        book.setAvailableCopies(0);
        CheckoutRequest request = CheckoutRequest.builder()
                .studentProfileId(1L)
                .bookId(10L)
                .build();

        when(studentProfileRepo.findById(1L)).thenReturn(Optional.of(student));
        when(borrowRecordRepository.countByStudent_IdAndStatus(1L, BorrowStatus.ACTIVE)).thenReturn(1L);
        when(booksRecordRepo.findByIdWithLock(10L)).thenReturn(Optional.of(book));

        assertThrows(BookUnavailableException.class, () -> circulationService.checkoutBook(request, "staff"));
    }

    @Test
    void returnBook_OnTimeWithoutWaitlist_RestoresCopyAvailable() {
        BookIssueRecord record = BookIssueRecord.builder()
                .id(200L)
                .bookCopy(copy)
                .student(student)
                .issueDate(LocalDateTime.now().minusDays(5))
                .dueDate(LocalDateTime.now().plusDays(9))
                .status(BorrowStatus.ACTIVE)
                .build();

        ReturnRequest request = ReturnRequest.builder()
                .borrowRecordId(200L)
                .build();

        when(borrowRecordRepository.findById(200L)).thenReturn(Optional.of(record));
        when(reservationRepository.findFirstByBook_BookIdAndStatusOrderByQueuePositionAsc(10L, ReservationStatus.WAITING))
                .thenReturn(Optional.empty());
        when(borrowRecordRepository.save(any(BookIssueRecord.class))).thenAnswer(i -> i.getArgument(0));

        BorrowRecordResponse response = circulationService.returnBook(request, "staff");

        assertEquals(BorrowStatus.RETURNED, response.getStatus());
        assertEquals(BookCopyStatus.AVAILABLE, copy.getStatus());
        assertEquals(3, book.getAvailableCopies());
        verify(booksRecordRepo).save(book);
    }

    @Test
    void returnBook_WhenWaitlistExists_ReservesCopyForTopQueue() {
        BookIssueRecord record = BookIssueRecord.builder()
                .id(200L)
                .bookCopy(copy)
                .student(student)
                .issueDate(LocalDateTime.now().minusDays(5))
                .dueDate(LocalDateTime.now().plusDays(9))
                .status(BorrowStatus.ACTIVE)
                .build();

        Reservation reservation = Reservation.builder()
                .id(999L)
                .book(book)
                .student(student)
                .status(ReservationStatus.WAITING)
                .build();

        ReturnRequest request = ReturnRequest.builder()
                .borrowRecordId(200L)
                .build();

        when(borrowRecordRepository.findById(200L)).thenReturn(Optional.of(record));
        when(reservationRepository.findFirstByBook_BookIdAndStatusOrderByQueuePositionAsc(10L, ReservationStatus.WAITING))
                .thenReturn(Optional.of(reservation));
        when(borrowRecordRepository.save(any(BookIssueRecord.class))).thenAnswer(i -> i.getArgument(0));

        BorrowRecordResponse response = circulationService.returnBook(request, "staff");

        assertEquals(BorrowStatus.RETURNED, response.getStatus());
        assertEquals(BookCopyStatus.RESERVED, copy.getStatus());
        assertEquals(ReservationStatus.READY_FOR_PICKUP, reservation.getStatus());
        assertNotNull(reservation.getExpiryDate());
        verify(reservationRepository).save(reservation);
    }

    @Test
    void renewBook_FailsWhenWaitlistExists() {
        BookIssueRecord record = BookIssueRecord.builder()
                .id(200L)
                .bookCopy(copy)
                .student(student)
                .status(BorrowStatus.ACTIVE)
                .renewalCount(0)
                .build();

        when(borrowRecordRepository.findById(200L)).thenReturn(Optional.of(record));
        when(reservationRepository.countByBook_BookIdAndStatus(10L, ReservationStatus.WAITING)).thenReturn(1L);

        assertThrows(InvalidOperationException.class, () -> circulationService.renewBook(200L, "john"));
    }
}
