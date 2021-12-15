package at.technikum.demo;

import at.technikum.demo.model.school.Gender;
import at.technikum.demo.model.school.Teacher;
import at.technikum.orm.Orm;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;


@Slf4j
public class App 
{
    public static void main( String[] args ) {
        Orm orm = null;
        try {
            orm = new Orm("jdbc:postgresql://localhost:5432/postgres?user=platform&password=platform");
        } catch (SQLException e) {
            log.info("Failed to connect to db");
            return;
        }

        log.info("Storing teacher");
        try {
            orm.save(teacher1());
            log.info("Stored teacher");
        } catch (SQLException e) {
            log.info("Failed to store teacher", e);
        }




    }

    private static Teacher teacher1 () {
        var birthDate = LocalDate.of(1980, 2, 12);
        var hireDate = LocalDate.of(2018, 4, 1);
        Teacher teacher = new Teacher();
        teacher.setSalary(40000);
        teacher.setGender(Gender.MALE);
        teacher.setFirstName("Mickey");
        teacher.setName("Mickey");
        teacher.setId(UUID.randomUUID().toString());
        teacher.setHireDate(hireDate);
        teacher.setBirthDate(birthDate);
        return teacher;
    }
}
