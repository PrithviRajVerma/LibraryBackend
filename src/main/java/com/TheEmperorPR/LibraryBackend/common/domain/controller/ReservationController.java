package com.TheEmperorPR.LibraryBackend.common.domain.controller;

import com.TheEmperorPR.LibraryBackend.common.Auth.CustomUserDetails;
import com.TheEmperorPR.LibraryBackend.common.database.dto.ReservationResponse;
import com.TheEmperorPR.LibraryBackend.common.domain.service.ReservationService;
import com.TheEmperorPR.LibraryBackend.core.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations & Waitlist", description = "Endpoints for reserving out-of-stock books and tracking waitlist queues")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping("/{bookId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Join the waitlist queue for a book when all physical copies are on loan")
    public ResponseEntity<ApiResponse<ReservationResponse>> reserveBook(
            @PathVariable("bookId") Long bookId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReservationResponse response = reservationService.reserveBook(bookId, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Successfully joined reservation waitlist", response));
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "Cancel an active reservation")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
            @PathVariable("reservationId") Long reservationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        reservationService.cancelReservation(reservationId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.message("Reservation cancelled successfully"));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get all reservations made by the current student")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMyReservations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReservationResponse> reservations = reservationService.getMyReservations(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(reservations));
    }

    @GetMapping("/book/{bookId}")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @Operation(summary = "View current active waitlist queue for a specific book title")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getBookWaitlist(@PathVariable("bookId") Long bookId) {
        List<ReservationResponse> waitlist = reservationService.getBookWaitlist(bookId);
        return ResponseEntity.ok(ApiResponse.success(waitlist));
    }
}
