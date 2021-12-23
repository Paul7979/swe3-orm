package at.technikum.demo;

import at.technikum.demo.model.school.Gender;
import at.technikum.demo.model.school.SClass;
import at.technikum.demo.model.school.Teacher;
import at.technikum.orm.Orm;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;


@Slf4j
public class App 
{

    public static final String TEACHER1_ID = "t_1";
    public static final String TEACHER2_ID = "t_2";
    public static final String TEACHER3_ID = "t_3";

    public static void main(String[] args ) throws SQLException {
        Orm orm = null;
        try {
            orm = new Orm("jdbc:postgresql://localhost:5432/postgres?user=platform&password=platform");
        } catch (SQLException e) {
            log.info("Failed to connect to db");
            return;
        }

        log.info("Storing teacher");
        //orm.save(teacher(TEACHER1_ID));
        log.info("Stored teacher");

        try {
            var teacher = orm.get(Teacher.class, TEACHER1_ID);
            log.info("Fetched Teacher {} {}", teacher.getFirstName(), teacher.getName());
        } catch (SQLException e) {
            log.error("Could not fetch teacher", e);
        }
        var sClass1 = sClass1(teacher(TEACHER2_ID));
        var sClass2 = sClass2(teacher(TEACHER1_ID));
        //orm.save(sClass1);
        //orm.save(sClass2);

        var teacher3 = teacher(TEACHER3_ID);
        teacher3.setClasses(List.of(sClassRandId(teacher3), sClassRandId(teacher3)));
        orm.save(teacher3);

        var teacher2 = orm.get(Teacher.class, TEACHER2_ID);

    }

    private static Teacher teacher (String id) {
        var birthDate = LocalDate.of(1980, 2, 12);
        var hireDate = LocalDate.of(2018, 4, 1);
        Teacher teacher = new Teacher();
        teacher.setSalary(50000);
        teacher.setGender(Gender.MALE);
        teacher.setFirstName("Mickey");
        teacher.setName("Mouse");
        teacher.setId(id);
        teacher.setHireDate(hireDate);
        teacher.setBirthDate(birthDate);
        return teacher;
    }

    private static SClass sClass1(Teacher teacher) {
        SClass sClass = new SClass();
        sClass.setId("s_1");
        sClass.setName("DATABASES 101");
        sClass.setTeacher(teacher);
        return sClass;
    }
    private static SClass sClass2(Teacher teacher) {
        SClass sClass = new SClass();
        sClass.setId("s_2");
        sClass.setName("TESTING 101");
        sClass.setTeacher(teacher);
        return sClass;
    }

    private static SClass sClassRandId(Teacher teacher) {
        SClass sClass = new SClass();
        sClass.setId(UUID.randomUUID().toString());
        sClass.setName("RANDOM 101");
        sClass.setTeacher(teacher);
        return sClass;
    }


}
