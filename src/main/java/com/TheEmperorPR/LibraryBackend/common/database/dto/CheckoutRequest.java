package com.TheEmperorPR.LibraryBackend.common.database.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {

    @NotNull(message = "Student ID is required")
    private Long studentProfileId;

    private Long bookId;

    private String barcode;

    @Builder.Default
    private int loanDays = 14;
}
