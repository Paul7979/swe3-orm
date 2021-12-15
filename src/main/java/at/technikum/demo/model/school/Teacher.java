package at.technikum.demo.model.school;

import at.technikum.orm.annotations.Enitity;
import at.technikum.orm.annotations.ForeignKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Enitity(tableName = "teachers")
@Setter
@Getter
@NoArgsConstructor
public class Teacher extends Person {

    private int salary;
    
    private LocalDate hireDate;

    @ForeignKey
    private List<SClass> classes;

    @ForeignKey
    private List<Course> courses;

    public Teacher(String id, String name, String firstName, LocalDate birthDate, Gender gender, int salary, LocalDate hireDate, List<SClass> classes, List<Course> courses) {
        super(id, name, firstName, birthDate, gender);
        this.salary = salary;
        this.hireDate = hireDate;
        this.classes = classes;
        this.courses = courses;
    }
}
