package com.TheEmperorPR.LibraryBackend.common.database.repository;

import com.TheEmperorPR.LibraryBackend.common.database.entity.Reservation;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByBook_BookIdAndStatusOrderByQueuePositionAsc(Long bookId, ReservationStatus status);

    Optional<Reservation> findFirstByBook_BookIdAndStatusOrderByQueuePositionAsc(Long bookId, ReservationStatus status);

    List<Reservation> findByStudent_Id(Long studentId);

    List<Reservation> findByStudent_IdAndStatus(Long studentId, ReservationStatus status);

    Page<Reservation> findByStudent_Id(Long studentId, Pageable pageable);

    boolean existsByBook_BookIdAndStudent_IdAndStatusIn(Long bookId, Long studentId, List<ReservationStatus> statuses);

    long countByBook_BookIdAndStatus(Long bookId, ReservationStatus status);

    List<Reservation> findByStatusAndExpiryDateBefore(ReservationStatus status, LocalDateTime now);
}
