package org.example;

import java.io.IOException;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleMenu {

    private School school;
    private final DataService dataService;
    private final Scanner scanner;
    // Файл для збереження/завантаження
    private static final String DATA_FILE = "school_data.json";

    public ConsoleMenu() {
        this.dataService = new DataService();
        this.scanner = new Scanner(System.in);
        // Спробуємо завантажити дані при старті
        try {
            this.school = dataService.importSchoolFromJson(DATA_FILE);
            System.out.println("Завантажено дані для школи: " + school.getName());
        } catch (IOException e) {
            System.err.println("Не вдалося завантажити дані з " + DATA_FILE + ". Створюється нова школа. Помилка: " + e.getMessage());
            // Створюємо нову школу, якщо завантаження не вдалося
            this.school = new School("Школа без назви");
        }
    }

    public void run() {
        int choice;
        do {
            displayMenu();
            choice = readIntInput("Введіть ваш вибір: ");

            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    viewAllStudents();
                    break;
                case 3:
                    findStudentById();
                    break;
                case 4:
                    updateStudent();
                    break;
                case 5:
                    removeStudent();
                    break;
                case 6:
                    addDisciplineToStudent();
                    break;
                case 7:
                    removeDisciplineFromStudent();
                    break;
                case 8:
                    calculateStudentAverage();
                    break;
                case 9:
                    calculateSchoolAverage();
                    break;
                case 10:
                    exportData();
                    break;
                case 11:
                    importData(); // Дозволимо імпортувати вручну теж
                    break;
                case 0:
                    saveDataOnExit(); // Зберігаємо дані перед виходом
                    System.out.println("Завершення роботи програми...");
                    break;
                default:
                    System.out.println("Неправильний вибір. Спробуйте ще раз.");
            }
            System.out.println("------------------------------------");
        } while (choice != 0);

        scanner.close();
    }

    private void displayMenu() {
        System.out.println("\n--- Меню управління школою ---");
        System.out.println("Поточна школа: " + school.getName());
        System.out.println("1. Додати учня");
        System.out.println("2. Переглянути всіх учнів");
        System.out.println("3. Знайти учня за ID");
        System.out.println("4. Оновити інформацію про учня");
        System.out.println("5. Видалити учня");
        System.out.println("6. Додати/Оновити дисципліну для учня");
        System.out.println("7. Видалити дисципліну учня");
        System.out.println("8. Обчислити середній бал учня");
        System.out.println("9. Обчислити середній бал школи");
        System.out.println("10. Експортувати дані в JSON (з опціями сортування)");
        System.out.println("11. Імпортувати дані з JSON");
        System.out.println("0. Зберегти та вийти");
        System.out.println("------------------------------------");
    }

    private String readStringInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        while (input == null || input.trim().isEmpty()) {
            System.out.println("Введення не може бути порожнім. Спробуйте ще раз.");
            System.out.print(prompt);
            input = scanner.nextLine();
        }
        return input.trim();
    }

    private int readIntInput(String prompt) {
        System.out.print(prompt);
        int value = -1; // Початкове значення, яке не є валідним вибором меню
        boolean validInput = false;
        while (!validInput) {
            try {
                value = scanner.nextInt();
                validInput = true;
            } catch (InputMismatchException e) {
                System.out.println("Невірне введення. Будь ласка, введіть ціле число.");
                System.out.print(prompt); // Повторюємо запит
            } finally {
                scanner.nextLine();
            }
        }
        return value;
    }

    private int readGradeInput(String prompt) {
        int grade = -1; // Початкове невалідние значення
        while (grade < 1 || grade > 12) {
            grade = readIntInput(prompt + " (1-12): ");
            if (grade < 1 || grade > 12) {
                System.out.println("Невірна оцінка. Будь ласка, введіть значення від 1 до 12.");
            }
        }
        return grade;
    }

    // Методи для опцій меню

    private void addStudent() {
        System.out.println("--- Додавання нового учня ---");
        int id = -1;
        boolean idOk = false;
        while (!idOk) {
            id = readIntInput("Введіть ID учня: ");
            if (id <= 0) {
                System.out.println("ID повинен бути позитивним числом.");
            } else if (school.getStudentById(id).isPresent()) {
                System.out.println("Учень з ID " + id + " вже існує. Будь ласка, використайте інший ID.");
            } else {
                idOk = true;
            }
        }
        String firstName = readStringInput("Введіть ім'я: ");
        String lastName = readStringInput("Введіть прізвище: ");
        try {
            Student newStudent = new Student(id, firstName, lastName);
            if (school.addStudent(newStudent)) {
                System.out.println("Учня успішно додано: " + newStudent);
            } else {
                System.out.println("Не вдалося додати учня.");
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Помилка створення учня: " + e.getMessage());
        }
    }

    private void viewAllStudents() {
        System.out.println("--- Всі учні ---");
        List<Student> students = school.getStudents();
        if (students.isEmpty()) {
            System.out.println("У школі немає учнів.");
        } else {
            students.forEach(System.out::println);
        }
    }

    private void findStudentById() {
        System.out.println("--- Пошук учня за ID ---");
        int id = readIntInput("Введіть ID учня для пошуку: ");
        Optional<Student> studentOpt = school.getStudentById(id);
        if (studentOpt.isPresent()) {
            System.out.println("Учня знайдено: " + studentOpt.get());
        } else {
            System.out.println("Учня з ID " + id + " не знайдено.");
        }
    }

    private void updateStudent() {
        System.out.println("--- Оновлення інформації про учня ---");
        int id = readIntInput("Введіть ID учня для оновлення: ");
        Optional<Student> studentOpt = school.getStudentById(id);

        if (studentOpt.isPresent()) {
            Student existingStudent = studentOpt.get();
            System.out.println("Знайдено учня: " + existingStudent);

            String newFirstNamePrompt = "Введіть нове ім'я (або натисніть Enter, щоб залишити '" + existingStudent.getFirstName() + "'): ";
            String newFirstName = scanner.nextLine();
            if (newFirstName.trim().isEmpty()) {
                newFirstName = existingStudent.getFirstName();
            } else {
                newFirstName = newFirstName.trim();
            }

            String newLastNamePrompt = "Введіть нове прізвище (або натисніть Enter, щоб залишити '" + existingStudent.getLastName() + "'): ";
            String newLastName = scanner.nextLine();
            if (newLastName.trim().isEmpty()) {
                newLastName = existingStudent.getLastName();
            } else {
                newLastName = newLastName.trim();
            }

            try {
                Student updatedStudent = new Student(existingStudent.getId(), newFirstName, newLastName);
                updatedStudent.setDisciplines(existingStudent.getDisciplines());

                if (school.updateStudent(updatedStudent)) {
                    System.out.println("Інформацію про учня успішно оновлено.");
                } else {
                    System.out.println("Не вдалося оновити інформацію про учня.");
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Помилка оновлення учня: " + e.getMessage());
            }
        } else {
            System.out.println("Учня з ID " + id + " не знайдено.");
        }
    }


    private void removeStudent() {
        System.out.println("--- Видалення учня ---");
        int id = readIntInput("Введіть ID учня для видалення: ");
        if (school.removeStudent(id)) {
            System.out.println("Учня з ID " + id + " успішно видалено.");
        } else {
            System.out.println("Учня з ID " + id + " не знайдено.");
        }
    }

    private void addDisciplineToStudent() {
        System.out.println("--- Додавання/Оновлення дисципліни ---");
        int studentId = readIntInput("Введіть ID учня: ");
        Optional<Student> studentOpt = school.getStudentById(studentId);

        if (studentOpt.isPresent()) {
            Student student = studentOpt.get(); // Отримуємо об'єкт учня
            String disciplineName = readStringInput("Введіть назву дисципліни: ");
            int grade = readGradeInput("Введіть оцінку для " + disciplineName);
            try {
                Discipline discipline = new Discipline(disciplineName, grade);
                student.addOrUpdateDiscipline(discipline);
                school.updateStudent(student);
                System.out.println("Дисципліну '" + disciplineName + "' додано/оновлено для учня " + student.getFirstName() + ".");
            } catch (IllegalArgumentException e) {
               System.err.println("Помилка додавання дисципліни: " + e.getMessage());
            }
        } else {
            System.out.println("Учня з ID " + studentId + " не знайдено.");
        }
    }

    private void removeDisciplineFromStudent() {
        System.out.println("--- Видалення дисципліни ---");
        int studentId = readIntInput("Введіть ID учня: ");
        Optional<Student> studentOpt = school.getStudentById(studentId);

        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            if (student.getDisciplines().isEmpty()) {
                System.out.println("Учень " + student.getFirstName() + " не має дисциплін для видалення.");
                return;
            }
            System.out.println("Дисципліни для " + student.getFirstName() + ": " + student.getDisciplines());
            String disciplineName = readStringInput("Введіть назву дисципліни для видалення: ");

            if (student.removeDiscipline(disciplineName)) {
                school.updateStudent(student);
                System.out.println("Дисципліну '" + disciplineName + "' видалено для учня " + student.getFirstName() + ".");
            } else {
                System.out.println("Дисципліну '" + disciplineName + "' не знайдено для цього учня.");
            }
        } else {
            System.out.println("Учня з ID " + studentId + " не знайдено.");
        }
    }


    private void calculateStudentAverage() {
        System.out.println("--- Обчислення середнього балу учня ---");
        int id = readIntInput("Введіть ID учня: ");
        Optional<Student> studentOpt = school.getStudentById(id);
        if (studentOpt.isPresent()) {
            Student student = studentOpt.get();
            double avg = student.calculateAverageGrade();
            System.out.printf("Середній бал для %s %s (ID: %d) становить: %.2f%n",
                    student.getFirstName(), student.getLastName(), student.getId(), avg);
        } else {
            System.out.println("Учня з ID " + id + " не знайдено.");
        }
    }

    private void calculateSchoolAverage() {
        System.out.println("--- Обчислення середнього балу школи ---");
        double avg = school.calculateSchoolAverageGrade();
        System.out.printf("Загальний середній бал для школи '%s' становить: %.2f%n", school.getName(), avg);
    }

    private void exportData() {
        System.out.println("--- Експорт даних ---");
        Comparator<Student> sorter = null;

        System.out.println("Сортувати учнів перед експортом?");
        System.out.println("1. Без сортування");
        System.out.println("2. Сортувати за ID");
        System.out.println("3. Сортувати за прізвищем");
        System.out.println("4. Сортувати за середнім балом (за спаданням)");
        int sortChoice = readIntInput("Введіть варіант сортування: ");

        switch (sortChoice) {
            case 2:
                sorter = Comparator.comparingInt(Student::getId);
                System.out.println("Сортування за ID.");
                break;
            case 3:
                sorter = Comparator.comparing(Student::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Student::getFirstName, String.CASE_INSENSITIVE_ORDER);
                System.out.println("Сортування за прізвищем.");
                break;
            case 4:
                sorter = Comparator.comparingDouble(Student::calculateAverageGrade).reversed();
                System.out.println("Сортування за середнім балом (за спаданням).");
                break;
            case 1:
            default:
                System.out.println("Сортування не застосовано.");
                break;
        }

        String filenamePrompt = "Введіть ім'я файлу для експорту (за замовчуванням: " + DATA_FILE + "): ";
        String filenameInput = scanner.nextLine();
        String filename = filenameInput.trim().isEmpty() ? DATA_FILE : filenameInput.trim();

        if (!filename.toLowerCase().endsWith(".json")) {
            filename += ".json";
        }

        try {
            dataService.exportSchoolToJson(school, filename, sorter);
        } catch (IOException e) {
            System.err.println("Помилка експорту даних у файл " + filename + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Помилка експорту: " + e.getMessage());
        }
    }

    private void importData() {
        System.out.println("--- Імпорт даних ---");
        System.out.println("УВАГА: Імпорт перезапише поточні дані школи в пам'яті.");
        String confirmation = readStringInput("Ви впевнені, що хочете імпортувати? (так/ні): ");

        if (confirmation.equalsIgnoreCase("так") || confirmation.equalsIgnoreCase("yes")) {
            String filenamePrompt = "Введіть ім'я файлу для імпорту (за замовчуванням: " + DATA_FILE + "): ";
            String filenameInput = scanner.nextLine();
            String filename = filenameInput.trim().isEmpty() ? DATA_FILE : filenameInput.trim();

            if (!filename.toLowerCase().endsWith(".json")) {
                filename += ".json";
            }

            try {
                this.school = dataService.importSchoolFromJson(filename);
                System.out.println("Дані імпортовано. Поточна школа: " + school.getName());
            } catch (IOException e) {
                System.err.println("Помилка імпорту даних з файлу " + filename + ": " + e.getMessage());

            } catch (IllegalArgumentException e) {
                System.err.println("Помилка імпорту: " + e.getMessage());
            }
        } else {
            System.out.println("Імпорт скасовано.");
        }
    }

    private void saveDataOnExit() {
        System.out.println("--- Збереження даних перед виходом ---");
        try {
            dataService.exportSchoolToJson(school, DATA_FILE, null);
        } catch (IOException e) {
            System.err.println("Помилка збереження даних у файл " + DATA_FILE + ": " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Помилка збереження: " + e.getMessage());
        }
    }
}