package com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo;

import com.TheEmperorPR.LibraryBackend.common.database.entity.StudentCred;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentCredRepo extends JpaRepository<StudentCred,Long> {
    Optional<StudentCred> findByUsername(String username);
}
