package com.TheEmperorPR.LibraryBackend.common.database.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequest {

    private Long borrowRecordId;
    private String barcode;
    private String conditionNote;
}
