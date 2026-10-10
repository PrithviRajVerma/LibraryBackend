package com.TheEmperorPR.LibraryBackend.common.domain.service;

import com.TheEmperorPR.LibraryBackend.common.database.dto.ReservationResponse;

import java.util.List;

public interface ReservationService {

    ReservationResponse reserveBook(Long bookId, String studentUsername);

    void cancelReservation(Long reservationId, String studentUsername);

    List<ReservationResponse> getMyReservations(String studentUsername);

    List<ReservationResponse> getBookWaitlist(Long bookId);
}
