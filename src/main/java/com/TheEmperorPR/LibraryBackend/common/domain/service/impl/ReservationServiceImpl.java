package com.TheEmperorPR.LibraryBackend.common.domain.service.impl;

import com.TheEmperorPR.LibraryBackend.common.database.dto.ReservationResponse;
import com.TheEmperorPR.LibraryBackend.common.database.entity.Book;
import com.TheEmperorPR.LibraryBackend.common.database.entity.Reservation;
import com.TheEmperorPR.LibraryBackend.common.database.entity.StudentProfileEntity;
import com.TheEmperorPR.LibraryBackend.common.database.repository.BooksRecordRepo;
import com.TheEmperorPR.LibraryBackend.common.database.repository.ReservationRepository;
import com.TheEmperorPR.LibraryBackend.common.database.repository.StudentProfileRepo;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.ReservationStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.service.ReservationService;
import com.TheEmperorPR.LibraryBackend.core.exception.InvalidOperationException;
import com.TheEmperorPR.LibraryBackend.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final BooksRecordRepo booksRecordRepo;
    private final StudentProfileRepo studentProfileRepo;

    @Override
    @Transactional
    public ReservationResponse reserveBook(Long bookId, String studentUsername) {
        StudentProfileEntity student = studentProfileRepo.findByUser_Username(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile for user " + studentUsername + " not found"));

        if (student.isHasActiveHold()) {
            throw new InvalidOperationException("Cannot place reservation: student account has an active hold due to unpaid fines");
        }

        Book book = booksRecordRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        if (book.getAvailableCopies() > 0) {
            throw new InvalidOperationException(String.format("Book '%s' currently has %d available copies. Please checkout directly rather than joining waitlist.", book.getBookName(), book.getAvailableCopies()));
        }

        boolean alreadyReserved = reservationRepository.existsByBook_BookIdAndStudent_IdAndStatusIn(
                bookId, student.getId(), List.of(ReservationStatus.WAITING, ReservationStatus.READY_FOR_PICKUP));
        if (alreadyReserved) {
            throw new InvalidOperationException("You already have an active reservation for this book");
        }

        int nextQueuePosition = (int) reservationRepository.countByBook_BookIdAndStatus(bookId, ReservationStatus.WAITING) + 1;

        Reservation reservation = Reservation.builder()
                .book(book)
                .student(student)
                .reservedDate(LocalDateTime.now())
                .queuePosition(nextQueuePosition)
                .status(ReservationStatus.WAITING)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId, String studentUsername) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation", "id", reservationId));

        StudentProfileEntity student = studentProfileRepo.findByUser_Username(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile for " + studentUsername + " not found"));

        if (!reservation.getStudent().getId().equals(student.getId())) {
            throw new InvalidOperationException("You can only cancel your own reservations");
        }

        if (reservation.getStatus() != ReservationStatus.WAITING && reservation.getStatus() != ReservationStatus.READY_FOR_PICKUP) {
            throw new InvalidOperationException("Only WAITING or READY_FOR_PICKUP reservations can be cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyReservations(String studentUsername) {
        StudentProfileEntity student = studentProfileRepo.findByUser_Username(studentUsername)
                .orElseThrow(() -> new ResourceNotFoundException("StudentProfile for " + studentUsername + " not found"));

        return reservationRepository.findByStudent_Id(student.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getBookWaitlist(Long bookId) {
        return reservationRepository.findByBook_BookIdAndStatusOrderByQueuePositionAsc(bookId, ReservationStatus.WAITING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .reservationId(reservation.getId())
                .bookId(reservation.getBook().getBookId())
                .bookTitle(reservation.getBook().getBookName())
                .authorName(reservation.getBook().getAuthorName())
                .studentId(reservation.getStudent().getId())
                .studentMemberId(reservation.getStudent().getStudentId())
                .studentName(reservation.getStudent().getFirstName() + " " + reservation.getStudent().getLastName())
                .reservedDate(reservation.getReservedDate())
                .expiryDate(reservation.getExpiryDate())
                .queuePosition(reservation.getQueuePosition())
                .status(reservation.getStatus())
                .build();
    }
}
