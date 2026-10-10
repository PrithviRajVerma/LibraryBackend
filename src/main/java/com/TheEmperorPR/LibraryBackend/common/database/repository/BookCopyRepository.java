package com.TheEmperorPR.LibraryBackend.common.database.repository;

import com.TheEmperorPR.LibraryBackend.common.database.entity.BookCopy;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.BookCopyStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    Optional<BookCopy> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    List<BookCopy> findByBook_BookId(Long bookId);

    List<BookCopy> findByBook_BookIdAndStatus(Long bookId, BookCopyStatus status);

    long countByBook_BookIdAndStatus(Long bookId, BookCopyStatus status);

    long countByBook_BookId(Long bookId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM BookCopy c WHERE c.book.bookId = :bookId AND c.status = :status ORDER BY c.id ASC LIMIT 1")
    Optional<BookCopy> findFirstAvailableCopyWithLock(@Param("bookId") Long bookId, @Param("status") BookCopyStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM BookCopy c WHERE c.barcode = :barcode")
    Optional<BookCopy> findByBarcodeWithLock(@Param("barcode") String barcode);
}
