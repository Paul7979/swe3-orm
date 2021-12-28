package at.technikum.demo.model.school;

import at.technikum.orm.annotations.Entity;
import at.technikum.orm.annotations.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity(tableName = "student")
@Setter
@Getter
@ToString(callSuper = true)
@NoArgsConstructor
public class Student extends Person {

  private int grade;

  private LocalDate hireDate;

  @ManyToMany(referencedColumnName = "fk_student", referenceTableName = "student_courses")
  private SClass sClass;

  public Student(String id, String name, String firstName, LocalDate birthDate, Gender gender, int grade, LocalDate hireDate, SClass sClass) {
    super(id, name, firstName, birthDate, gender);
    this.grade = grade;
    this.hireDate = hireDate;
    this.sClass = sClass;
  }
}
