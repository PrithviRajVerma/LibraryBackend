package com.TheEmperorPR.LibraryBackend.common.database.repository;

import com.TheEmperorPR.LibraryBackend.common.database.entity.StudentProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepo extends JpaRepository<StudentProfileEntity, Long> {

    Optional<StudentProfileEntity> findByStudentId(String studentId);

    Optional<StudentProfileEntity> findByUser_Username(String username);

    Optional<StudentProfileEntity> findByUser_Id(Long userId);

    Optional<StudentProfileEntity> findByEmail(String email);

    boolean existsByStudentId(String studentId);

    boolean existsByEmail(String email);
}
