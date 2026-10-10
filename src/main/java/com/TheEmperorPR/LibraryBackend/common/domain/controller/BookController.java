package com.TheEmperorPR.LibraryBackend.common.domain.controller;

import com.TheEmperorPR.LibraryBackend.common.Auth.CustomUserDetails;
import com.TheEmperorPR.LibraryBackend.common.database.dto.*;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.BookCopyStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.Role;
import com.TheEmperorPR.LibraryBackend.common.domain.service.BookService;
import com.TheEmperorPR.LibraryBackend.core.response.ApiResponse;
import com.TheEmperorPR.LibraryBackend.core.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Book Catalog & Inventory", description = "Endpoints for searching, cataloging, and managing physical book copies")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Search books with dynamic multi-criteria filtering, sorting, and pagination")
    public ResponseEntity<ApiResponse<PageResponse<BookResponse>>> searchBooks(
            @ModelAttribute BookFilterCriteria criteria,
            @PageableDefault(size = 10, sort = "bookName", direction = Sort.Direction.ASC) Pageable pageable) {
        PageResponse<BookResponse> response = PageResponse.from(bookService.searchBooks(criteria, pageable));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full book details including individual physical copy availability")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable("id") Long id) {
        BookResponse book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success(book));
    }

    @PostMapping
    @Operation(summary = "Create a new book title and auto-generate initial physical copy inventory (Admin & Staff)")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
            @Valid @RequestBody BookCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String authorityId = userDetails != null ? userDetails.getUsername() : "SYSTEM";
        Role authorityRole = userDetails != null ? userDetails.getRole() : Role.STAFF;
        BookResponse created = bookService.createBook(request, authorityId, authorityRole);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book created successfully with physical inventory", created));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update book catalog metadata (Admin & Staff)")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable("id") Long id,
            @Valid @RequestBody BookUpdateRequest request) {
        BookResponse updated = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book title if no copies are currently on loan (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable("id") Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.message("Book deleted successfully"));
    }

    @PostMapping("/{id}/copies")
    @Operation(summary = "Add additional physical copies with custom barcodes to an existing title")
    public ResponseEntity<ApiResponse<List<BookCopyResponse>>> addCopies(
            @PathVariable("id") Long id,
            @Valid @RequestBody List<BookCopyRequest> requests) {
        List<BookCopyResponse> copies = bookService.addCopies(id, requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Copies added successfully", copies));
    }

    @PutMapping("/copies/{copyId}/status")
    @Operation(summary = "Update physical copy status (e.g. AVAILABLE, DAMAGED, LOST, RETIRED)")
    public ResponseEntity<ApiResponse<BookCopyResponse>> updateCopyStatus(
            @PathVariable("copyId") Long copyId,
            @RequestParam("status") BookCopyStatus status,
            @RequestParam(value = "conditionNote", required = false) String conditionNote) {
        BookCopyResponse copy = bookService.updateCopyStatus(copyId, status, conditionNote);
        return ResponseEntity.ok(ApiResponse.success("Copy status updated successfully", copy));
    }
}
