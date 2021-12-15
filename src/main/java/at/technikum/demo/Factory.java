package at.technikum.demo;

import at.technikum.demo.model.Gender;
import at.technikum.demo.model.Student;

import java.time.LocalDate;
import java.util.Random;

public class Factory {
  static final Random random = new Random();

  public static Student buildStudent() {
    var birthday = LocalDate.of(2000, 1, 1);
    return new Student(random.nextInt(),"Max", "Mustermann", birthday, Gender.Male, random.nextInt());
  }
}
