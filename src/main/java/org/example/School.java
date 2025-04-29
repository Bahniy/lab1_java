package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class School {
    private String name;
    private List<Student> students;

    // Потрібен для десеріалізації Jackson
    public School() {
        this.students = new ArrayList<>();
    }

    public School(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("School name cannot be empty.");
        }
        this.name = name;
        this.students = new ArrayList<>();
    }

    // Гетери
    public String getName() {
        return name;
    }

    public List<Student> getStudents() {
        return Collections.unmodifiableList(students);
    }

    // Сетери
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("School name cannot be empty.");
        }
        this.name = name;
    }

    public void setStudents(List<Student> students) {
        this.students = new ArrayList<>(students); // Створюємо копію
    }

    public boolean addStudent(Student student) {
        if (student == null) {
            System.err.println("Cannot add null student.");
            return false;
        }
        if (students.stream().anyMatch(s -> s.getId() == student.getId())) {
            System.err.println("Student with ID " + student.getId() + " already exists.");
            return false;
        }
        return students.add(student);
    }

    public Optional<Student> getStudentById(int id) {
        return students.stream()
                .filter(s -> s.getId() == id)
                .findFirst();
    }

    public boolean updateStudent(Student updatedStudent) {
        if (updatedStudent == null) return false;
        Optional<Student> existingStudentOpt = getStudentById(updatedStudent.getId());
        if (existingStudentOpt.isPresent()) {
            // Видаляємо старий запис і додаємо новий
            students.remove(existingStudentOpt.get());
            students.add(updatedStudent);
            return true;
        }
        return false;
    }

    public boolean removeStudent(int id) {
        return students.removeIf(student -> student.getId() == id);
    }

    // Бізнес-логіка
    public double calculateSchoolAverageGrade() {
        if (students == null || students.isEmpty()) {
            return 0.0;
        }

        // Отримуємо середні бали всіх учнів, які мають оцінки (> 0.0)
        List<Double> studentAverages = students.stream()
                .map(Student::calculateAverageGrade)
                .filter(avg -> avg > 0.0) // Враховуємо тільки учнів з оцінками
                .collect(Collectors.toList());

        if (studentAverages.isEmpty()) {
            return 0.0; // Немає учнів з оцінками
        }

        double sumOfAverages = studentAverages.stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        return sumOfAverages / studentAverages.size();
    }

    public void sortStudents(Comparator<Student> comparator) {
        if (students != null && comparator != null) {
            students.sort(comparator);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        School school = (School) o;
        return Objects.equals(name, school.name) && Objects.equals(students, school.students);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, students);
    }

    @Override
    public String toString() {
        return "School{" +
                "name='" + name + '\'' +
                ", numberOfStudents=" + (students != null ? students.size() : 0) +
                ", schoolAverageGrade=" + String.format("%.2f", calculateSchoolAverageGrade()) +
                '}';
    }
}