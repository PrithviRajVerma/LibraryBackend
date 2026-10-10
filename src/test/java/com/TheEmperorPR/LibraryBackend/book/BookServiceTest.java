package com.TheEmperorPR.LibraryBackend.book;

import com.TheEmperorPR.LibraryBackend.common.database.dto.BookCreateRequest;
import com.TheEmperorPR.LibraryBackend.common.database.dto.BookResponse;
import com.TheEmperorPR.LibraryBackend.common.database.entity.Book;
import com.TheEmperorPR.LibraryBackend.common.database.repository.BookCopyRepository;
import com.TheEmperorPR.LibraryBackend.common.database.repository.BooksRecordRepo;
import com.TheEmperorPR.LibraryBackend.common.domain.enums.Role;
import com.TheEmperorPR.LibraryBackend.common.domain.service.impl.BookServiceImpl;
import com.TheEmperorPR.LibraryBackend.core.exception.DuplicateResourceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BooksRecordRepo booksRecordRepo;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void createBook_GeneratesPhysicalCopiesAndReturnsResponse() {
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-0132350884")
                .bookName("Clean Code")
                .authorName("Robert C. Martin")
                .domain("Computer Science")
                .numberOfBooks(3)
                .build();

        when(booksRecordRepo.existsByIsbn("978-0132350884")).thenReturn(false);
        when(booksRecordRepo.save(any(Book.class))).thenAnswer(i -> {
            Book b = i.getArgument(0);
            b.setBookId(1L);
            return b;
        });
        when(bookCopyRepository.existsByBarcode(anyString())).thenReturn(false);
        when(bookCopyRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        BookResponse response = bookService.createBook(request, "admin_user", Role.ADMIN);

        assertNotNull(response);
        assertEquals("Clean Code", response.getBookName());
        assertEquals(3, response.getTotalCopies());
        assertEquals(3, response.getAvailableCopies());
        verify(booksRecordRepo).save(any(Book.class));
        verify(bookCopyRepository).saveAll(argThat(list -> java.util.stream.StreamSupport.stream(list.spliterator(), false).count() == 3));
    }

    @Test
    void createBook_FailsWhenIsbnDuplicate() {
        BookCreateRequest request = BookCreateRequest.builder()
                .isbn("978-0132350884")
                .bookName("Clean Code")
                .authorName("Robert C. Martin")
                .domain("Computer Science")
                .build();

        when(booksRecordRepo.existsByIsbn("978-0132350884")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> bookService.createBook(request, "admin", Role.ADMIN));
        verify(booksRecordRepo, never()).save(any(Book.class));
    }
}
