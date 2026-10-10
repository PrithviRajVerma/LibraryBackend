package com.TheEmperorPR.LibraryBackend.common.domain.service;

import com.TheEmperorPR.LibraryBackend.common.database.dto.FinePaymentRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.FineResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FineService {

    Page<FineResponse> getStudentFines(Long studentProfileId, Pageable pageable);

    List<FineResponse> getMyFines(String studentUsername);

    FineResponse payFine(Long fineId, FinePaymentRequest request);

    FineResponse waiveFine(Long fineId, String adminReason);

    int assessOverdueLoansAndApplyFines();
}
