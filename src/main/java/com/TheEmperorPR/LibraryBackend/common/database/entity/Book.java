package com.TheEmperorPR.LibraryBackend.common.database.entity;

import com.TheEmperorPR.LibraryBackend.common.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id", nullable = false, updatable = false)
    private Long bookId;

    @Column(name = "isbn", unique = true)
    private String isbn;

    @Column(name = "book_name", nullable = false)
    private String bookName;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "edition")
    private String edition;

    @Column(name = "domain", nullable = false)
    private String domain;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "total_copies", nullable = false)
    @Builder.Default
    private int numberOfBooks = 1;

    @Column(name = "available_copies", nullable = false)
    @Builder.Default
    private int availableCopies = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "issuing_authority")
    private Role issuingAuthority;

    @Column(name = "issuing_authority_id")
    private String issuingAuthorityId;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
