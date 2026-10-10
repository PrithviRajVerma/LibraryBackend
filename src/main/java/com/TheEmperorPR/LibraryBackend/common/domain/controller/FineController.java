package com.TheEmperorPR.LibraryBackend.common.domain.controller;

import com.TheEmperorPR.LibraryBackend.common.Auth.CustomUserDetails;
import com.TheEmperorPR.LibraryBackend.common.database.dto.FinePaymentRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.FineResponse;
import com.TheEmperorPR.LibraryBackend.common.domain.service.FineService;
import com.TheEmperorPR.LibraryBackend.core.response.ApiResponse;
import com.TheEmperorPR.LibraryBackend.core.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fines")
@RequiredArgsConstructor
@Tag(name = "Fines & Payments", description = "Endpoints for viewing outstanding fees, recording payments, and triggering batch audits")
public class FineController {

    private final FineService fineService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "View outstanding and settled fines for the current authenticated student")
    public ResponseEntity<ApiResponse<List<FineResponse>>> getMyFines(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<FineResponse> fines = fineService.getMyFines(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(fines));
    }

    @GetMapping("/student/{studentProfileId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "View fine records for a specific student (Staff & Admin)")
    public ResponseEntity<ApiResponse<PageResponse<FineResponse>>> getStudentFines(
            @PathVariable("studentProfileId") Long studentProfileId,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<FineResponse> response = PageResponse.from(fineService.getStudentFines(studentProfileId, pageable));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{fineId}/pay")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "Record a fine payment (full or partial) and auto-release account holds if under threshold")
    public ResponseEntity<ApiResponse<FineResponse>> payFine(
            @PathVariable("fineId") Long fineId,
            @Valid @RequestBody FinePaymentRequest request) {
        FineResponse updated = fineService.payFine(fineId, request);
        return ResponseEntity.ok(ApiResponse.success("Fine payment recorded successfully", updated));
    }

    @PostMapping("/{fineId}/waive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Waive an accrued fine (Admin only)")
    public ResponseEntity<ApiResponse<FineResponse>> waiveFine(
            @PathVariable("fineId") Long fineId,
            @RequestParam(value = "reason", required = false) String reason) {
        FineResponse waived = fineService.waiveFine(fineId, reason);
        return ResponseEntity.ok(ApiResponse.success("Fine waived successfully", waived));
    }

    @PostMapping("/batch-evaluate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Manually trigger the overdue loan audit and fine calculation batch process")
    public ResponseEntity<ApiResponse<Map<String, Object>>> triggerBatchAudit() {
        int processed = fineService.assessOverdueLoansAndApplyFines();
        return ResponseEntity.ok(ApiResponse.success("Batch evaluation complete", Map.of("processedLoans", processed)));
    }
}
