package at.technikum.demo.model.school;

import at.technikum.orm.annotations.Enitity;
import at.technikum.orm.annotations.ForeignKey;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Enitity(tableName = "CLASSES")
@Getter
@Setter
public class SClass 
{

    private String id;
    
    private String name;

    @ForeignKey(columnName = "")
    private Teacher teacher;

    @ForeignKey
    private List<Student> students;

}
