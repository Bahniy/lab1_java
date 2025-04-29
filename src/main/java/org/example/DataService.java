package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DataService {

    private final ObjectMapper objectMapper;

    public DataService() {
        this.objectMapper = new ObjectMapper();
        // Налаштування для кращого форматування JSON
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void exportSchoolToJson(School school, String filePath, Comparator<Student> sorter) throws IOException {
        if (school == null || filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("School object and file path cannot be null or empty.");
        }

        School schoolToExport;
        if (sorter != null) {
            // Створюємо копію списку учнів, сортуємо її і створюємо тимчасовий об'єкт школи для експорту
            List<Student> sortedStudents = new ArrayList<>(school.getStudents());
            sortedStudents.sort(sorter);

            // Створюємо новий об'єкт School тільки для експорту з відсортованим списком
            schoolToExport = new School(school.getName());
            schoolToExport.setStudents(sortedStudents);
        } else {
            schoolToExport = school;
        }

        File file = new File(filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        objectMapper.writeValue(file, schoolToExport);
        System.out.println("School data successfully exported to " + filePath);
    }

    public School importSchoolFromJson(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Import file not found: " + filePath + ". Creating a new empty school.");
            return new School("Default School Name");
        }
        if (file.length() == 0) {
            System.out.println("Import file is empty: " + filePath + ". Creating a new empty school.");
            return new School("Default School Name");
        }

        School school = objectMapper.readValue(file, School.class);
        System.out.println("School data successfully imported from " + filePath);
        return school;
    }
}