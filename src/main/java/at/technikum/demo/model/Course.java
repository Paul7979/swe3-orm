package at.technikum.demo.model;

import at.technikum.orm.annotations.Enitity;
import at.technikum.orm.annotations.ForeignKey;
import lombok.Data;

import java.util.List;

@Enitity(tableName = "t_course")
@Data
public class Course {

  private String name;

  @ForeignKey(referencesTable = "t_students", columnName = "fk_students")
  private List<Student> students;


}
