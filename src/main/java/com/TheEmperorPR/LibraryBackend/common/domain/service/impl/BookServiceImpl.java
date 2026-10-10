package com.TheEmperorPR.LibraryBackend.common.domain.service.impl;

import com.TheEmperorPR.LibraryBackend.common.database.dto.*;
import com.TheEmperorPR.LibraryBackend.common.database.entity.Book;
import com.TheEmperorPR.LibraryBackend.common.database.entity.BookCopy;
import com.TheEmperorPR.LibraryBackend.common.database.repository.BookCopyRepository;
import com.TheEmperorPR.LibraryBackend.common.database.repository.BooksRecordRepo;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.BookCopyStatus;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.Role;
import com.TheEmperorPR.LibraryBackend.common.domain.service.BookService;
import com.TheEmperorPR.LibraryBackend.common.domain.specification.BookSpecification;
import com.TheEmperorPR.LibraryBackend.core.exception.DuplicateResourceException;
import com.TheEmperorPR.LibraryBackend.core.exception.InvalidOperationException;
import com.TheEmperorPR.LibraryBackend.core.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BooksRecordRepo booksRecordRepo;
    private final BookCopyRepository bookCopyRepository;

    @Override
    @Transactional
    public BookResponse createBook(BookCreateRequest request, String authorityId, Role authorityRole) {
        if (request.getIsbn() != null && !request.getIsbn().isBlank()) {
            if (booksRecordRepo.existsByIsbn(request.getIsbn().trim())) {
                throw new DuplicateResourceException("Book", "isbn", request.getIsbn());
            }
        }

        int copyCount = request.getNumberOfBooks() != null ? request.getNumberOfBooks() : 1;

        Book book = Book.builder()
                .isbn(request.getIsbn() != null ? request.getIsbn().trim() : null)
                .bookName(request.getBookName().trim())
                .authorName(request.getAuthorName().trim())
                .publisher(request.getPublisher())
                .edition(request.getEdition())
                .domain(request.getDomain().trim())
                .description(request.getDescription())
                .numberOfBooks(copyCount)
                .availableCopies(copyCount)
                .issuingAuthority(authorityRole)
                .issuingAuthorityId(authorityId)
                .build();

        Book savedBook = booksRecordRepo.save(book);

        List<BookCopy> copies = new ArrayList<>();
        String isbnBase = savedBook.getIsbn() != null ? savedBook.getIsbn() : ("BK" + savedBook.getBookId());
        for (int i = 1; i <= copyCount; i++) {
            String barcode = String.format("%s-%03d", isbnBase, i);
            if (bookCopyRepository.existsByBarcode(barcode)) {
                barcode = barcode + "-" + UUID.randomUUID().toString().substring(0, 4);
            }
            BookCopy copy = BookCopy.builder()
                    .book(savedBook)
                    .barcode(barcode)
                    .status(BookCopyStatus.AVAILABLE)
                    .rackLocation(request.getDefaultRackLocation() != null ? request.getDefaultRackLocation() : "General-Shelf")
                    .build();
            copies.add(copy);
        }
        List<BookCopy> savedCopies = bookCopyRepository.saveAll(copies);

        return mapToBookResponse(savedBook, savedCopies);
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long bookId, BookUpdateRequest request) {
        Book book = booksRecordRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        book.setBookName(request.getBookName().trim());
        book.setAuthorName(request.getAuthorName().trim());
        book.setPublisher(request.getPublisher());
        book.setEdition(request.getEdition());
        book.setDomain(request.getDomain().trim());
        book.setDescription(request.getDescription());

        Book updatedBook = booksRecordRepo.save(book);
        List<BookCopy> copies = bookCopyRepository.findByBook_BookId(bookId);
        return mapToBookResponse(updatedBook, copies);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        Book book = booksRecordRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        long issuedCount = bookCopyRepository.countByBook_BookIdAndStatus(bookId, BookCopyStatus.ISSUED);
        if (issuedCount > 0) {
            throw new InvalidOperationException("Cannot delete book: " + issuedCount + " copies are currently issued to students");
        }

        booksRecordRepo.delete(book);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long bookId) {
        Book book = booksRecordRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));
        List<BookCopy> copies = bookCopyRepository.findByBook_BookId(bookId);
        return mapToBookResponse(book, copies);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(BookFilterCriteria criteria, Pageable pageable) {
        return booksRecordRepo.findAll(BookSpecification.withCriteria(criteria), pageable)
                .map(b -> mapToBookResponse(b, null));
    }

    @Override
    @Transactional
    public List<BookCopyResponse> addCopies(Long bookId, List<BookCopyRequest> requests) {
        Book book = booksRecordRepo.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        List<BookCopy> newCopies = new ArrayList<>();
        for (BookCopyRequest r : requests) {
            if (bookCopyRepository.existsByBarcode(r.getBarcode())) {
                throw new DuplicateResourceException("BookCopy", "barcode", r.getBarcode());
            }
            BookCopy copy = BookCopy.builder()
                    .book(book)
                    .barcode(r.getBarcode())
                    .status(BookCopyStatus.AVAILABLE)
                    .rackLocation(r.getRackLocation())
                    .conditionNote(r.getConditionNote())
                    .build();
            newCopies.add(copy);
        }

        List<BookCopy> saved = bookCopyRepository.saveAll(newCopies);

        // Update counts on Book entity
        book.setNumberOfBooks((int) bookCopyRepository.countByBook_BookId(bookId));
        book.setAvailableCopies((int) bookCopyRepository.countByBook_BookIdAndStatus(bookId, BookCopyStatus.AVAILABLE));
        booksRecordRepo.save(book);

        return saved.stream().map(this::mapToCopyResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookCopyResponse updateCopyStatus(Long copyId, BookCopyStatus status, String conditionNote) {
        BookCopy copy = bookCopyRepository.findById(copyId)
                .orElseThrow(() -> new ResourceNotFoundException("BookCopy", "id", copyId));

        copy.setStatus(status);
        if (conditionNote != null) copy.setConditionNote(conditionNote);
        BookCopy saved = bookCopyRepository.save(copy);

        // Refresh book available copy count
        Book book = copy.getBook();
        book.setAvailableCopies((int) bookCopyRepository.countByBook_BookIdAndStatus(book.getBookId(), BookCopyStatus.AVAILABLE));
        booksRecordRepo.save(book);

        return mapToCopyResponse(saved);
    }

    private BookResponse mapToBookResponse(Book book, List<BookCopy> copies) {
        return BookResponse.builder()
                .bookId(book.getBookId())
                .isbn(book.getIsbn())
                .bookName(book.getBookName())
                .authorName(book.getAuthorName())
                .publisher(book.getPublisher())
                .edition(book.getEdition())
                .domain(book.getDomain())
                .description(book.getDescription())
                .totalCopies(book.getNumberOfBooks())
                .availableCopies(book.getAvailableCopies())
                .createdAt(book.getCreatedAt())
                .copies(copies != null ? copies.stream().map(this::mapToCopyResponse).collect(Collectors.toList()) : null)
                .build();
    }

    private BookCopyResponse mapToCopyResponse(BookCopy copy) {
        return BookCopyResponse.builder()
                .copyId(copy.getId())
                .bookId(copy.getBook().getBookId())
                .barcode(copy.getBarcode())
                .status(copy.getStatus())
                .rackLocation(copy.getRackLocation())
                .conditionNote(copy.getConditionNote())
                .build();
    }
}
