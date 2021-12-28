package at.technikum.demo.model.school;

import at.technikum.orm.annotations.Entity;
import at.technikum.orm.annotations.ForeignKey;
import at.technikum.orm.annotations.ManyToMany;
import at.technikum.orm.annotations.PrimaryKey;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;


@Entity(tableName = "class")
@Getter
@Setter
@ToString
public class SClass {

  @PrimaryKey
  private String id;

  private String name;

  @ForeignKey(columnName = "fk_teacher")
  private Teacher teacher;

  @ManyToMany(referencedColumnName = "fk_class", referenceTableName = "student_courses")
  private List<Student> students;
}
