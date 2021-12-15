package at.technikum.demo.model;

import at.technikum.orm.annotations.Column;
import at.technikum.orm.annotations.Enitity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Enitity
public class Student extends Person {

  @Column(name = "fk_StudentNo")
  private int studentNumber;

  public Student(int id, String firstName, String lastName, LocalDate birthday, Gender gender, int studentNumber) {
    super(id, firstName, lastName, birthday, gender);
    this.studentNumber = studentNumber;
  }
}
