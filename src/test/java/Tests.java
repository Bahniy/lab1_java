import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.DataService;
import org.example.Discipline;
import org.example.School;
import org.example.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class Tests {

    private DataService dataService;
    private School testSchool;
    private Student student1, student2;

    @TempDir
    Path tempDir;

    private File testFile;

    @BeforeEach
    void setUp() {
        dataService = new DataService();
        testSchool = new School("Test Export School");

        student1 = new Student(10, "Zara", "Alpha");
        student1.addOrUpdateDiscipline(new Discipline("Math", 10));

        student2 = new Student(5, "Adam", "Beta");
        student2.addOrUpdateDiscipline(new Discipline("Physics", 9));

        testSchool.addStudent(student1);
        testSchool.addStudent(student2);

        // Створюємо шлях до тестового файлу всередині тимчасової директорії
        testFile = tempDir.resolve("test_school_data.json").toFile();
    }


    @Test
    void testExportSchoolToJsonSortByName() throws IOException {
        Comparator<Student> sorter = Comparator.comparing(Student::getLastName); // Alpha before Beta
        dataService.exportSchoolToJson(testSchool, testFile.getAbsolutePath(), sorter);

        assertTrue(testFile.exists());
        String content = Files.readString(testFile.toPath());
        // Перевіримо, що Zara Alpha йде перед Adam Beta
        assertTrue(content.indexOf("\"lastName\" : \"Alpha\"") < content.indexOf("\"lastName\" : \"Beta\""));
    }

    @Test
    void testExportSchoolToJsonNullSchool() {
        assertThrows(IllegalArgumentException.class, () -> {
            dataService.exportSchoolToJson(null, testFile.getAbsolutePath(), null);
        });
    }


    @Test
    void testImportSchoolFromJsonSuccess() throws IOException {
        // Спочатку експортуємо, щоб створити файл
        dataService.exportSchoolToJson(testSchool, testFile.getAbsolutePath(), null);

        // Тепер імпортуємо
        School importedSchool = dataService.importSchoolFromJson(testFile.getAbsolutePath());

        assertNotNull(importedSchool);
        assertEquals(testSchool.getName(), importedSchool.getName());
        assertEquals(testSchool.getStudents().size(), importedSchool.getStudents().size());

        // Перевіримо наявність учнів (equals базується на ID)
        assertTrue(importedSchool.getStudents().contains(student1));
        assertTrue(importedSchool.getStudents().contains(student2));

        // Перевіримо деталі одного учня
        Student importedStudent1 = importedSchool.getStudentById(10).orElse(null);
        assertNotNull(importedStudent1);
        assertEquals("Zara", importedStudent1.getFirstName());
        assertEquals(1, importedStudent1.getDisciplines().size());
        assertEquals("Math", importedStudent1.getDisciplines().get(0).getName());
        assertEquals(10, importedStudent1.getDisciplines().get(0).getGrade());
    }

    @Test
    void testImportSchoolFromJsonFileNotFound() {
        // Не створюємо файл, просто намагаємось імпортувати
        String nonExistentFilePath = tempDir.resolve("non_existent_file.json").toString();
        // Очікуємо, що метод обробить це і поверне нову порожню школу (згідно нашої реалізації)
        assertDoesNotThrow(() -> {
            School school = dataService.importSchoolFromJson(nonExistentFilePath);
            assertNotNull(school);
            assertTrue(school.getStudents().isEmpty());
        });
    }


    @Test
    void testImportSchoolFromJsonInvalidJson() throws IOException {
        // Створюємо файл з некоректним JSON
        Files.writeString(testFile.toPath(), "{ invalid json data }");

        // Очікуємо, що Jackson кине виняток при парсингу, який буде прокинуто далі
        assertThrows(JsonProcessingException.class, () -> {
            dataService.importSchoolFromJson(testFile.getAbsolutePath());
        });
    }
}

class SchoolTest {

    private School school;
    private Student student1;
    private Student student2;
    private Student student3; // Студент без оцінок

    @BeforeEach
    void setUp() {
        school = new School("Test School");
        student1 = new Student(1, "Alice", "Smith");
        student1.addOrUpdateDiscipline(new Discipline("Math", 10)); //  10.0
        student1.addOrUpdateDiscipline(new Discipline("Art", 8));    //  (10+8)/2 = 9.0

        student2 = new Student(2, "Bob", "Jones");
        student2.addOrUpdateDiscipline(new Discipline("Physics", 11)); //  11.0
        student2.addOrUpdateDiscipline(new Discipline("History", 7));  //  (11+7)/2 = 9.0

        student3 = new Student(3, "Charlie", "Brown"); // Нема предметів, оцінок

        school.addStudent(student1);
        school.addStudent(student2);
    }

    @Test
    void testSchoolCreationValid() {
        School newSchool = new School("Valid Name");
        assertEquals("Valid Name", newSchool.getName());
        assertTrue(newSchool.getStudents().isEmpty());
    }

    @Test
    void testAddStudentDuplicateId() {
        Student duplicateStudent = new Student(1, "Alicia", "Smith"); // Same ID as student1
        assertFalse(school.addStudent(duplicateStudent));
        assertEquals(2, school.getStudents().size());
        assertEquals("Alice", school.getStudentById(1).get().getFirstName());
    }

