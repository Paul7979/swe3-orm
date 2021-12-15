package at.technikum.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Teacher extends Person{

  private LocalDate hiredOn;

  public Teacher(int id, String firstName, String lastName, LocalDate birthday, Gender gender, LocalDate hiredOn) {
    super(id, firstName, lastName, birthday, gender);
    this.hiredOn = hiredOn;
  }
}
