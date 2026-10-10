package com.TheEmperorPR.LibraryBackend.common.database.repository;

import com.TheEmperorPR.LibraryBackend.common.database.entity.Fine;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.FineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {

    Optional<Fine> findByBorrowRecord_Id(Long borrowRecordId);

    List<Fine> findByBorrowRecord_Student_Id(Long studentId);

    List<Fine> findByBorrowRecord_Student_IdAndStatus(Long studentId, FineStatus status);

    Page<Fine> findByBorrowRecord_Student_Id(Long studentId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(f.amount - f.paidAmount), 0) FROM Fine f WHERE f.borrowRecord.student.id = :studentId AND f.status != 'PAID' AND f.status != 'WAIVED'")
    BigDecimal calculateTotalUnpaidFineByStudentId(@Param("studentId") Long studentId);
}
