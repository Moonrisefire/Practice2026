package dev.vorstu.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@Entity
@Table(name = "teachers")
@EqualsAndHashCode(callSuper = true)
public class Teacher extends BaseUser {

    @Column(nullable = false)
    private String fio;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true)
    private String phone;

    @ElementCollection
    @CollectionTable(name = "teacher_groups", joinColumns = @JoinColumn(name = "teacher_id"))
    @Column(name = "group_name")
    private Set<String> assignedGroups;
}