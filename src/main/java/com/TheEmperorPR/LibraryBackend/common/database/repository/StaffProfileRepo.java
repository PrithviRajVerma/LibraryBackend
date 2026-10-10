package com.TheEmperorPR.LibraryBackend.common.database.repository;

import com.TheEmperorPR.LibraryBackend.common.database.entity.StaffProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffProfileRepo extends JpaRepository<StaffProfileEntity, Long> {

    Optional<StaffProfileEntity> findByEmployeeId(String employeeId);

    Optional<StaffProfileEntity> findByUser_Username(String username);

    Optional<StaffProfileEntity> findByUser_Id(Long userId);

    Optional<StaffProfileEntity> findByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);
}
