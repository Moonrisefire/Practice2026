package dev.vorstu.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    public Student() {}

    public Student(Long id, String fio, String group, String phoneNumber) {
        this(fio, group, phoneNumber);
        this.id = id;
    }

    public Student(String fio, String group, String phoneNumber) {
        this.fio = fio;
        this.group = group;
        this.phoneNumber = phoneNumber;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fio;
    @Column(name = "group_of_students")
    private String group;
    private String phoneNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFio() {
        return fio;
    }

    public void setFio(String fio) {
        this.fio = fio;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
