package com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo;

import com.TheEmperorPR.LibraryBackend.common.database.entity.AdminCred;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@Repository
public interface AdminCredRepo extends JpaRepository<AdminCred,Long> {
    Optional<AdminCred> findByUsername(String username);
}
