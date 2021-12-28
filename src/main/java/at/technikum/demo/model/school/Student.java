package at.technikum.demo.model.school;

import at.technikum.orm.annotations.Entity;
import at.technikum.orm.annotations.ForeignKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity(tableName = "STUDENTS")
@Setter
@Getter
@ToString
@NoArgsConstructor
public class Student extends Person {

    private int grade;

    private LocalDate hireDate;

    @ForeignKey
    private SClass sClass;

    public Student(String id, String name, String firstName, LocalDate birthDate, Gender gender, int grade, LocalDate hireDate, SClass sClass) {
        super(id, name, firstName, birthDate, gender);
        this.grade = grade;
        this.hireDate = hireDate;
        this.sClass = sClass;
    }
}
