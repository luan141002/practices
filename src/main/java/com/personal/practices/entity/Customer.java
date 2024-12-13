package com.personal.practices.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @Column
    private UUID id;

    @Column
    private Long machineId;
}
