package at.technikum.demo;

import at.technikum.demo.model.school.Gender;
import at.technikum.demo.model.school.SClass;
import at.technikum.demo.model.school.Student;
import at.technikum.demo.model.school.Teacher;
import at.technikum.orm.Orm;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Slf4j
public class App {

  public static final String TEACHER0_ID = "t_0";
  public static final String TEACHER1_ID = "t_1";
  public static final String TEACHER2_ID = "t_2";
  public static final String TEACHER3_ID = "t_3";

  public static void main(String[] args) throws SQLException {
    Orm orm = getOrm();
    sampleMToN(orm);
    storeTeacher0(orm);
    var t0 = orm.get(Teacher.class, TEACHER0_ID);
    log.info("Fetched teacher 0: {}", t0);
    saveClassWithTeacher1ToN(orm);
    var t1 = orm.get(Teacher.class, TEACHER1_ID);
    log.info("Fetched teacher 1: {}", t1);

    var sClass2 = sClass2(teacher(TEACHER2_ID));
    orm.save(sClass2);
    var t2 = orm.get(Teacher.class, TEACHER2_ID);
    log.info("Fetched teacher 2: {}", t2);
    sampleMToN(orm);
  }

  private static void sampleMToN(Orm orm) throws SQLException {
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
    var getStudent1 = orm.get(Student.class, student1Id);
    log.info("Got Student {}", getStudent1);
  }

  private static void testMToN(Orm orm) throws SQLException {
    var teacher1ID = "teacher_1";
    var teacher2ID = "teacher_2";

    var teacher1 = teacher(teacher1ID);
    var teacher2 = teacher(teacher2ID);

    orm.save(teacher1);
    orm.save(teacher2);

    var student1Id = "student_1";
    var student2Id = "student_2";
    var student3Id = "student_3";

    var student1 = student(student1Id);
    var student2 = student(student2Id);
    var student3 = student(student3Id);

    orm.save(student1);
    orm.save(student2);
    orm.save(student3);

    var classId1 = "a_class_1";
    var classId2 = "a_class_2";
    var classId3 = "a_class_3";

    var aClass1 = sClassId(teacher1, classId1);
    var aClass2 = sClassId(teacher1, classId1);
    var aClass3 = sClassId(teacher1, classId1);


  }

  private static void saveClassWithTeacher1ToN(Orm orm) throws SQLException {
    var sClass1 = sClass1(teacher(TEACHER1_ID));
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

  private static void storeTeacher0(Orm orm) throws SQLException {
    log.info("Storing teacher");
    orm.save(teacher(TEACHER0_ID));
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

  private static Teacher fetchTeacher(Orm orm, String teacherId) {
    try {
      var teacher = orm.get(Teacher.class, teacherId);
      log.info("Fetched Teacher {} {}", teacher.getFirstName(), teacher.getName());
      return teacher;
    } catch (SQLException e) {
      log.error("Could not fetch teacher", e);
      throw new RuntimeException(e);
    }
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
