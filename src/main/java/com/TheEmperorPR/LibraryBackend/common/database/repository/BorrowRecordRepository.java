package com.TheEmperorPR.LibraryBackend.common.database.repository;

import com.TheEmperorPR.LibraryBackend.common.database.entity.BookIssueRecord;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.BorrowStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BookIssueRecord, Long> {

    List<BookIssueRecord> findByStudent_IdAndStatus(Long studentId, BorrowStatus status);

    long countByStudent_IdAndStatus(Long studentId, BorrowStatus status);

    Optional<BookIssueRecord> findByBookCopy_BarcodeAndStatus(String barcode, BorrowStatus status);

    Optional<BookIssueRecord> findByBookCopy_IdAndStatus(Long copyId, BorrowStatus status);

    List<BookIssueRecord> findByStatusAndDueDateBefore(BorrowStatus status, LocalDateTime now);

    Page<BookIssueRecord> findByStudent_Id(Long studentId, Pageable pageable);

    Page<BookIssueRecord> findByStatus(BorrowStatus status, Pageable pageable);
}
