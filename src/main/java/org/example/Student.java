package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Student {
    private int id;
    private String firstName;
    private String lastName;
    private List<Discipline> disciplines;

    public Student() {
        this.disciplines = new ArrayList<>();
    }

    public Student(int id, String firstName, String lastName) {
        if (id <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        if (firstName == null || firstName.trim().isEmpty() || lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name and last name cannot be empty.");
        }
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.disciplines = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<Discipline> getDisciplines() {
        return new ArrayList<>(disciplines);
    }

    public void setId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Student ID must be positive.");
        }
        this.id = id;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("First name cannot be empty.");
        }
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("Last name cannot be empty.");
        }
        this.lastName = lastName;
    }

    public void setDisciplines(List<Discipline> disciplines) {
        this.disciplines = new ArrayList<>(disciplines); // Створюємо копію
    }

    public void addOrUpdateDiscipline(Discipline discipline) {
        if (discipline == null) {
            System.err.println("Cannot add a null discipline.");
            return;
        }
        Optional<Discipline> existing = disciplines.stream()
                .filter(d -> d.getName().equalsIgnoreCase(discipline.getName()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setGrade(discipline.getGrade());
        } else {
            disciplines.add(discipline);
        }
    }

    public boolean removeDiscipline(String disciplineName) {
        if (disciplineName == null || disciplineName.trim().isEmpty()) {
            return false;
        }
        return disciplines.removeIf(d -> d.getName().equalsIgnoreCase(disciplineName));
    }

    public double calculateAverageGrade() {
        if (disciplines == null || disciplines.isEmpty()) {
            return 0.0;
        }
        List<Discipline> gradedDisciplines = disciplines.stream().collect(Collectors.toList());

        if (gradedDisciplines.isEmpty()) {
            return 0.0;
        }

        double sum = gradedDisciplines.stream()
                .mapToInt(Discipline::getGrade)
                .sum();
        return sum / gradedDisciplines.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", averageGrade=" + String.format("%.2f", calculateAverageGrade()) + // Показуємо середній бал
                ", disciplines=" + disciplines +
                '}';
    }
}