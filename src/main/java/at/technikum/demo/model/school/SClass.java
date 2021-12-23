package at.technikum.demo.model.school;

import at.technikum.orm.annotations.Enitity;
import at.technikum.orm.annotations.ForeignKey;
import at.technikum.orm.annotations.PrimaryKey;
import lombok.Getter;
import lombok.Setter;


@Enitity(tableName = "class")
@Getter
@Setter
public class SClass 
{

    @PrimaryKey
    private String id;
    
    private String name;

    @ForeignKey(columnName = "fk_teacher")
    private Teacher teacher;

    /*@ForeignKey(columnName = "fk_student")
    private List<Student> students;*/

}
