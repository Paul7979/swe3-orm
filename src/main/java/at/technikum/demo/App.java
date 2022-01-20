package at.technikum.demo;

import at.technikum.demo.model.school.*;
import at.technikum.orm.Operation;
import at.technikum.orm.Orm;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Slf4j
public class App {

  public static final String line = "-----------------------------------------------------------------------------------------------";

  public static final String TEACHER0_ID = "t_0";
  public static final String TEACHER1_ID = "t_1";

  public static void main(String[] args) throws SQLException {
    Orm orm = getOrm();

    storeTeacherAndRetrieve0(orm);
    log.info(line);

    saveClassWithTeacherAndRetrieve1ToN(orm);
    log.info(line);

    saveClassAndUpdateAfterwards(orm);
    log.info(line);

    sampleMToN(orm);
    log.info(line);

    samplesForFluentApi(orm);
  }

  private static void saveClassAndUpdateAfterwards(Orm orm) throws SQLException {
    SClass sClass = new SClass("scID", "name", null, null);
    orm.save(sClass);
    var beforeUpdate = orm.get(SClass.class, "scID");
    log.info("Before Update Class has teacher {}", beforeUpdate.getTeacher());
    sClass.setTeacher(teacher("teacherID"));
    orm.save(sClass);
    var afterUpdate = orm.get(SClass.class, "scID");
    log.info("After Update Class has teacher {}", afterUpdate.getTeacher());
  }

  private static void storeTeacherAndRetrieve0(Orm orm) throws SQLException {
    log.info("Storing teacher");
    orm.save(teacher(TEACHER0_ID));
    log.info("Stored teacher");
    var t0 = orm.get(Teacher.class, TEACHER0_ID);
    log.info("Fetched teacher 0: {}", t0);
  }

  private static void saveClassWithTeacherAndRetrieve1ToN(Orm orm) throws SQLException {
    var sClass1 = sClass1(teacher(TEACHER1_ID));
    orm.save(sClass1);
    var t1 = orm.get(Teacher.class, TEACHER1_ID);
    log.info("Fetched teacher 1 with classes: {}", t1.getClasses());
  }

  private static void samplesForFluentApi(Orm orm) throws SQLException {
    orm.deleteTableData(Course.class);
    var course1 = new Course("1", "course1", true, null, null);
    var course2 = new Course("2", "course2", false, null, null);
    var course3 = new Course("3", "course3", true, null, null);
    var course4 = new Course("4", "course4", true, null, null);

    orm.save(course1);
    orm.save(course2);
    orm.save(course3);

    var courses = orm.select(Course.class).where("active", Operation.IS, true).and("name", Operation.NOT, "course4").get(Course.class);
    log.info("Got {} courses - should be 2", courses.size());
  }

  private static void sampleMToN(Orm orm) throws SQLException {
    log.info("Starting m to n example");

    var student1Id = "student_1";
    var student2Id = "student_2";

    var student1 = student(student1Id);
    var student2 = student(student2Id);
    orm.save(student1);
    orm.save(student2);
    var teacher1ID = "teacher_1";
    var teacher1 = teacher(teacher1ID);
    orm.save(teacher1);
    var classId1 = "a_class_1";
    var aClass1 = sClassId(teacher1, classId1);
    orm.save(aClass1);
    aClass1.setStudents(List.of(student1, student2));
    orm.save(aClass1);
    var classId2 = "a_class_2";
    var aClass2 = sClassId(teacher1, classId2);
    orm.save(aClass2);
    aClass2.setStudents(List.of(student1, student2));
    orm.save(aClass2);
    var getStudent1 = orm.get(Student.class, student1Id);

    log.info("Got Student1 with classes {}", getStudent1.getSClass());

    var getClass2 = orm.get(SClass.class, classId2);
    log.info("Got SClass2 with Students {}", getClass2.getStudents());
  }


  private static Orm getOrm() throws SQLException {
    return new Orm("jdbc:postgresql://localhost:5432/postgres?user=platform&password=platform");
  }

  private static Teacher teacher(String id) {
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

  private static Student student(String id) {
    var birthDate = randomDate(null);
    var hireDate = randomDate(birthDate.toEpochDay());
    var student = new Student();
    student.setGender(Gender.MALE);
    student.setFirstName("Student");
    student.setName("Some");
    student.setId(id);
    student.setHireDate(hireDate);
    student.setBirthDate(birthDate);
    student.setGrade(1);
    return student;
  }

  static LocalDate randomDate(Long minDayIn) {
    long minDay = minDayIn == null ? LocalDate.of(1970, 1, 1).toEpochDay() : minDayIn;
    long maxDay = LocalDate.of(2021, 12, 31).toEpochDay();
    long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
    return LocalDate.ofEpochDay(randomDay);
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
