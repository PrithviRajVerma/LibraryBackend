package com.TheEmperorPR.LibraryBackend.common.database.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookCopyRequest {

    @NotBlank(message = "Barcode is required")
    private String barcode;

    private String rackLocation;

    private String conditionNote;
}
