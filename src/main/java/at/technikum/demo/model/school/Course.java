package at.technikum.demo.model.school;

import at.technikum.orm.annotations.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity(tableName = "COURSES")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course {
  @PrimaryKey
  private String id;

  private String name;

  private boolean active;

  @ForeignKey(columnName = "teacher_fk")
  private Teacher teacher_fk;

  @ManyToMany(referencedColumnName = "fk_student", referenceTableName = "student_Courses")
  private List<Student> students;
}
