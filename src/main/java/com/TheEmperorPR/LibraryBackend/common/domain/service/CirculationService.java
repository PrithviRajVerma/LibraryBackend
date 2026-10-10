package com.TheEmperorPR.LibraryBackend.common.domain.service;

import com.TheEmperorPR.LibraryBackend.common.database.dto.BorrowRecordResponse;
import com.TheEmperorPR.LibraryBackend.common.database.dto.CheckoutRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.ReturnRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CirculationService {

    BorrowRecordResponse checkoutBook(CheckoutRequest request, String staffUsername);

    BorrowRecordResponse returnBook(ReturnRequest request, String staffUsername);

    BorrowRecordResponse renewBook(Long recordId, String requesterUsername);

    Page<BorrowRecordResponse> getStudentLoans(Long studentProfileId, Pageable pageable);

    List<BorrowRecordResponse> getMyActiveLoans(String studentUsername);
}
