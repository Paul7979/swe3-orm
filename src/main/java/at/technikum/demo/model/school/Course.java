package at.technikum.demo.model.school;

import at.technikum.orm.annotations.Enitity;
import at.technikum.orm.annotations.ForeignKey;
import at.technikum.orm.annotations.Ignore;
import at.technikum.orm.annotations.PrimaryKey;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Enitity(tableName = "COURSES")
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
    @ForeignKey(columnName = "Students", referencesTable = "")
    private ArrayList<Student> _students;

    
  /*
    @ForeignKey(assignmentTable = "STUDENT_COURSES", columnName = "KCOURSE", remoteColumnName = "KSTUDENT", columnType = Student.class)
    public ArrayList<Student> getStudents()
    {
        return _students;
    }

    @ForeignKey(fieldName = "Students")
    private void _setStudents(ArrayList<Student> value)
    {
        _students = value;
    }*/
}
