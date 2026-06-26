package dev.vorstu.controllers;

import dev.vorstu.dto.Student;
import dev.vorstu.repositories.StudentRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("api/base")
public class BaseController {
    private StudentRepository studentRepository;

    public BaseController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping(value = "students", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @PostMapping(value = "students", produces = MediaType.APPLICATION_JSON_VALUE)
    public Student createStudent(@RequestBody Student newStudent) {
        return studentRepository.save(newStudent);
    }

    @PutMapping(value = "students", produces = MediaType.APPLICATION_JSON_VALUE)
    public Student updateStudent(@RequestBody Student student) {
        if (student.getId() == null) {
            throw new RuntimeException("The ID cannot be empty when updating");
        }
        return studentRepository.save(student);
    }

    @DeleteMapping(value = "students/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Long deleteStudent(@PathVariable("id") Long id) {
        studentRepository.deleteById(id);
        return id;
    }

    @GetMapping(value = "students/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Optional<Student> getStudentById(@PathVariable("id") Long id) {
        return studentRepository.findById(id);
    }

    @GetMapping(value = "students/filter", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<Student> getStudentsByGroup(@RequestParam(value = "group") String group) {
        return studentRepository.findByGroup(group);
    }

//    private Long counter =0L;
//    private Long generateId() {return counter++;}
//    private final List<Student> students = new ArrayList<>();
//
//    @PostConstruct
//    public void init() {
//        students.add(new Student(0L, "User1", "VM", "+7"));
//        students.add(new Student(1L, "User2", "VM", "+8"));
//        students.add(new Student(2L, "User3", "AM", "+99"));
//    }
//
//    @PostMapping(value = "students", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Student createStudent(@RequestBody Student newStudent) {return addStudent(newStudent);}
//
//    private Student addStudent(Student student) {
//        student.setId(generateId());
//        students.add(student);
//        return student;
//    }
//
//    @PutMapping(value = "students", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Student changeStudent(@RequestBody Student changingStudent) {
//        return updateStudent(changingStudent);
//    }
//
//    private Student updateStudent(Student student) {
//        if(student.getId() == null) {
//            throw new RuntimeException("if of changing student cannot be null");
//        }
//        Student changingStudent = students.stream()
//                .filter(el -> Objects.equals(el.getId(), student.getId()))
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("student with id: " + student.getId() + " was not found"));
//
//        changingStudent.setFio(student.getFio());
//        changingStudent.setGroup(student.getGroup());
//        changingStudent.setPhoneNumber(student.getPhoneNumber());
//        return student;
//    }
//
//    @DeleteMapping(value = "students/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Long deleteStudent(@PathVariable("id") Long id) {
//        return removeStudent(id);
//    }
//
//    private Long removeStudent(Long id) {
//        students.removeIf(el -> el.getId().equals(id));
//        return id;
//    }
//
//    @GetMapping("students")
//    public List<Student> getAllStudents() {
//        return students;
//    }
//
//    @GetMapping(value = "students/{id}")
//    public Student getStudentById(@PathVariable("id") Long id) {
//        return students.stream().filter(el -> el.getId().equals(id))
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("student with id: " + id + " was not found"));
//    }
//
//    @GetMapping(value = "students/filter", produces = MediaType.APPLICATION_JSON_VALUE)
//    public List<Student> getStudentByGroup(@RequestParam(value = "group") String group) {
//        return students.stream()
//                .filter(el -> el.getGroup().equals(group))
//                .toList();
//    }
//
//    @GetMapping("check")
//    public String greetJava() {
//        return "Hello World " + new Date();
//    }
}
