package com.TheEmperorPR.LibraryBackend.common.domain.controller;

import com.TheEmperorPR.LibraryBackend.common.Auth.CustomUserDetails;
import com.TheEmperorPR.LibraryBackend.common.database.dto.BorrowRecordResponse;
import com.TheEmperorPR.LibraryBackend.common.database.dto.CheckoutRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.ReturnRequest;
import com.TheEmperorPR.LibraryBackend.common.domain.service.CirculationService;
import com.TheEmperorPR.LibraryBackend.core.response.ApiResponse;
import com.TheEmperorPR.LibraryBackend.core.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/circulation")
@RequiredArgsConstructor
@Tag(name = "Circulation & Loan Operations", description = "Endpoints for book checkout, return, renewal, and loan history with pessimistic concurrency protection")
public class CirculationController {

    private final CirculationService circulationService;

    @PostMapping("/checkout")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Checkout a book to a student with concurrency locking and limit validation")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> checkoutBook(
            @Valid @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String staffUsername = userDetails != null ? userDetails.getUsername() : "STAFF";
        BorrowRecordResponse response = circulationService.checkoutBook(request, staffUsername);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book successfully checked out", response));
    }

    @PostMapping("/return")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Return a book copy, assess fines if overdue, and trigger waitlist notifications")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> returnBook(
            @Valid @RequestBody ReturnRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String staffUsername = userDetails != null ? userDetails.getUsername() : "STAFF";
        BorrowRecordResponse response = circulationService.returnBook(request, staffUsername);
        return ResponseEntity.ok(ApiResponse.success("Book successfully returned", response));
    }

    @PostMapping("/renew/{recordId}")
    @Operation(summary = "Renew an active book loan for 14 additional days (max 2 times, blocked if waitlist exists)")
    public ResponseEntity<ApiResponse<BorrowRecordResponse>> renewBook(
            @PathVariable("recordId") Long recordId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String requester = userDetails != null ? userDetails.getUsername() : "USER";
        BorrowRecordResponse response = circulationService.renewBook(recordId, requester);
        return ResponseEntity.ok(ApiResponse.success("Book loan renewed successfully", response));
    }

    @GetMapping("/student/{studentProfileId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Get loan circulation history for a specific student")
    public ResponseEntity<ApiResponse<PageResponse<BorrowRecordResponse>>> getStudentLoans(
            @PathVariable("studentProfileId") Long studentProfileId,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<BorrowRecordResponse> response = PageResponse.from(circulationService.getStudentLoans(studentProfileId, pageable));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-loans")
    @Operation(summary = "Get all active loans for the currently authenticated student")
    public ResponseEntity<ApiResponse<List<BorrowRecordResponse>>> getMyActiveLoans(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<BorrowRecordResponse> loans = circulationService.getMyActiveLoans(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(loans));
    }
}
