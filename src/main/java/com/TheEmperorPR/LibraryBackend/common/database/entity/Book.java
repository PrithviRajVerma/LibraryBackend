package com.TheEmperorPR.LibraryBackend.common.database.entity;

import com.TheEmperorPR.LibraryBackend.common.domain.enums.Role;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "BooksRecord")
@Data
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookId",nullable = false,updatable = false)
    private long bookId;

    @Column(name = "bookName",nullable = false,updatable = false)
    private String bookName;

    @Column(name = "authorName", nullable = false,updatable = false)
    private String authorName;

    @Column(name = "edition")
    private String edition;

    @Column(name = "domain",nullable = false,updatable = false)
    private String Domain;

    @Column(name = "numberOfBooks",nullable = false)
    private int numberOfBooks;

    @Column(name = "addOnDate",nullable = false,updatable = false)
    private Date addOnDate;

    @Column(name = "issuingAuthority",nullable = false,updatable = false)
    private Role issuingAuthority;

    @Column(name = "issuingAuthorityId",nullable = false,updatable = false)
    private String issuingAuthorityId;

}
