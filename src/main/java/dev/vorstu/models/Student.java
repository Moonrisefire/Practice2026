package dev.vorstu.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "students")
@EqualsAndHashCode(callSuper = true)
public class Student extends BaseUser {

    @Column(nullable = false)
    private String fio;

    @Column(name = "group_name", nullable = false)
    private String groupName;
}