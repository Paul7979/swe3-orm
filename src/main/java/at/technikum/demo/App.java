package at.technikum.demo;

import at.technikum.demo.model.school.Gender;
import at.technikum.demo.model.school.SClass;
import at.technikum.demo.model.school.Teacher;
import at.technikum.orm.Orm;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;


@Slf4j
public class App 
{

    public static final String TEACHER1_ID = "t_1";
    public static final String TEACHER2_ID = "t_2";
    public static final String TEACHER3_ID = "t_3";

    public static void main(String[] args ) throws SQLException {
      Orm orm = getOrm();

      //storeTeacher1(orm);

      //fetchTeacher1(orm);
      saveClassWithTeacher1ToN(orm);
      //var sClass2 = sClass2(teacher(TEACHER1_ID));
      //orm.save(sClass2);

      //storeTeacherWithClasses1ToN(orm);

      //var teacher2 = orm.get(Teacher.class, TEACHER2_ID);

    }

  private static void saveClassWithTeacher1ToN(Orm orm) throws SQLException {
    var sClass1 = sClass1(teacher(TEACHER2_ID));
    orm.save(sClass1);
  }

  private static void storeTeacherWithClasses1ToN(Orm orm) throws SQLException {
    var teacher3 = teacher(TEACHER3_ID);
    teacher3.setClasses(List.of(sClassId(teacher3, "cl_1"), sClassId(teacher3, "cl_2")));
    orm.save(teacher3);
  }

  private static Orm getOrm() throws SQLException {
    return new Orm("jdbc:postgresql://localhost:5432/postgres?user=platform&password=platform");
  }

  private static void storeTeacher1(Orm orm) throws SQLException {
    log.info("Storing teacher");
    orm.save(teacher(TEACHER1_ID));
    log.info("Stored teacher");
  }

  private static void fetchTeacher1(Orm orm) {
    try {
       var teacher = orm.get(Teacher.class, TEACHER1_ID);
        log.info("Fetched Teacher {} {}", teacher.getFirstName(), teacher.getName());
    } catch (SQLException e) {
        log.error("Could not fetch teacher", e);
    }
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

    private static SClass sClassId(Teacher teacher, String id) {
        SClass sClass = new SClass();
        sClass.setId(id);
        sClass.setName("RANDOM 101");
        sClass.setTeacher(teacher);
        return sClass;
    }


}
