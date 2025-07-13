package com.TheEmperorPR.LibraryBackend.common.database.entity;


import com.TheEmperorPR.LibraryBackend.common.domain.enums.Department;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student")
@Data
@NoArgsConstructor
public class StudentProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "libraryId" , nullable = false, updatable = false)
    private long libraryId;

    @Column(name = "studentId" , nullable = false , updatable = false)
    private String studentId;

    @Column(name = "name" , nullable = false, updatable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "contactNo",nullable = false)
    private int contactNo;

    private Department department;





}
