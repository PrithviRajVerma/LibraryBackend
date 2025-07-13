package com.TheEmperorPR.LibraryBackend.common.database.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class StaffCred {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @OneToOne(mappedBy = "staffCred")
    private StaffProfileEntity staffProfileEntity;
}
