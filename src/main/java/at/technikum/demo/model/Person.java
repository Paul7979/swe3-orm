package at.technikum.demo.model;

import at.technikum.orm.annotations.PrimaryKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
public abstract class Person {

  @PrimaryKey
  private int id;
  private String firstName;
  private String lastName;
  private LocalDate birthday;
  private Gender gender;



}