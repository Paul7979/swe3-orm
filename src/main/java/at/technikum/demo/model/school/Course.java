package at.technikum.demo.model.school;

import at.technikum.orm.annotations.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "COURSES")
@Data
@NoArgsConstructor
public class Course 
{
    @PrimaryKey
    private String id;

    private String name;

    private boolean active;

    @ForeignKey(columnName = "teacher_fk")
    private Teacher teacher_fk;

    @Ignore
    @ManyToMany(columnName = "students_fk", referenceTableName = "Student_Courses")
    private List<Student> _students;
}
