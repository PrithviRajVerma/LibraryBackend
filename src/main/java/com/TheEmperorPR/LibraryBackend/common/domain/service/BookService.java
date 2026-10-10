package com.TheEmperorPR.LibraryBackend.common.domain.service;

import com.TheEmperorPR.LibraryBackend.common.database.dto.*;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.BookCopyStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {

    BookResponse createBook(BookCreateRequest request, String authorityId, Role authorityRole);

    BookResponse updateBook(Long bookId, BookUpdateRequest request);

    void deleteBook(Long bookId);

    BookResponse getBookById(Long bookId);

    Page<BookResponse> searchBooks(BookFilterCriteria criteria, Pageable pageable);

    List<BookCopyResponse> addCopies(Long bookId, List<BookCopyRequest> requests);

    BookCopyResponse updateCopyStatus(Long copyId, BookCopyStatus status, String conditionNote);
}