    @Test
    void testAddStudentNull() {
        assertFalse(school.addStudent(null));
        assertEquals(2, school.getStudents().size());
    }

    @Test
    void testGetStudentByIdNotFound() {
        Optional<Student> notFound = school.getStudentById(99);
        assertFalse(notFound.isPresent());
    }

    @Test
    void testUpdateStudentFound() {
        Student updatedStudent2 = new Student(2, "Bob", "Johnson");
        updatedStudent2.addOrUpdateDiscipline(new Discipline("Physics", 12));
        updatedStudent2.addOrUpdateDiscipline(new Discipline("History", 7));

        assertTrue(school.updateStudent(updatedStudent2));

        Optional<Student> refetchedOpt = school.getStudentById(2);
        assertTrue(refetchedOpt.isPresent());
        Student refetched = refetchedOpt.get();
        assertEquals("Johnson", refetched.getLastName());
        assertEquals("Bob", refetched.getFirstName()); // Ім'я не повинно змінитися
        // перевіримо чи змінилася дисципліна
        assertEquals(2, refetched.getDisciplines().size());
        assertEquals(12, refetched.getDisciplines().stream().filter(d -> d.getName().equals("Physics")).findFirst().get().getGrade());
    }

    @Test
    void testUpdateStudentNotFound() {
        Student nonExistent = new Student(99, "Unknown", "Person");
        assertFalse(school.updateStudent(nonExistent));
    }

    @Test
    void testSortStudentsByName() {
        // Студенти: Alice Smith (1), Bob Jones (2)
        school.addStudent(student3); // Charlie Brown (3)
        // Сортувати за прізвищем: Brown, Jones, Smith
        school.sortStudents(Comparator.comparing(Student::getLastName));

        List<Student> sortedStudents = school.getStudents();
        assertEquals("Brown", sortedStudents.get(0).getLastName());
        assertEquals("Jones", sortedStudents.get(1).getLastName());
        assertEquals("Smith", sortedStudents.get(2).getLastName());
    }
}

class StudentTest {

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student(1, "John", "Doe");
    }

    @Test
    void testStudentCreationEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> new Student(2, "", "Doe"));
        assertThrows(IllegalArgumentException.class, () -> new Student(3, "Jane", ""));
        assertThrows(IllegalArgumentException.class, () -> new Student(4, null, "Doe"));
        assertThrows(IllegalArgumentException.class, () -> new Student(5, "Jane", null));
    }


    @Test
    void testAddOrUpdateDisciplineAddNew() {
        Discipline math = new Discipline("Math", 10);
        student.addOrUpdateDiscipline(math);
        assertEquals(1, student.getDisciplines().size());
        assertEquals(math, student.getDisciplines().get(0));
        assertEquals(10.0, student.calculateAverageGrade());
    }

    @Test
    void testAddOrUpdateDisciplineUpdateExisting() {
        Discipline math1 = new Discipline("Math", 8);
        Discipline math2 = new Discipline("Math", 11); // Same name, different grade
        student.addOrUpdateDiscipline(math1);
        student.addOrUpdateDiscipline(math2); // Should update the grade

        assertEquals(1, student.getDisciplines().size());
        assertEquals("Math", student.getDisciplines().get(0).getName());
        assertEquals(11, student.getDisciplines().get(0).getGrade()); // Grade should be updated
        assertEquals(11.0, student.calculateAverageGrade());
    }

    @Test
    void testRemoveDisciplineExists() {
        Discipline math = new Discipline("Math", 10);
        Discipline physics = new Discipline("Physics", 9);
        student.addOrUpdateDiscipline(math);
        student.addOrUpdateDiscipline(physics);

        assertTrue(student.removeDiscipline("Math"));
        assertEquals(1, student.getDisciplines().size());
        assertEquals(physics, student.getDisciplines().get(0));
        assertEquals(9.0, student.calculateAverageGrade());
    }

    @Test
    void testEqualsAndHashCode() {
        Student student1 = new Student(10, "Alice", "Smith");
        Student student2 = new Student(10, "Alice", "Smith"); // Same ID
        Student student3 = new Student(11, "Bob", "Jones"); // Different ID
        Student student4 = new Student(10, "Alicia", "Smithy"); // Same ID, different name

        assertEquals(student1, student2);
        assertNotEquals(student1, student3);
        assertEquals(student1, student4);

        assertEquals(student1.hashCode(), student2.hashCode());
        assertNotEquals(student1.hashCode(), student3.hashCode());
        assertEquals(student1.hashCode(), student4.hashCode());
    }

    @Test
    void testGetDisciplinesReturnsCopy() {
        Discipline math = new Discipline("Math", 10);
        student.addOrUpdateDiscipline(math);

        List<Discipline> disciplinesCopy = student.getDisciplines();
        assertEquals(1, disciplinesCopy.size());

        // Try to modify the returned list
        disciplinesCopy.add(new Discipline("Physics", 9));

        // Check if the original list in the student object was modified
        assertEquals(1, student.getDisciplines().size()); // Should still be 1
        assertEquals("Math", student.getDisciplines().get(0).getName());
    }
}