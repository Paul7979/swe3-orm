package at.technikum.orm;

import at.technikum.demo.model.school.*;
import at.technikum.orm.model.Entity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static at.technikum.orm.Operation.*;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Orm.
 */
public class OrmTest {
    private static final String dbURL = "jdbc:postgresql://localhost:5432/postgres?user=platform&password=platform";
    private static Orm orm;

    static {
        try {
            orm = new Orm(dbURL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void givenAnEntityWhenSimpleSelectAllInternalFieldsThenSqlShouldBeGenerated()
    {
        var expected = "SELECT grade, hireDate, id, name, firstName, birthDate, gender FROM student";
        var entity = Entity.ofClass(Student.class);

        var sql = Orm.simpleSelectAllInternalFields(entity);

        Assert.assertEquals(expected, sql);
    }

    @Test
    public void givenEntityWhenStoringThenGetShouldReturn() throws SQLException {
        var sid_1 = "sid_1";
        Student student = new Student(sid_1, "name", "firstName", LocalDate.of(2000,1,1), Gender.MALE, 1,LocalDate.of(2020,1,1), emptyList());

        orm.save(student);

        var student1 = orm.get(Student.class, sid_1);
        assertThat(student).isEqualTo(student1);
    }

    @Test
    public void givenEntityWithSimpleForeignFieldsWhenStoringThenGetShouldReturnForeignFields() throws SQLException {
        Teacher teacher = new Teacher("1", "name", "firstName", LocalDate.of(2000,1,1), Gender.MALE, 1,LocalDate.of(2020,1,1), null, null);
        var course = new Course("cid", "course", true, teacher, null);

        orm.save(course);

        var course1 = orm.get(Course.class, "cid");
        assertThat(course).isEqualTo(course1);
    }

    @Test
    public void givenEntityWithSimpleForeignFieldsWhenStoringThenGetForeignObjectShouldReturnFields() throws SQLException {
        Teacher teacher = new Teacher("1", "name", "firstName", LocalDate.of(2000,1,1), Gender.MALE, 1,LocalDate.of(2020,1,1), null, null);
        var course = new Course("cid", "course", true, teacher, null);

        orm.save(course);

        var teacher1 = orm.get(Teacher.class, "1");
        assertThat(teacher).isEqualTo(teacher1);
    }

    @Test
    public void givenObjectsInDbWhenUsingFluentSelectCollectionIsReturned() throws SQLException {
        Teacher teacher = new Teacher("1", "name", "firstName", LocalDate.of(2000,1,1), Gender.MALE, 100,LocalDate.of(2020,1,1), null, null);
        Teacher teacher1 = new Teacher("2", "name1", "firstName1", LocalDate.of(2000,1,1), Gender.MALE, 100,LocalDate.of(2020,1,1), null, null);
        Teacher teacher2 = new Teacher("3", "name2", "firstName2", LocalDate.of(2000,1,1), Gender.MALE, 100,LocalDate.of(2020,1,1), null, null);
        orm.save(teacher);
        orm.save(teacher1);
        orm.save(teacher2);

        var teachers = orm.select(Teacher.class).where("salary", GREATER, 10).get(Teacher.class);

        assertThat(teachers).hasSize(3);
    }

    @Test
    public void givenObjectsInDbWhenUsingFluentSelectWithAndThenCollectionIsReturned() throws SQLException {
        Teacher teacher = new Teacher("1", "name", "firstName", LocalDate.of(2000,1,1), Gender.MALE, 100,LocalDate.of(2020,1,1), null, null);
        Teacher teacher1 = new Teacher("2", "name1", "firstName1", LocalDate.of(2000,1,1), Gender.MALE, 100,LocalDate.of(2020,1,1), null, null);
        Teacher teacher2 = new Teacher("3", "name2", "firstName2", LocalDate.of(2000,1,1), Gender.MALE, 100,LocalDate.of(2020,1,1), null, null);
        orm.save(teacher);
        orm.save(teacher1);
        orm.save(teacher2);

        var teachers = orm.select(Teacher.class).where("salary", GREATER, 10).and("name", NOT, "name" ).get(Teacher.class);

        assertThat(teachers).hasSize(2);
    }

    @Test
    public void givenObjectsInDbWhenUsingFluentSelectWithConcatenatedAndThenCollectionIsReturned() throws SQLException {
        List<Teacher> teachers = List.of(teacherWithSalary(1),
        teacherWithSalary(2),
        teacherWithSalary(3),
        teacherWithSalary(4),
        teacherWithSalary(5),
        teacherWithSalary(6));

        for (Teacher teacher : teachers) {
            orm.save(teacher);
        }


        var teachersFromDB = orm.select(Teacher.class)
          .where("name", NOT, " ")
          .and("salary", GREATER, 2)
          .and("salary", LESS, 5)
          .get(Teacher.class);


        assertThat(teachersFromDB).hasSize(2);
    }

    @Test
    public void givenObjectsInDbWhenUsingFluentSelectWithConcatenatedORTThenCollectionIsReturned() throws SQLException {
        List<Teacher> teachers = List.of(teacherWithSalary(1),
          teacherWithSalary(2),
          teacherWithSalary(3),
          teacherWithSalary(4),
          teacherWithSalary(5),
          teacherWithSalary(6));

        for (Teacher teacher : teachers) {
            orm.save(teacher);
        }


        var teachersFromDB = orm.select(Teacher.class)
          .where("salary", IS, 2)
          .or("salary", IS, 1)
          .or("salary", GREATER, 5)
          .get(Teacher.class);


        assertThat(teachersFromDB).hasSize(3);
    }

    @Test
    public void givenEntityWithForeignMToNFieldsWhenStoringThenGetShouldReturnObjectWithForeignFields() throws SQLException {
        var sid_1 = "sid_1";
        Student student = new Student(sid_1, "name", "firstName", LocalDate.of(2000,1,1), Gender.MALE, 1,LocalDate.of(2020,1,1), null);
        orm.save(student);
        Teacher teacher = new Teacher("1", "name", "firstName", LocalDate.of(2000,1,1), Gender.MALE, 1,LocalDate.of(2020,1,1), null, null);
        List<SClass> classes = new ArrayList<>();
        classes.add(new SClass("scid_1", "class1", teacher, emptyList()));
        classes.add(new SClass("scid_2", "class2", teacher, emptyList()));
        student.setSClass(classes);
        orm.save(student);
        var student1 = orm.get(Student.class, sid_1);
        assertThat(student).isEqualTo(student1);
        assertThat(student1.getSClass()).hasSize(2);
    }

    @Before
    public void cleanUpDB() {
        orm.deleteTableData("class");
        orm.deleteTableData("courses");
        orm.deleteTableData("student");
        orm.deleteTableData("student_courses");
        orm.deleteTableData("student_classes");
        orm.deleteTableData("teachers");
    }

    private static Teacher teacherWithSalary(int salary) {
        return new Teacher(UUID.randomUUID().toString(), "name", "firstName", LocalDate.of(2000,1,1), Gender.MALE, salary,LocalDate.of(2020,1,1), null, null);
    }

}
