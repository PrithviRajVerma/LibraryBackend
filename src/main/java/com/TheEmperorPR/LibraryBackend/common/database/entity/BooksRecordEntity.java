package com.TheEmperorPR.LibraryBackend.common.database.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "BooksRecord")
public class BooksRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookId",nullable = false,updatable = false)
    private long bookId;

    @Column(name = "bookName",nullable = false,updatable = false)
    private String bookName;

    @Column(name = "authorName", nullable = false,updatable = false)
    private String authorName;

//    private String
}
