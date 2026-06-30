package dev.vorstu.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "admins")
@EqualsAndHashCode(callSuper = true)
public class Admin extends BaseUser {

    @Column(name = "admin_level", nullable = false)
    private String adminLevel; // Например: "GLOBAL", "FACULTY"
}