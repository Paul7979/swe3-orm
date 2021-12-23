package at.technikum.demo.model.school;

import at.technikum.orm.annotations.Ignore;
import at.technikum.orm.annotations.PrimaryKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;



@Getter
@Setter
@NoArgsConstructor
public abstract class Person {

    private static int counter = 1;

    @PrimaryKey
    private String id;
    
    private String name;
    
    private String firstName;
    
    private LocalDate birthDate;
    
    private Gender gender;

    @Ignore
    private int instanceNumber;

    public Person(String id, String name, String firstName, LocalDate birthDate, Gender gender) {
        this.id = id;
        this.name = name;
        this.firstName = firstName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.instanceNumber = counter++;
    }
}
