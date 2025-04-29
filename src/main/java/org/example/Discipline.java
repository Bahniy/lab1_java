package org.example;

import java.util.Objects;

public class Discipline {
    private String name;
    private int grade; // Оцінка (від 1 до 12)

    // Потрібен для десеріалізації Jackson
    public Discipline() {}

    public Discipline(String name, int grade) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Дисципліна не може бути пустою.");
        }
        if (grade < 1 || grade > 12) {
            System.err.println("Оцінка " + grade + " для " + name + " не можлива, повинна бути 1-12.");
        }
        this.name = name;
        this.grade = grade;
    }

    // Гетери
    public String getName() {
        return name;
    }

    public int getGrade() {
        return grade;
    }

    // Сетери
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Discipline name cannot be empty.");
        }
        this.name = name;
    }

    public void setGrade(int grade) {
        if (grade < 1 || grade > 12) {
            System.err.println("Warning: Grade " + grade + " for " + name + " is outside the typical range (1-12).");
            // throw new IllegalArgumentException("Grade must be between 1 and 12.");
        }
        this.grade = grade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discipline that = (Discipline) o;
        // Дві дисципліни рівні, якщо у них однакова назва (в контексті учня)
        // Оцінка може бути різною (наприклад, якщо оновили)
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name + ": " + grade;
    }
}