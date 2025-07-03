package com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo;

import com.TheEmperorPR.LibraryBackend.common.database.entity.StaffCred;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffCredRepo extends JpaRepository<StaffCred, Long> {
    Optional<StaffCred> findByUsername(String username);
}
