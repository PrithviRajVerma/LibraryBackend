package com.TheEmperorPR.LibraryBackend.common.database.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class StaffProfileEntity {

    @Id
    private Long id;

    private String username;
    private String password;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private StaffCred staffCred;

}
